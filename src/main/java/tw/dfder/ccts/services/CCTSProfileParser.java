package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import tw.dfder.ccts.configuration.CCTSConfigure;
import tw.dfder.ccts.entity.CCTSProfile;

import java.util.Map;


@Service
public class CCTSProfileParser {
    private  CCTSConfigure cctsConfigure;

    @Autowired
    public CCTSProfileParser(CCTSConfigure cctsConfigure) {

        this.cctsConfigure = cctsConfigure;
    }

    public Map<String, Object> parseYaml() {
        Yaml yaml = new Yaml();
        Map<String, Object> profileProperties = null;
        try {
            profileProperties = yaml.load(cctsConfigure.cctsFile.getInputStream());
        }catch (Exception e ){
            System.out.println("CCTS profile not found or format not legal");
            System.out.println(e);

        }
        return profileProperties;

    }

    public CCTSProfile convert2CCTSProfile(Map<String, Object> yml){
        CCTSProfile cctsFile = new CCTSProfile();

        return cctsFile;
    }

}
