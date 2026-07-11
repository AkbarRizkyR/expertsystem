package akbar.rizki.expertsystem.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class FileValidationService {

    // whitelist ekstensi yang didukung Tahap 1 — sesuaikan kalau nambah bahasa lain
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("java", "js", "vue", "ts");
    private static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024; // 2 MB, cukup untuk 1 file kode

    private static final Path UPLOAD_DIR = Path.of("/tmp/uploads");

    /**
     * Validasi file upload (ekstensi, ukuran) lalu simpan dengan nama acak (UUID)
     * di direktori sementara. TIDAK pernah pakai nama asli dari user sebagai nama file
     * di server — mencegah path traversal dan overwrite antar-user.
     *
     * @return path file yang tersimpan di server, beserta ekstensi aslinya
     */
    public StoredFile validateAndStore(FileUpload upload) throws IOException {
        if (upload == null || upload.fileName() == null || upload.fileName().isBlank()) {
            throw new BadRequestException("File tidak boleh kosong");
        }

        String originalFilename = upload.fileName();
        String extension = extractExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BadRequestException(
                    "Ekstensi file '." + extension + "' tidak didukung. Ekstensi yang didukung: " + ALLOWED_EXTENSIONS
            );
        }

        long size = Files.size(upload.uploadedFile());
        if (size > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Ukuran file melebihi batas maksimum 2 MB");
        }

        Files.createDirectories(UPLOAD_DIR);

        String storedFilename = UUID.randomUUID() + "." + extension;
        Path target = UPLOAD_DIR.resolve(storedFilename);

        // resolve() + normalize() lalu pastikan hasilnya tetap di dalam UPLOAD_DIR (defense in depth)
        Path normalized = target.normalize();
        if (!normalized.startsWith(UPLOAD_DIR)) {
            throw new BadRequestException("Nama file tidak valid");
        }

        Files.copy(upload.uploadedFile(), normalized, StandardCopyOption.REPLACE_EXISTING);

        return new StoredFile(originalFilename, storedFilename, extension, normalized);
    }

    /** Hapus file sementara. Dipanggil selalu setelah scan selesai, baik sukses maupun gagal. */
    public void cleanup(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // gagal hapus tidak boleh menggagalkan response ke user — cukup dicatat
            // TODO: ganti dengan logger project (mis. org.jboss.logging.Logger)
            System.err.println("Gagal menghapus file sementara: " + filePath + " — " + e.getMessage());
        }
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new BadRequestException("File harus memiliki ekstensi yang valid");
        }
        return filename.substring(dot + 1);
    }

    public record StoredFile(String originalFilename, String storedFilename, String extension, Path path) {}
}