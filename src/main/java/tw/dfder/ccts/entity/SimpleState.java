package tw.dfder.ccts.entity;


import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document("SimpleState")
public class SimpleState {
    private String stateName;
    private String Comment;
    private NextState nextState;
    private Boolean end;


    /*
    below for getter && setter
     */

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public NextState getNextState() {
        return nextState;
    }

    public void setNextState(NextState nextState) {
        this.nextState = nextState;
    }

    public Boolean getEnd() {
        return end;
    }

    public void setEnd(Boolean end) {
        this.end = end;
    }
}
