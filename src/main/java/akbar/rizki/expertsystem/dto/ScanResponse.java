package akbar.rizki.expertsystem.dto;

import java.util.List;

public class ScanResponse {

    public String scanStatus;        // "SUCCESS" | "FAILED" | "TIMEOUT"
    public String suggestedStatus;   // "VALID" | "INVALID" | "NA" (NA kalau scan gagal/timeout)
    public List<Finding> findings;

    public static class Finding {
        public String semgrepCheckId;
        public Integer line;
        public String severityRaw;
        public boolean matchedInKnowledgeBase;
        public String recommendationText;   // null kalau unmatched
        public String referenceUrl;         // null kalau unmatched
        public String rawMessage;           // fallback pesan asli dari Semgrep kalau unmatched

        public Finding(String semgrepCheckId, Integer line, String severityRaw,
                       boolean matchedInKnowledgeBase, String recommendationText,
                       String referenceUrl, String rawMessage) {
            this.semgrepCheckId = semgrepCheckId;
            this.line = line;
            this.severityRaw = severityRaw;
            this.matchedInKnowledgeBase = matchedInKnowledgeBase;
            this.recommendationText = recommendationText;
            this.referenceUrl = referenceUrl;
            this.rawMessage = rawMessage;
        }
    }
}