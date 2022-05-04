package tw.dfder.ccts.entity.cctsdocumentmodel;


import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Document(collection = "CCTSDocuments")
public class CCTSDocument {
    private String CCTSversion;
    private String title;
    private String startAt;

    private ArrayList<SimpleState> states;


    public SimpleState findSimpleState(String stateName) {
        for (SimpleState state : states) {
            if (state.getStateName().equals(stateName)) {
                return state;
            }
        }
        return null;
    }

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

    public void setStates(ArrayList<SimpleState> states) {
        this.states = states;
    }

    public ArrayList<SimpleState> getStates() {
        return states;
    }
}
