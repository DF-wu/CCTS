package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import tw.dfder.ccts.configuration.CCTSConfigure;
import tw.dfder.ccts.entity.CCTSProfile;
import tw.dfder.ccts.entity.SimpleState;

import java.util.HashMap;
import java.util.Map;


@Service
public class CCTSProfileParser {
    private  CCTSConfigure cctsConfigure;

    @Autowired
    public CCTSProfileParser(CCTSConfigure cctsConfigure) {

        this.cctsConfigure = cctsConfigure;
    }

    public CCTSProfile parseYaml() {
        Yaml yaml = new Yaml(new Constructor(CCTSProfile.class));

        Map<String, Object> profileProperties = null;
        CCTSProfile cctsProfile = null;
        try {
            cctsProfile = yaml.load(cctsConfigure.cctsFile.getInputStream());

        }catch (Exception e ){
            System.out.println("CCTS profile parse error");
            System.out.println(e);

        }
        return cctsProfile;

    }

    public CCTSProfile convert2CCTSProfile(Map<String, Object> yml){
        CCTSProfile cctsFile = new CCTSProfile();

        //set basic info
        cctsFile.setStartAt((String) yml.get("StartAt"));
        cctsFile.setTitle((String) yml.get("Title"));
        cctsFile.setCCTSversion((String) yml.get("CCTS"));

        HashMap inputMap = (HashMap) yml.get("States");
        HashMap<String, SimpleState> states = new HashMap<>();
        // set states
        for (int i = 0; i < yml.size(); i++) {
            // current state
            inputMap.get(i);
            // Setting a state
            SimpleState s = new SimpleState();


        }

        return cctsFile;
    }

}
