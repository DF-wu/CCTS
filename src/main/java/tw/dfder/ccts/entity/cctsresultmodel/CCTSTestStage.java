package tw.dfder.ccts.entity.cctsresultmodel;

public enum CCTSTestStage {


    DOCUMENT_STAGE("CCTS Document Verification Stage","A-2", ""+
                    "        + parse to CCTS data model\n"+
                    "        + every document's title name should be unique"+
                    "        + state name should be unique in whole document states\n" +
                    "        + required properties should not be null\n" +
                    "        + nextState name  should be found in Document states set.\n" +
                    "        + timeSequenceLabel should be uniqe in whole document's delivery\n" ),


    PATH_STAGE("Path Construction and Verification Stage","B-2", ""+
                    "        + document是否存在未被包含於可能潛在的path的delivery\n" +
                    "        + no valid path found error\n" +
                    "        + delivery連結的service是否合理(last consumer should be next provider)\n" +
                    "        + time sequenceLabel should be increased for each path that  found in CCTS document\n"),
    CONTRACT_STAGE("Contract Retrieval Stage","B-3", "" +
            "        + delivery testCaseId not found in Conctract\n" ),

    CONTRACT_TEST_STAGE("Service Verification Stage","B-5", "" +
            "        + service hasn't not passed contract test\n" ),

    EVENTLOG_STAGE("EventLog Verification Stage","B-1",""+
            "        + no eventlog found between provider and consumer for the delivery. \n" +
            "        + delivery testCaseId not found in eventlog.\n"),

    PATH_VERIFY_STAGE("Path Verification Stage","B-6",""+
            "        + eventlog produce time should follow the sequence, at least a valid eventlog path found.\n");



    private final String stageName;
    private final String stageId;
    private final String instruction;

    CCTSTestStage(String s, String s1, String instruction) {
        stageName = s;
        stageId = s1;
        this.instruction = instruction;
    }


    public String getStageName() {
        return stageName;
    }

    public String getStageId() {
        return stageId;
    }

    public String getInstruction() {
        return instruction;
    }
}
