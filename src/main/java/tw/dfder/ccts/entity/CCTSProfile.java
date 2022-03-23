package tw.dfder.ccts.entity;


import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document("CCTSProfile")
public class CCTSProfile {
    private String CCTSversion;
    private String title;
    private String startAt;
    private Map<String, SimpleState> states;




    /*
    below for getter && setter
     */

    public String getCCTSversion() {
        return CCTSversion;
    }

    public void setCCTSversion(String CCTSversion) {
        this.CCTSversion = CCTSversion;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public Map<String, SimpleState> getStates() {
        return states;
    }

    public void setStates(Map<String, SimpleState> states) {
        this.states = states;
    }
}
