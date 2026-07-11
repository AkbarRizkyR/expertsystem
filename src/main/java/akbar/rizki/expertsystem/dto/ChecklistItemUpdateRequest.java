package akbar.rizki.expertsystem.dto;

import jakarta.validation.constraints.Pattern;

public class ChecklistItemUpdateRequest {

    @Pattern(regexp = "NA|VALID|INVALID", message = "status harus salah satu dari: NA, VALID, INVALID")
    public String status;

    public String keterangan;
}
