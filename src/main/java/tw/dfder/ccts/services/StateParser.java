package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import tw.dfder.ccts.configuration.CCTSConfigure;

import java.io.InputStream;
import java.util.Map;


@Service
public class StateParser {

    @Autowired
    public StateParser() {

    }

    public Map<String, Object> parseYaml() {
        Yaml yaml = new Yaml();
        Map<String, Object> profileProperties = null;
        try {
            profileProperties = yaml.load(CCTSConfigure.cctsFile.getInputStream());
        }catch (Exception e ){
            System.out.println(e);
        }
        return profileProperties;
    }

}
