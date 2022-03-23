package tw.dfder.ccts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import tw.dfder.ccts.entity.CCTSProfile;
import tw.dfder.ccts.services.CCTSProfileParser;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class Controller {

    CCTSProfileParser CCTSProfileParser;

    @Autowired
    public Controller(CCTSProfileParser CCTSProfileParser) {
        this.CCTSProfileParser = CCTSProfileParser;
    }

    @PostConstruct
    public void testMethod() throws IOException {
        CCTSProfile cctsProfile = CCTSProfileParser.parseYaml();

//        System.out.println(profile.get("States"));

    }

}
