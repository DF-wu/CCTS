package tw.dfder.ccts.entity;

public enum CCTSStatusCode {
    ALLGREEN("All conditions passed."),
    ERROR_PARTICIPANT("provider or consumer not match CCTS document"),
    ERROR_TESTCASEID_IN_CONTRACT("Contract testCaseId not match CCTS document"),

    ERROR_NO_MATCH_TESTCASEID_IN_EVENTLOGS("There is no correspond testCaseId for this path."),
    ERROR_NO_EVENT_FOUND("There is no event between the provider and consumer"),
    PATH_TESTCASE_NOT_FOUND_IN_CONTRACT("the testCaseId is not found in contract"),
    CONTREACT_TEST_RESULT_NOT_PASS("The service's contract test hasn't done fully");
    private final String infoMessage;

    CCTSStatusCode(String infoMessage) {
        this.infoMessage = infoMessage;
    }


    public String getInfoMessage() {
        return infoMessage;
    }
}
