package surveilance.fish.publisher.audit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import surveilance.fish.model.DataBrick;
import surveilance.fish.model.ViewerData;
import surveilance.fish.persistence.api.DataAccessor;
import surveilance.fish.publisher.AuthCookieUpdater;
import surveilance.fish.publisher.base.BaseConsumer;
import surveilance.fish.security.AesDecrypter;
import surveilance.fish.security.RsaDecrypter;

public class ViewerDataConsumer extends BaseConsumer<List<ViewerData>> {

    public static final String PROP_DATA_PRODUCER_URL = "data.producer.url";
    public static final String PROP_DATA_PRODUCER_GET_DATA_DELAY = "data.producer.get.data.delay";
    
    private final DataAccessor<AuditData> auditDataAccessor;
    
    private final AuditDataMapper auditDataMapper;
    
    public ViewerDataConsumer(Map<String, String> properties, AesDecrypter aesDecrypter, RsaDecrypter rsaDecrypter, DataAccessor<AuditData> dataAccessor, AuthCookieUpdater authCookieUpdater) {
        super(properties, aesDecrypter, rsaDecrypter, authCookieUpdater);
        this.auditDataAccessor = dataAccessor;
        
        auditDataMapper = new AuditDataMapper();

        System.out.println("Viewer data consumer is saving data to: " + properties.get("persist.data.folder.path"));
    }

    @Override
    protected String getRepeatTaskDelay(Map<String, String> properties) {
        return properties.get(PROP_DATA_PRODUCER_GET_DATA_DELAY);
    }

    @Override
    protected String getDataProducerUrl(Map<String, String> properties) {
        return properties.get(PROP_DATA_PRODUCER_URL);
    }
    
    @Override
    protected void doWork() throws IOException {
        DataBrick<List<ViewerData>> dataBrick = retrieveData();
        if (EMPTY_DATA_BRICK == dataBrick) {
            return;
        }

        List<ViewerData> listViewerData = decryptPayload(dataBrick, new TypeReference<List<ViewerData>>() {});
        for (ViewerData currentViewerData : listViewerData) {
            auditDataAccessor.saveData(auditDataMapper.map(currentViewerData));
        }
    }
}
