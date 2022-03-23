package tw.dfder.ccts.entity;


import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document("SimpleState")
public class SimpleState {
    private String comment;
    private Boolean end;
    private NextState nextState;
    private Map<String, NextState> options;


    /*
    below for getter && setter
     */

    public Map<String, NextState> getOptions() {
        return options;
    }

    public void setOptions(Map<String, NextState> options) {
        this.options = options;
    }

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

    public Boolean getEnd() {
        return end;
    }

    public void setEnd(Boolean end) {
        this.end = end;
    }
}
