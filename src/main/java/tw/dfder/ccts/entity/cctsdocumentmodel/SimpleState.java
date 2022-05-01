package tw.dfder.ccts.entity.cctsdocumentmodel;


import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;

@Document(collection = "SimpleState")
public class SimpleState {

    private String stateName;
    private String comment;
    private boolean end;
    private NextState nextState;
    private ArrayList<NextState> options;


    /*
    below for getter && setter
     */


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public NextState getNextState() {
        return nextState;
    }

    public void setNextState(NextState nextState) {
        this.nextState = nextState;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public ArrayList<NextState> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<NextState> options) {
        this.options = options;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}
