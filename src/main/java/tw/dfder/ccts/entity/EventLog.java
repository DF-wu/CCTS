package tw.dfder.ccts.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "EventLogs")
public class EventLog {
    @Id
    private String Id;


    // what CCTS test case this message belongs to.
    @Field
    private String CCTSProfileName;

    // when it parsed to this obj
    @Field
    private long timeInMillis;

    @Field
    private String providerName;
    @Field
    private String consumerName;

    //    "description" in Pact spec
    @Field
    private String testCaseId;

    @Field
    private boolean isValidated;



    public EventLog(long timeInMillis, String providerName, String consumerName, String testCaseId) {
        this.timeInMillis = timeInMillis;
        this.providerName = providerName;
        this.consumerName = consumerName;
        this.testCaseId = testCaseId;
    }


    public String getCCTSProfileName() {
        return CCTSProfileName;
    }

    public void setCCTSProfileName(String CCTSProfileName) {
        this.CCTSProfileName = CCTSProfileName;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String gettestCaseId() {
        return testCaseId;
    }

    public void settestCaseId(String term) {
        this.testCaseId = term;
    }

    public boolean getValidated() {
        return isValidated;
    }

    public void setValidated(boolean validated) {
        isValidated = validated;
    }
}
