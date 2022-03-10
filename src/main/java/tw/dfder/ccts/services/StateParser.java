package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import tw.dfder.ccts.configuration.CCTSConfigure;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


@Service
public class StateParser {
    InputStream CCTSFileStream;

    @Autowired
    public StateParser() {

    }

    public Map<String, Object> parseYaml() {
        Yaml yaml = new Yaml();
        Map<String, Object> profileProperties = null;
        Resource resource = new ClassPathResource("static/cctsProfile.yaml");
        try {
            profileProperties  = yaml.load(resource.getInputStream());

        }catch (Exception e ){
            System.out.println(e);
            System.out.println("!!!!!!!!!!!!");
        }
        return profileProperties;
    }

}
