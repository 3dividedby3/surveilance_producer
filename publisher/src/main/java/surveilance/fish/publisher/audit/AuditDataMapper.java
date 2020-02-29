package surveilance.fish.publisher.audit;

import surveilance.fish.model.ViewerData;

public class AuditDataMapper {

    public AuditData map(ViewerData viewerData) {
        AuditData auditData = new AuditData(viewerData.getTimestamp());
        auditData.setBody(viewerData.getBody());
        auditData.setFullUrl(viewerData.getFullUrl());
        auditData.setHeaders(viewerData.getHeaders());
        
        return auditData;
    }
}
