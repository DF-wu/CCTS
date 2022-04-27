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
    private long timeStamp;

    @Field
    private String providerName;
    @Field
    private String consumerName;

    //    "description" in Pact spec
    @Field
    private String testCaseId;

    @Field
    private Integer caseSequenceLabel;




    public EventLog(long timeStamp, String providerName, String consumerName, String testCaseId, Integer caseSequenceLabel) {
        this.timeStamp = timeStamp;
        this.providerName = providerName;
        this.consumerName = consumerName;
        this.testCaseId = testCaseId;
        this.caseSequenceLabel = caseSequenceLabel;
    }


    public String getCCTSProfileName() {
        return CCTSProfileName;
    }

    public void setCCTSProfileName(String CCTSProfileName) {
        this.CCTSProfileName = CCTSProfileName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
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

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String term) {
        this.testCaseId = term;
    }

    public Integer getCaseSequenceLabel() {
        return caseSequenceLabel;
    }

    public void setCaseSequenceLabel(Integer caseSequenceLabel) {
        this.caseSequenceLabel = caseSequenceLabel;
    }
}
