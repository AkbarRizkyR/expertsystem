package akbar.rizki.expertsystem.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class SemgrepService {

    // ruleset yang dipakai (comma-separated di config). Default "auto" = Semgrep memilih
    // aturan otomatis dari registry sesuai bahasa/framework yang terdeteksi (deteksi paling luas,
    // termasuk aturan Vue). Bisa di-pin ke pack tertentu untuk hasil reproducible, mis:
    //   semgrep.rulesets=p/owasp-top-ten,p/security-audit
    @ConfigProperty(name = "semgrep.rulesets", defaultValue = "auto")
    List<String> rulesets;

    // Semgrep dijalankan lewat Docker (binary tidak jalan native di Windows).
    // File di-mount ke /src di dalam container, lalu di-scan dari sana.
    @ConfigProperty(name = "semgrep.docker.image", defaultValue = "semgrep/semgrep:latest")
    String dockerImage;

    @ConfigProperty(name = "semgrep.timeout.seconds", defaultValue = "120")
    long timeoutSeconds;

    // custom rules kurasi tim (di-bundle di resources). Default MATI karena 'auto' sudah menutupi.
    // Nyalakan (semgrep.custom-rules.enabled=true) kalau ingin id deterministik "rules.*".
    @ConfigProperty(name = "semgrep.custom-rules.enabled", defaultValue = "false")
    boolean customRulesEnabled;

    private static final String CUSTOM_RULES_RESOURCE = "semgrep/custom-rules.yml";
    private volatile Path customRulesDir;

    public static class SemgrepTimeoutException extends RuntimeException {
        public SemgrepTimeoutException(String message) { super(message); }
    }

    public static class SemgrepExecutionException extends RuntimeException {
        public SemgrepExecutionException(String message, Throwable cause) { super(message, cause); }
        public SemgrepExecutionException(String message) { super(message); }
    }

    /**
     * Jalankan Semgrep terhadap 1 file, kembalikan output JSON mentah.
     * Melempar SemgrepTimeoutException kalau proses melebihi timeoutSeconds.
     */
    public String scan(Path filePath) {
        Path rulesDir = null;
        if (customRulesEnabled) {
            try {
                rulesDir = ensureCustomRulesDir();
            } catch (IOException e) {
                throw new SemgrepExecutionException("Gagal menyiapkan custom rules Semgrep", e);
            }
        }

        ProcessBuilder pb = new ProcessBuilder(buildCommand(filePath, rulesDir));
        pb.redirectErrorStream(false);

        Process process = null;
        try {
            process = pb.start();

            // stderr dibaca di thread terpisah supaya tidak deadlock dengan stdout
            StreamCollector errCollector = new StreamCollector(process.getErrorStream());
            Thread errThread = new Thread(errCollector);
            errThread.start();

            // baca stdout di thread ini SEBELUM waitFor() untuk menghindari pipe buffer penuh
            String json = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new SemgrepTimeoutException(
                        "Semgrep tidak selesai dalam " + timeoutSeconds + " detik untuk file: " + filePath.getFileName()
                );
            }

            errThread.join(TimeUnit.SECONDS.toMillis(5));
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String stderr = errCollector.getContent();
                throw new SemgrepExecutionException(
                        "Semgrep keluar dengan exit code " + exitCode + ": "
                                + (stderr.isBlank() ? "(tidak ada pesan stderr)" : stderr.strip())
                );
            }

            return json;

        } catch (IOException e) {
            throw new SemgrepExecutionException("Gagal menjalankan proses Semgrep (cek Docker & image '" + dockerImage + "')", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SemgrepExecutionException("Proses Semgrep terinterupsi", e);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * Bangun perintah:
     * docker run --rm -v &lt;dirFile&gt;:/src [-v &lt;dirRules&gt;:/rules] &lt;image&gt;
     *   semgrep scan --json --quiet --config ... [--config /rules/custom-rules.yml] /src/&lt;namaFile&gt;
     */
    private String[] buildCommand(Path filePath, Path rulesDir) {
        Path abs = filePath.toAbsolutePath();
        String hostDir = abs.getParent().toString();
        String fileName = abs.getFileName().toString();

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-v");
        cmd.add(hostDir + ":/src");
        if (rulesDir != null) {
            cmd.add("-v");
            cmd.add(rulesDir.toAbsolutePath() + ":/rules");
        }
        cmd.add(dockerImage);
        cmd.add("semgrep");
        cmd.add("scan");
        cmd.add("--json");
        cmd.add("--quiet");
        for (String ruleset : rulesets) {
            cmd.add("--config");
            cmd.add(ruleset.trim());
        }
        if (rulesDir != null) {
            cmd.add("--config");
            cmd.add("/rules/custom-rules.yml");
        }
        cmd.add("/src/" + fileName);
        return cmd.toArray(new String[0]);
    }

    /**
     * Ekstrak custom-rules.yml dari classpath ke folder temp di disk (sekali saja),
     * supaya bisa di-mount ke container. Mengembalikan folder yang berisi custom-rules.yml.
     */
    private Path ensureCustomRulesDir() throws IOException {
        if (customRulesDir != null) {
            return customRulesDir;
        }
        synchronized (this) {
            if (customRulesDir != null) {
                return customRulesDir;
            }
            Path dir = Path.of(System.getProperty("java.io.tmpdir"), "expertsystem-semgrep-rules");
            Files.createDirectories(dir);
            Path target = dir.resolve("custom-rules.yml");
            try (InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(CUSTOM_RULES_RESOURCE)) {
                if (in == null) {
                    throw new IOException("Resource tidak ditemukan di classpath: " + CUSTOM_RULES_RESOURCE);
                }
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            customRulesDir = dir;
            return dir;
        }
    }

    /** Pengumpul isi stream (untuk stderr) agar bisa dibaca setelah proses selesai. */
    private static final class StreamCollector implements Runnable {
        private final InputStream in;
        private volatile String content = "";

        StreamCollector(InputStream in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                content = "(gagal membaca stderr: " + e.getMessage() + ")";
            }
        }

        String getContent() {
            return content;
        }
    }
}
