package tw.dfder.ccts.entity;

public enum ERRORCODEEnum {
    PROVIDER_ERROR("provider not match CCTS document"),
    CONSUMER_ERROR("Consumer not match CCTS document"),
    TESTCASEID_ERROR("testCaseId not match CCTS document");

    private final String infoMessage;

    ERRORCODEEnum(String infoMessage) {
        this.infoMessage = infoMessage;
    }
}
