package tw.dfder.ccts.entity.cctsresultmodel;

public class CCTSTestCase {

    private CCTSTestSubject testSubject;
    private Boolean testResult;


    public CCTSTestCase(CCTSTestSubject testSubject, Boolean testResult) {
        this.testSubject = testSubject;
        this.testResult = testResult;
    }


    public CCTSTestSubject getTestSubject() {
        return testSubject;
    }

    public void setTestSubject(CCTSTestSubject testSubject) {
        this.testSubject = testSubject;
    }

    public Boolean getTestResult() {
        return testResult;
    }

    public void setTestResult(Boolean testResult) {
        this.testResult = testResult;
    }
}
