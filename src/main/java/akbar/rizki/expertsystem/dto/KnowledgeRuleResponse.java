package akbar.rizki.expertsystem.dto;

import akbar.rizki.expertsystem.entity.KnowledgeRule;

public class KnowledgeRuleResponse {

    public Long id;
    public Long checklistMasterId;
    public Integer frameworkId;
    public String semgrepCheckId;
    public String recommendationText;
    public String severitySource;
    public String referenceUrl;

    public static KnowledgeRuleResponse from(KnowledgeRule rule) {
        KnowledgeRuleResponse r = new KnowledgeRuleResponse();
        r.id = rule.id;
        r.checklistMasterId = rule.checklistMaster != null ? rule.checklistMaster.id : null;
        r.frameworkId = rule.frameworkId;
        r.semgrepCheckId = rule.semgrepCheckId;
        r.recommendationText = rule.recommendationText;
        r.severitySource = rule.severitySource;
        r.referenceUrl = rule.referenceUrl;
        return r;
    }
}
