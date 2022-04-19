package tw.dfder.ccts.entity;

public enum CCTSStatusCode {
    ALLGREEN("All conditions passed."),
    ERROR_PROVIDER("provider not match CCTS document"),
    ERROR_CONSUMER("Consumer not match CCTS document"),
    ERROR_TESTCASEID("testCaseId not match CCTS document"),
    PATH_TESTCASE_NOT_FOUND_IN_CONTRACT("the testCaseId is not found in contract");

    private final String infoMessage;

    CCTSStatusCode(String infoMessage) {
        this.infoMessage = infoMessage;
    }


    public String getInfoMessage() {
        return infoMessage;
    }
}
