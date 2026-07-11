package akbar.rizki.expertsystem.service;

import akbar.rizki.expertsystem.dto.ChecklistItemUpdateRequest;
import akbar.rizki.expertsystem.entity.ApplicationChecklistItem;
import akbar.rizki.expertsystem.entity.ChecklistMaster;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ChecklistService {

    @Inject
    ApplicationService applicationService;

    @Inject
    CurrentUser currentUser;

    /**
     * Generate checklist item untuk 1 aplikasi dari semua ChecklistMaster aktif.
     * Idempoten: item yang sudah ada (per checklist_master) tidak diduplikasi.
     */
    @Transactional
    public List<ApplicationChecklistItem> generateForApplication(Long applicationId) {
        applicationService.getOwned(applicationId); // otorisasi + memastikan aplikasi ada

        Set<Long> existingMasterIds = ApplicationChecklistItem.<ApplicationChecklistItem>list("applicationId", applicationId)
                .stream()
                .filter(i -> i.checklistMaster != null)
                .map(i -> i.checklistMaster.id)
                .collect(Collectors.toSet());

        List<ChecklistMaster> activeMasters = ChecklistMaster.list("isActive", true);

        for (ChecklistMaster master : activeMasters) {
            if (existingMasterIds.contains(master.id)) {
                continue;
            }
            ApplicationChecklistItem item = new ApplicationChecklistItem();
            item.applicationId = applicationId;
            item.checklistMaster = master;
            item.status = "NA";
            item.updatedBy = currentUser.id();
            item.updatedAt = OffsetDateTime.now();
            item.persist();
        }

        return ApplicationChecklistItem.list("applicationId", applicationId);
    }

    public List<ApplicationChecklistItem> listForApplication(Long applicationId) {
        applicationService.getOwned(applicationId); // otorisasi
        return ApplicationChecklistItem.list("applicationId", applicationId);
    }

    @Transactional
    public ApplicationChecklistItem updateItem(Long itemId, ChecklistItemUpdateRequest request) {
        ApplicationChecklistItem item = ApplicationChecklistItem.findById(itemId);
        if (item == null) {
            throw new NotFoundException("Checklist item tidak ditemukan: " + itemId);
        }
        applicationService.getOwned(item.applicationId); // otorisasi via aplikasi induk

        if (request.status != null) {
            item.status = request.status;
        }
        if (request.keterangan != null) {
            item.keterangan = request.keterangan;
        }
        item.updatedBy = currentUser.id();
        item.updatedAt = OffsetDateTime.now();
        item.persist();
        return item;
    }
}
