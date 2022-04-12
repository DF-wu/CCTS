package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.CCTSModel.CCTSDocument;
import tw.dfder.ccts.entity.CCTSModel.NextState;
import tw.dfder.ccts.entity.CCTSModel.SimpleState;
import tw.dfder.ccts.repository.CCTSDocumentRepository;

import java.util.ArrayList;
import java.util.Map;


@Service
public class CCTSDocumentParser {
    private ServiceConfigure serviceConfigure;
    private CCTSDocumentRepository repo;
    @Autowired
    public CCTSDocumentParser(ServiceConfigure serviceConfigure, CCTSDocumentRepository repo) {
        this.serviceConfigure = serviceConfigure;
        this.repo = repo;
    }

    private CCTSDocument parse2CCTSProfile(Resource fileResource) {
        Yaml yaml = new Yaml(new Constructor(CCTSDocument.class));
        Map<String, Object> profileProperties = null;

        CCTSDocument c = null;
        try {
            c = yaml.load(fileResource.getInputStream());

        }catch (Exception e ){
            System.out.println("CCTS profile parse error");
            System.out.println(e);

        }


        return c;

    }

    public ArrayList<NextState> findPathList(CCTSDocument cctsDocument){
        ArrayList<NextState> pathList = new ArrayList<NextState>();
        for (String k : cctsDocument.getStates().keySet()) {
            SimpleState simpleState = cctsDocument.getStates().get(k);
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


    public void parseAllCCTSProfilesAndSave2DB(){
        ArrayList<CCTSDocument> cctsDocuments = new ArrayList<>();
        for (Resource r : serviceConfigure.cctsFiles) {
            cctsDocuments.add(parse2CCTSProfile(r));
        }
        repo.saveAll(cctsDocuments);
    }
}
