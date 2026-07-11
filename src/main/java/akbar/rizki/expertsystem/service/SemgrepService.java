package akbar.rizki.expertsystem.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SemgrepService {

    // ruleset DIPIN, bukan --config auto — supaya hasil scan reproducible dan
    // bisa dipetakan manual ke checklist_master lewat knowledge_rules
    private static final String[] RULESETS = {"p/owasp-top-ten", "p/security-audit"};
    private static final long TIMEOUT_SECONDS = 30;

    public static class SemgrepTimeoutException extends RuntimeException {
        public SemgrepTimeoutException(String message) { super(message); }
    }

    public static class SemgrepExecutionException extends RuntimeException {
        public SemgrepExecutionException(String message, Throwable cause) { super(message, cause); }
    }

    /**
     * Jalankan Semgrep terhadap 1 file, kembalikan output JSON mentah.
     * Melempar SemgrepTimeoutException kalau proses melebihi TIMEOUT_SECONDS.
     */
    public String scan(Path filePath) {
        ProcessBuilder pb = new ProcessBuilder(buildCommand(filePath));
        pb.redirectErrorStream(false);

        Process process = null;
        try {
            process = pb.start();

            // baca stdout SEBELUM waitFor() untuk menghindari pipe buffer penuh & deadlock
            String json = new String(process.getInputStream().readAllBytes());

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new SemgrepTimeoutException(
                        "Semgrep tidak selesai dalam " + TIMEOUT_SECONDS + " detik untuk file: " + filePath.getFileName()
                );
            }

            return json;

        } catch (IOException e) {
            throw new SemgrepExecutionException("Gagal menjalankan proses Semgrep", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SemgrepExecutionException("Proses Semgrep terinterupsi", e);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private String[] buildCommand(Path filePath) {
        String[] base = {"semgrep", "scan", "--json", "--quiet"};
        String[] cmd = new String[base.length + RULESETS.length * 2 + 1];
        int i = 0;
        for (String b : base) cmd[i++] = b;
        for (String ruleset : RULESETS) {
            cmd[i++] = "--config";
            cmd[i++] = ruleset;
        }
        cmd[i] = filePath.toString();
        return cmd;
    }
}