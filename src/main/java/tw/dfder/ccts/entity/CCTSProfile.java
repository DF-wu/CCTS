package tw.dfder.ccts.entity;


import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document("CCTSProfile")
public class CCTSProfile {
    private String version;
    private String Title;
    private SimpleState startState;
    private HashMap<String, SimpleState> states;




    /*
    below for getter && setter
     */

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public SimpleState getStartState() {
        return startState;
    }

    public void setStartState(SimpleState startState) {
        this.startState = startState;
    }

    public HashMap<String, SimpleState> getStates() {
        return states;
    }

    public void setStates(HashMap<String, SimpleState> states) {
        this.states = states;
    }
}
