package akbar.rizki.expertsystem.service;

import akbar.rizki.expertsystem.dto.KnowledgeRuleRequest;
import akbar.rizki.expertsystem.dto.UnmatchedQueueResponse;
import akbar.rizki.expertsystem.entity.ChecklistMaster;
import akbar.rizki.expertsystem.entity.KnowledgeRule;
import akbar.rizki.expertsystem.entity.ScanResult;
import akbar.rizki.expertsystem.entity.UnmatchedQueue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class ReviewService {

    @Inject
    CurrentUser currentUser;

    // ==================== unmatched_queue ====================

    public List<UnmatchedQueueResponse> listUnmatched(boolean includeReviewed) {
        List<UnmatchedQueue> queue = includeReviewed
                ? UnmatchedQueue.listAll()
                : UnmatchedQueue.list("reviewed", false);

        return queue.stream()
                .map(q -> UnmatchedQueueResponse.from(q, ScanResult.findById(q.scanResultId)))
                .toList();
    }

    @Transactional
    public UnmatchedQueue markReviewed(Long queueId) {
        UnmatchedQueue queue = UnmatchedQueue.findById(queueId);
        if (queue == null) {
            throw new NotFoundException("Item unmatched_queue tidak ditemukan: " + queueId);
        }
        queue.reviewed = true;
        queue.reviewedBy = currentUser.id();
        queue.persist();
        return queue;
    }

    // ==================== knowledge_rules ====================

    public List<KnowledgeRule> listRules() {
        return KnowledgeRule.listAll();
    }

    public KnowledgeRule getRule(Long id) {
        KnowledgeRule rule = KnowledgeRule.findById(id);
        if (rule == null) {
            throw new NotFoundException("Knowledge rule tidak ditemukan: " + id);
        }
        return rule;
    }

    @Transactional
    public KnowledgeRule createRule(KnowledgeRuleRequest request) {
        KnowledgeRule rule = new KnowledgeRule();
        applyRequest(rule, request);
        rule.persist();
        return rule;
    }

    @Transactional
    public KnowledgeRule updateRule(Long id, KnowledgeRuleRequest request) {
        KnowledgeRule rule = getRule(id);
        applyRequest(rule, request);
        rule.persist();
        return rule;
    }

    @Transactional
    public void deleteRule(Long id) {
        KnowledgeRule rule = getRule(id);
        rule.delete();
    }

    /**
     * Buat knowledge rule baru dari item unmatched_queue lalu tandai queue-nya reviewed,
     * semuanya dalam 1 transaksi.
     */
    @Transactional
    public KnowledgeRule createRuleFromUnmatched(Long queueId, KnowledgeRuleRequest request) {
        UnmatchedQueue queue = UnmatchedQueue.findById(queueId);
        if (queue == null) {
            throw new NotFoundException("Item unmatched_queue tidak ditemukan: " + queueId);
        }

        KnowledgeRule rule = new KnowledgeRule();
        applyRequest(rule, request);
        rule.persist();

        queue.reviewed = true;
        queue.reviewedBy = currentUser.id();
        queue.persist();

        return rule;
    }

    private void applyRequest(KnowledgeRule rule, KnowledgeRuleRequest request) {
        ChecklistMaster master = ChecklistMaster.findById(request.checklistMasterId);
        if (master == null) {
            throw new NotFoundException("ChecklistMaster tidak ditemukan: " + request.checklistMasterId);
        }
        rule.checklistMaster = master;
        rule.frameworkId = request.frameworkId;
        rule.semgrepCheckId = request.semgrepCheckId;
        rule.recommendationText = request.recommendationText;
        rule.severitySource = request.severitySource;
        rule.referenceUrl = request.referenceUrl;
    }
}
