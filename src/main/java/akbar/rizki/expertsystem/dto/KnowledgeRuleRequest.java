package akbar.rizki.expertsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class KnowledgeRuleRequest {

    @NotNull(message = "checklistMasterId wajib diisi")
    public Long checklistMasterId;

    @NotNull(message = "frameworkId wajib diisi")
    public Integer frameworkId;

    @NotBlank(message = "semgrepCheckId wajib diisi")
    public String semgrepCheckId;

    @NotBlank(message = "recommendationText wajib diisi")
    public String recommendationText;

    public String severitySource;

    public String referenceUrl;
}
