package tw.dfder.ccts.entity;

public enum ERRORCODEEnum {
    PROVIDER_ERROR("provider not match CCTS document"),
    CONSUMER_ERROR("Consumer not match CCTS document"),
    TESTCASEID_ERROR("testCaseId not match CCTS document"),
    PATH_TESTCASE_NOT_FOUND_IN_CONTRACT("the testCaseId is not found in contract");

    private final String infoMessage;

    ERRORCODEEnum(String infoMessage) {
        this.infoMessage = infoMessage;
    }


    public String getInfoMessage() {
        return infoMessage;
    }
}
