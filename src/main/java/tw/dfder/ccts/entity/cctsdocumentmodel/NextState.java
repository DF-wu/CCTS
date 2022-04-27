package tw.dfder.ccts.entity.cctsdocumentmodel;

public class NextState {
    private String stateName;
    private String provider;
    private String consumer;
    private String testCaseId;
    private Integer timeSequenceLabel;





    public String toPrretyString() {
        String s =
                "  stateName: " + stateName + "\n"+
                "  provider: " + provider + "\n"+
                "  consumer: " + consumer+ "\n"+
                "  testCaseId: "+ testCaseId ;
        return s;
    }

    /*
    below for getter && setter
     */

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public Integer getTimeSequenceLabel() {
        return timeSequenceLabel;
    }

    public void setTimeSequenceLabel(Integer timeSequenceLabel) {
        this.timeSequenceLabel = timeSequenceLabel;
    }
}
