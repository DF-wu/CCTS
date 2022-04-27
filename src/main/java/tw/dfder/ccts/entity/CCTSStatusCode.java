package tw.dfder.ccts.entity;

import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;

public enum CCTSStatusCode {
    ALLGREEN("All conditions passed."),
    ERROR_PARTICIPANT("provider or consumer not match CCTS document"),
    ERROR_TESTCASEID_IN_CONTRACT("Contract testCaseId not match CCTS document"),
    ERROR_NO_MATCH_TESTCASEID_IN_EVENTLOGS("There is no correspond testCaseId in eventlog for this delivery."),
    ERROR_NO_EVENT_FOUND("There is no event between the provider and consumer"),
    DELIVERY_TESTCASEID_NOT_FOUND_IN_CONTRACT("the testCaseId is not found in contract"),
    CONTREACT_TEST_RESULT_NOT_PASS("The service's contract test hasn't done fully"),

    // parser error
    CCTSDOCUMENT_ERROR("Some properties cause error while parsing CCTS document"),
    CCTSDOCUMENT_ERROR_STATENAME_NOT_FOUND("some state name may not correspond to CCTS document"),
    CCTSDOCUMENT_ERROR_CASESEQUENCE_NOT_LEGAL("Case sequence is not legal"),

    CASESEQUENCE_ERROR("Eventlog produced time are not quilified the sequence");







    private final String message;

    CCTSStatusCode(String message) {
        this.message = message;
    }


    public String getMessage() {
        return message;
    }
}
