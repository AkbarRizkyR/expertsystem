package akbar.rizki.expertsystem.dto;

import akbar.rizki.expertsystem.entity.ScanResult;
import akbar.rizki.expertsystem.entity.UnmatchedQueue;

public class UnmatchedQueueResponse {

    public Long id;
    public Long scanResultId;
    public boolean reviewed;
    public Long reviewedBy;

    // detail dari scan_result terkait (biar reviewer punya konteks)
    public String semgrepCheckId;
    public Integer matchedLine;
    public String message;
    public String severityRaw;

    public static UnmatchedQueueResponse from(UnmatchedQueue queue, ScanResult result) {
        UnmatchedQueueResponse r = new UnmatchedQueueResponse();
        r.id = queue.id;
        r.scanResultId = queue.scanResultId;
        r.reviewed = queue.reviewed;
        r.reviewedBy = queue.reviewedBy;
        if (result != null) {
            r.semgrepCheckId = result.semgrepCheckId;
            r.matchedLine = result.matchedLine;
            r.message = result.message;
            r.severityRaw = result.severityRaw;
        }
        return r;
    }
}
