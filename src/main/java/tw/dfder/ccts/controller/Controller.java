package tw.dfder.ccts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import tw.dfder.ccts.services.StateParser;

import javax.annotation.PostConstruct;
import javax.print.attribute.standard.JobKOctets;
import java.io.IOException;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class Controller {

    StateParser stateParser;

    @Autowired
    public Controller(StateParser stateParser) {
        this.stateParser = stateParser;
    }

    @PostConstruct
    public void testMethod() throws IOException {
        Map<String, Object> profile = stateParser.parseYaml();
        System.out.println(profile.get("States"));

    }

}
