package akbar.rizki.expertsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ApplicationRequest {

    @NotBlank(message = "name wajib diisi")
    @Size(max = 150, message = "name maksimal 150 karakter")
    public String name;

    public String description;
}
