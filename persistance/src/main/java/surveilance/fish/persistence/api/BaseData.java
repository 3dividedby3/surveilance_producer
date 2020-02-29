package surveilance.fish.persistence.api;

public abstract class BaseData {

    private final Long timestampCreated;

    public BaseData(Long timestampCreated) {
        this.timestampCreated = timestampCreated;
    }
    
    /**
     * @return the timestampCreated
     */
    public Long getTimestampCreated() {
        return timestampCreated;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((timestampCreated == null) ? 0 : timestampCreated.hashCode());
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BaseData)) {
            return false;
        }
        BaseData other = (BaseData) obj;
        if (timestampCreated == null) {
            if (other.timestampCreated != null) {
                return false;
            }
        } else if (!timestampCreated.equals(other.timestampCreated)) {
            return false;
        }
        return true;
    }
}
