package tw.dfder.ccts.entity;

public enum CCTSStatusCode {
    // 0~99
    ALLGREEN("All conditions passed.", 1),

    // 100~199
    CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR("Some properties cause error while parsing CCTS document", 100),
    CCTSDOCUMENT_ERROR_STATENAME_NOT_FOUND("some state name may not correspond to CCTS document", 101),
    CCTSDOCUMENT_PARSE_ERROR("unknown error occur while parsing CCTS document", 102),
    CCTSDOCUMENT_DUPLICATED_TITLE_ERROR("Some documents title is duplicated",   103),
    DOCUMENT_DUPLICATED_STATE_NAME_ERROR("There are duplicated state name in CCTS document", 104),

    // 200~299
    NO_VALID_PATH("There is no valid path", 201),
    // 300~399
    CCTSDOCUMENT_ERROR_STATE_NOT_REACHABLE("some state is not reachable", 300),
    DELIVERY_TESTCASEID_NOT_FOUND_IN_CONTRACT("the testCaseId is not found in contract", 301),
    ERROR_NO_MATCH_TESTCASEID_IN_EVENTLOGS("There is no correspond testCaseId in eventlog for this delivery.",   302),
    ERROR_NO_EVENT_FOUND("There is no event between the provider and consumer",  303),
    ERROR_PARTICIPANT("provider or consumer not match CCTS document", 304),
    PATH_NOT_EVENTLOG_FOUND("There is no corresponded eventlog found for this path", 305),
    PATH_SEQUENCE_ERROR("Eventlog produced time are not qualified the sequence", 306),
    PATH_NOT_CONNECTED("nextstate consumer i    s not connected by previous state", 307),
    PATH_SEQUENCELABEL_NOT_UNIQUE("some sequence label is not unique", 308),
    PATH_SEQUENCELABEL_NOT_SEQUENTIAL("some path's sequence label is not sequential",   309),


    // 400~499,
    ERROR_TESTCASEID_NOT_IN_CONTRACT("Contract testCaseId not match CCTS document", 400),

    // 500~599
    CONTREACT_TEST_RESULT_NOT_PASS("The service's contract test hasn't done fully", 500);







    private final String message;
    private final Integer stageCode;

    CCTSStatusCode(String message, Integer stageCode) {
        this.message = message;
        this.stageCode = stageCode;
    }


    public String getMessage() {
        return message;
    }

    public Integer getStageCode() {
        return stageCode;
    }

    public static Integer getStage(CCTSStatusCode status) {
        if (status.getStageCode() < 100) {
            return 0;
        } else if (status.getStageCode() < 200) {
            return 1;
        } else if (status.getStageCode() < 300) {
            return 2;
        } else if (status.getStageCode() < 400) {
            return 3;
        } else if (status.getStageCode() < 500) {
            return 4;
        } else if (status.getStageCode() < 600) {
            return 5;
        }else {
            //WTF
            return Integer.MAX_VALUE;
        }
    }
}
