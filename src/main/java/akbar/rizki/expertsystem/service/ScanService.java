package akbar.rizki.expertsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import akbar.rizki.expertsystem.dto.ScanResponse;
import akbar.rizki.expertsystem.entity.*;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ScanService {

    @Inject
    FileValidationService fileValidationService;

    @Inject
    SemgrepService semgrepService;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Alur lengkap: validasi & simpan file -> scan Semgrep -> parse -> matching ke
     * knowledge base -> simpan scan_submission + scan_results -> update saran status
     * di application_checklist_items -> hapus file sementara.
     *
     * @param applicationChecklistItemId item checklist yang sedang dicek
     * @param frameworkId                framework yang relevan untuk matching rule (mis. Quarkus)
     * @param submittedBy                user id yang upload
     */
    public ScanResponse processScan(Long applicationChecklistItemId, Integer frameworkId,
                                    Long submittedBy, FileUpload upload) {

        ApplicationChecklistItem item = ApplicationChecklistItem.findById(applicationChecklistItemId);
        if (item == null) {
            throw new NotFoundException("Checklist item tidak ditemukan: " + applicationChecklistItemId);
        }

        FileValidationService.StoredFile stored;
        try {
            stored = fileValidationService.validateAndStore(upload);
        } catch (Exception e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage(), e);
        }

        ScanSubmission submission = new ScanSubmission();
        submission.applicationChecklistItemId = applicationChecklistItemId;
        submission.originalFilename = stored.originalFilename();
        submission.storedFilename = stored.storedFilename();
        submission.fileExtension = stored.extension();
        submission.submittedBy = submittedBy;

        long startTime = System.currentTimeMillis();

        try {
            String rawJson = runScanSafely(stored.path(), submission);
            submission.scanDurationMs = (int) (System.currentTimeMillis() - startTime);

            List<ScanResponse.Finding> findings = new ArrayList<>();
            List<ScanResult> resultsToSave = new ArrayList<>();

            if (submission.scanStatus.equals("SUCCESS")) {
                JsonNode root = objectMapper.readTree(rawJson);
                JsonNode results = root.path("results");

                for (JsonNode result : results) {
                    String checkId = result.path("check_id").asText();
                    Integer line = result.path("start").path("line").asInt();
                    String message = result.path("extra").path("message").asText(null);
                    String severityRaw = result.path("extra").path("severity").asText(null);

                    KnowledgeRule matchedRule = KnowledgeRule.findByCheckIdAndFramework(checkId, frameworkId);

                    ScanResult scanResult = new ScanResult();
                    scanResult.semgrepCheckId = checkId;
                    scanResult.matchedLine = line;
                    scanResult.message = message;
                    scanResult.severityRaw = severityRaw;
                    scanResult.matchedRuleId = matchedRule != null ? matchedRule.id : null;
                    resultsToSave.add(scanResult);

                    findings.add(new ScanResponse.Finding(
                            checkId, line, severityRaw,
                            matchedRule != null,
                            matchedRule != null ? matchedRule.recommendationText : null,
                            matchedRule != null ? matchedRule.referenceUrl : null,
                            matchedRule == null ? message : null
                    ));
                }
            }

            // simpan submission + results + unmatched_queue + update status dalam 1 transaksi
            String suggestedStatus = determineSuggestedStatus(submission.scanStatus, findings);
            persistResults(submission, resultsToSave, item, suggestedStatus);

            ScanResponse response = new ScanResponse();
            response.scanStatus = submission.scanStatus;
            response.suggestedStatus = suggestedStatus;
            response.findings = findings;
            return response;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            // file sementara HARUS selalu dihapus, apapun hasilnya (sukses/gagal/timeout)
            fileValidationService.cleanup(stored.path());
        }
    }

    /**
     * PENTING: kalau scan gagal/timeout, findings pasti kosong — tapi itu BUKAN berarti
     * kode aman (VALID). Status harus tetap NA supaya tidak menyesatkan developer/reviewer.
     */
    private String determineSuggestedStatus(String scanStatus, List<ScanResponse.Finding> findings) {
        if (!"SUCCESS".equals(scanStatus)) {
            return "NA";
        }
        return findings.isEmpty() ? "VALID" : "INVALID";
    }

    private String runScanSafely(Path filePath, ScanSubmission submission) {
        try {
            String json = semgrepService.scan(filePath);
            submission.scanStatus = "SUCCESS";
            return json;
        } catch (SemgrepService.SemgrepTimeoutException e) {
            submission.scanStatus = "TIMEOUT";
            return null;
        } catch (Exception e) {
            submission.scanStatus = "FAILED";
            return null;
        }
    }

    private void persistResults(ScanSubmission submission, List<ScanResult> results,
                                ApplicationChecklistItem item, String suggestedStatus) {
        QuarkusTransaction.requiringNew().run(() -> {
            submission.persist();

            for (ScanResult r : results) {
                r.submissionId = submission.id;
                r.persist();

                if (r.matchedRuleId == null) {
                    UnmatchedQueue uq = new UnmatchedQueue();
                    uq.scanResultId = r.id;
                    uq.persist();
                }
            }

            // update saran status + keterangan di application_checklist_items
            // (developer/reviewer masih bisa edit manual setelah ini)
            item.status = suggestedStatus;
            item.keterangan = buildKeteranganSummary(submission.scanStatus, results);
            item.persist();
        });
    }

    private String buildKeteranganSummary(String scanStatus, List<ScanResult> results) {
        if ("TIMEOUT".equals(scanStatus)) {
            return "Scan otomatis timeout — silakan coba lagi atau cek manual.";
        }
        if ("FAILED".equals(scanStatus)) {
            return "Scan otomatis gagal dijalankan — silakan coba lagi atau cek manual.";
        }
        if (results.isEmpty()) {
            return "Tidak ditemukan temuan dari hasil scan otomatis.";
        }
        StringBuilder sb = new StringBuilder("Ditemukan " + results.size() + " temuan dari hasil scan otomatis:\n");
        for (ScanResult r : results) {
            sb.append("- ").append(r.semgrepCheckId)
                    .append(" (baris ").append(r.matchedLine).append(")\n");
        }
        return sb.toString();
    }
}