package tw.dfder.ccts.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;

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
    @Field
    private String contractName;

//    "description" in Pact spec
    @Field
    private String term;

    @Field
    private Boolean isValidated;



    public EventLog(long timeInMillis, String providerName, String consumerName, String contractName, String term) {
        this.timeInMillis = timeInMillis;
        this.providerName = providerName;
        this.consumerName = consumerName;
        this.contractName = contractName;
        this.term = term;
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

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getterm() {
        return term;
    }

    public void setterm(String term) {
        this.term = term;
    }

    public Boolean getValidated() {
        return isValidated;
    }

    public void setValidated(Boolean validated) {
        isValidated = validated;
    }
}
