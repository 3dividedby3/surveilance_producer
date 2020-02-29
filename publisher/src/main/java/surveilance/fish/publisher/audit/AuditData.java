package surveilance.fish.publisher.audit;

import java.util.List;
import java.util.Map;

import surveilance.fish.persistence.api.BaseData;

public class AuditData extends BaseData {
    
    private Map<String, List<String>> headers;
    
    private String fullUrl;
    
    private String body;

    public AuditData(Long timestampCreated) {
        super(timestampCreated);
    }

    /**
     * @return the headers
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * @param headers the headers to set
     */
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * @return the fullUrl
     */
    public String getFullUrl() {
        return fullUrl;
    }

    /**
     * @param fullUrl the fullUrl to set
     */
    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((fullUrl == null) ? 0 : fullUrl.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof AuditData)) {
            return false;
        }
        AuditData other = (AuditData) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        if (fullUrl == null) {
            if (other.fullUrl != null) {
                return false;
            }
        } else if (!fullUrl.equals(other.fullUrl)) {
            return false;
        }
        if (headers == null) {
            if (other.headers != null) {
                return false;
            }
        } else if (!headers.equals(other.headers)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AuditData [headers=" + headers + ", fullUrl=" + fullUrl + ", body=" + body + ", getTimestampCreated()="
                + getTimestampCreated() + "]";
    }
    
}
