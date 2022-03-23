package tw.dfder.ccts.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import tw.dfder.ccts.entity.CCTSProfile;
import tw.dfder.ccts.services.CCTSProfileParser;

import javax.annotation.PostConstruct;
import java.io.IOException;

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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        CCTSProfile cctsProfile = CCTSProfileParser.parse2CCTSProfile();

        System.out.println(CCTSProfileParser.findPathList(cctsProfile).size());

//        System.out.println(profile.get("States"));

    }

}
