package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import tw.dfder.ccts.configuration.CCTSConfigure;
import tw.dfder.ccts.entity.CCTSProfile;
import tw.dfder.ccts.entity.NextState;
import tw.dfder.ccts.entity.SimpleState;

import java.util.ArrayList;
import java.util.Map;


@Service
public class CCTSProfileParser {
    private  CCTSConfigure cctsConfigure;

    @Autowired
    public CCTSProfileParser(CCTSConfigure cctsConfigure) {

        this.cctsConfigure = cctsConfigure;
    }

    public CCTSProfile parse2CCTSProfile() {
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

    public ArrayList<NextState> findPathList(CCTSProfile cctsProfile){
        ArrayList<NextState> pathList = new ArrayList<NextState>();
        for (String k : cctsProfile.getStates().keySet()) {
            SimpleState simpleState = cctsProfile.getStates().get(k);
            // next exist
            if(simpleState.getNextState() != null && simpleState.getOptions() == null){
                pathList.add(simpleState.getNextState());
            // options exist
            }else if(simpleState.getNextState() == null && simpleState.getOptions() != null){
                pathList.addAll(simpleState.getOptions().values());
            // not end state exception
            }else if(!simpleState.getEnd()){
                System.out.println("exception!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }

        return pathList;
    }
}