package tw.dfder.ccts.entity.cctsresultmodel;

public enum CCTSTestSubject {

    DOCUMENT_STAGE("Document Stage","A-1", ""+
            "        + every document's title name should be unique\n" +
            "        + state name should be unique in whole document states\n" +
            "        + required properties should not be null\n" +
            "        + nextState name  should be found in Document states set.\n" +
            "        + no valid path found error\n" +
            "        + document是否存在未被包含於可能潛在的path的delivery\n" +
            "        + delivery連結的service是否合理(last consumer should be next provider)\n" +
            "        + timeSequenceLabel should be uniqe in whole document's delivery\n" +
            "        + time sequenceLabel should be increased for each path that CCTS found in document"),

    EVENTLOG_STAGE("EventLog Stage","B-1", "" +
            "        + no eventlog found between provider and consumer for the delivery. \n" +
            "        + delivery testCaseId not found in eventlog"),

    PATH_STAGE("Path Stage","B-2", "" +
            "+ \"難\" eventlog produce time should follow the sequence, at least a valid eventlog path found\n"),

    CONTRACT_STAGE("Contract Stage","B-3", "" +
            "+ delivery testCaseId not found in Conctract\n" ),

    CONTRACT_TEST_STAGE("Contract Test Stage","B-5", "" +
            "+ service hasn't not passed contract test\n" );


    private final String description;
    private final String testCaseId;
    private final String instruction;

    CCTSTestSubject(String s, String s1, String instruction) {
        description = s;
        testCaseId = s1;
        this.instruction = instruction;
    }


    public String getDescription() {
        return description;
    }

    public String getTestCaseId() {
        return testCaseId;
    }
}
