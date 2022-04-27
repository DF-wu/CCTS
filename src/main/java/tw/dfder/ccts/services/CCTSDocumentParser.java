package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.cctsdocumentmodel.SimpleState;
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
            System.out.println("CCTS profile parse error!");
            System.out.println(e);
        }

        return c;
    }


    public ArrayList<NextState> findDeliveryList(CCTSDocument cctsDocument){
        ArrayList<NextState> delieveryList = new ArrayList<NextState>();
        // iterate all possible state
        for (String k : cctsDocument.getStates().keySet()) {
            // specify a simpleState
            SimpleState simpleState = cctsDocument.getStates().get(k);
            switch (stateChecker(simpleState)){
                case "nextState exist" -> {
                    // nextSate exist -> add to path list
                    delieveryList.add(simpleState.getNextState());
                }
                case "options exist" -> {
                    // options exist
                    delieveryList.addAll(simpleState.getOptions().values());
                }
                case "not end state exception" -> {
                    System.out.println("exception!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                case "End State" -> {
                    // passed
                }
            }
        }
        return delieveryList;
    }

    private String stateChecker(SimpleState simpleState){
        if(simpleState.getNextState() != null && simpleState.getOptions() == null){
            return "nextState exist";
        }else if(simpleState.getNextState() == null && simpleState.getOptions() != null){
            return "options exist";
        }else if(!simpleState.isEnd()) {
            return "not end state exception";
        }else{
            return "End State";
        }


    }

    public void parseAllCCTSProfilesAndSave2DB(){
        ArrayList<CCTSDocument> cctsDocuments = new ArrayList<>();
        for (Resource r : serviceConfigure.cctsFiles) {
            cctsDocuments.add(parse2CCTSProfile(r));
        }
        repo.saveAll(cctsDocuments);
    }



}
