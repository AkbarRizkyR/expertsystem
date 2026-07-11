package akbar.rizki.expertsystem.dto;

import akbar.rizki.expertsystem.entity.ApplicationChecklistItem;

import java.time.OffsetDateTime;

public class ChecklistItemResponse {

    public Long id;
    public Long applicationId;
    public Long checklistMasterId;
    public String itemCode;
    public String itemDescription;
    public String severity;
    public String status;
    public String keterangan;
    public Long updatedBy;
    public OffsetDateTime updatedAt;

    public static ChecklistItemResponse from(ApplicationChecklistItem item) {
        ChecklistItemResponse r = new ChecklistItemResponse();
        r.id = item.id;
        r.applicationId = item.applicationId;
        if (item.checklistMaster != null) {
            r.checklistMasterId = item.checklistMaster.id;
            r.itemCode = item.checklistMaster.itemCode;
            r.itemDescription = item.checklistMaster.itemDescription;
            r.severity = item.checklistMaster.severity;
        }
        r.status = item.status;
        r.keterangan = item.keterangan;
        r.updatedBy = item.updatedBy;
        r.updatedAt = item.updatedAt;
        return r;
    }
}
