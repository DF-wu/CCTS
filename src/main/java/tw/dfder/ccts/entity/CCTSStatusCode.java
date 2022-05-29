package tw.dfder.ccts.entity;

public enum CCTSStatusCode {
    // 0~99
    ALLGREEN("All conditions passed.", 1),
    UNDONE("Not yet done.", 2),

    // 100~199
    CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR("Some properties cause error while parsing CCTS document. Please check the CCTS Document.", 100),
    CCTSDOCUMENT_STATE_NAME_NOT_FOUND("some state name may not correspond to CCTS document", 101),
    CCTSDOCUMENT_PARSE_ERROR("unknown error occur while parsing CCTS document", 102),
    CCTSDOCUMENT_DUPLICATED_TITLE_ERROR("Some documents title is duplicated",   103),
    DOCUMENT_DUPLICATED_STATE_NAME_ERROR("There are duplicated state name in CCTS document", 104),
    CCTSDOCUMENT_ERROR_STATE_NOT_REACHABLE("some state is not reachable", 105),
    PATH_NOT_CONNECTED("Delivery are not connected. check connection of provider and consumer",301),
    PATH_TIMESEQUENCELABEL_NOT_INCREASED("some path's sequence label is not increased",   302),
    PATH_TIMESEQUENCE_LABEL_NOT_UNIQUE("some path's sequence label is not unique",   303),

    // 200~299,
    NO_VALID_PATH_FOUND("There is no valid path", 201),
    DELIVERY_NOT_INCLUDED_BY_ANY_PATH("There is delivery not included by any path", 202),
    CIRCULATED_PATH_FOUND("There is a circulated path. Please Check CCTS Document.", 203),


    // 300~399,
//    ERROR_PARTICIPANT("provider or consumer not match CCTS document", 304),

    CONTRACT_RETRIEVE_FAILED("Fail to retrieve contract from pact broker, please check the testCaseId or related setting.",301),



    // 400~499,
    ERROR_NO_EVENT_FOUND("There is no event between the provider and consumer",  400),
    ERROR_NO_MATCH_TESTCASEID_IN_EVENTLOGS("There is no correspond testCaseId in eventlog for this delivery.",   401),
    NOT_AT_LEAST_A_VALID_EVENTLOG_COMPOSITION_PATH_FOUND("can't not find a valid path through eventlogs aspect path", 402),
    PATH_NOT_EVENTLOG_FOUND("There is no corresponded eventlog found for this path", 403),

    // 500~599,

    DELIVERY_TESTCASEID_NOT_FOUND_IN_CONTRACT("the testCaseId is not found in contract", 501),


    // 600~699
    CONTREACT_TEST_RESULT_NOT_PASS("The service's contract test hasn't done fully", 600);




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
