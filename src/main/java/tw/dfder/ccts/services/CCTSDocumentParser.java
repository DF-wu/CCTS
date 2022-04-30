package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import tw.dfder.ccts.configuration.ServiceConfigure;
import tw.dfder.ccts.entity.CCTSStatusCode;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;
import tw.dfder.ccts.entity.cctsdocumentmodel.NextState;
import tw.dfder.ccts.entity.cctsdocumentmodel.SimpleState;
import tw.dfder.ccts.repository.CCTSDocumentRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Service
public class CCTSDocumentParser {
    private ServiceConfigure serviceConfigure;
    private CCTSDocumentRepository repo;
    @Autowired
    public CCTSDocumentParser(ServiceConfigure serviceConfigure, CCTSDocumentRepository repo) {
        this.serviceConfigure = serviceConfigure;
        this.repo = repo;
    }



    public void parseAllCCTSProfilesAndSave2DB(){
        ArrayList<CCTSDocument> cctsDocuments = new ArrayList<>();
        for (Resource r : serviceConfigure.cctsFiles) {
            cctsDocuments.add(parse2CCTSProfile(r));
        }
        repo.saveAll(cctsDocuments);
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



        CCTSStatusCode result = verifyDocumentProperties(c);
        if(result == CCTSStatusCode.ALLGREEN){
            return  c;
        }else {
            // invalid
            System.out.println("!!!parse document error");
            return null;
        }

    }


    /*
        TODO:
        find path(case sequence) from start state to end state.
     */
    public void pathFinder(CCTSDocument cctsDocument, SimpleState state, ArrayList<NextState> path, ArrayList<ArrayList<NextState>> finalLists) {
        if(state.isEnd()){
            // final state
            finalLists.add(path);
        }else if(state.getNextState() != null && state.getOptions() == null){
            // has one next state
            ArrayList<NextState> newPathList = new ArrayList<>(path);
            newPathList.add(state.getNextState());
            pathFinder(cctsDocument, cctsDocument.getStates().get(state.getNextState().getStateName()), newPathList, finalLists);
        }else if(state.getNextState() == null && state.getOptions() != null){
            // options
            for (NextState ns : state.getOptions().values())  {
                ArrayList<NextState> newPathList = new ArrayList<>(path);
                newPathList.add(ns);
                pathFinder(cctsDocument, cctsDocument.getStates().get(ns.getStateName()), newPathList, finalLists);
            }
        }
    }

    public ArrayList<ArrayList<Integer>> caseSequencesParser(CCTSDocument cctsDocument){
        ArrayList<ArrayList<Integer>> caseSequences = new ArrayList<>();
        for (String str : cctsDocument.getCaseSequences()) {
            ArrayList<Integer> caseInt = new ArrayList<>();
            // clean string and split by comma.
            for (String token : str.trim().replace(" ","").split(",")) {
                caseInt.add(Integer.valueOf(token));
            }

            // verify
            for (int i = 1; i < caseInt.size(); i++) {
                // should be increased integer
                if( !(caseInt.get(i) > caseInt.get(i-1)) ){
                    return null;
                }
            }
            caseSequences.add(caseInt);
        }
        return caseSequences;
    }


    private CCTSStatusCode verifyDocumentProperties(CCTSDocument cctsDocument){
        // verify title version startAt states caseSequences are not null
        if(cctsDocument.getTitle() == null
                || cctsDocument.getCCTSversion() == null
                || cctsDocument.getStartAt() == null
                || cctsDocument.getStates() == null
                || cctsDocument.getCaseSequences() == null){
            System.out.println("CCTS document properties are not complete!");
            return CCTSStatusCode.CCTSDOCUMENT_ERROR;
        }


        //
        // logically verify stateName
        // all stateName in nextState should be found in documents states
        CCTSStatusCode stateNameResult = verifyStateNameLogically(cctsDocument);
        if (stateNameResult != CCTSStatusCode.ALLGREEN) return stateNameResult;


        // verify simpleState's properties are not null
        // verify nextState's properties are not null.
        for (String stateKey : cctsDocument.getStates().keySet())  {
            SimpleState simpleState = cctsDocument.getStates().get(stateKey);
            CCTSStatusCode result = verifySimpleStateProperties(simpleState);
            if (result != CCTSStatusCode.ALLGREEN) return result;
        }

        // logically verify CaseSequences .
        for (ArrayList<Integer> caseInt : caseSequencesParser(cctsDocument)){
            if(caseInt == null ){
                return CCTSStatusCode.CCTSDOCUMENT_ERROR_CASESEQUENCE_NOT_LEGAL;
            }
        }

        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode verifyStateNameLogically(CCTSDocument cctsDocument) {
        // NextState should be found in document's states
        Set<String> keyIndocument = cctsDocument.getStates().keySet();
        HashSet<String> keyInAllNextStates =  new HashSet<>();
        for (NextState nextState : findDeliveryList(cctsDocument)){
            keyInAllNextStates.add(nextState.getStateName());
        }
        for(String stateKey: keyInAllNextStates){
            if( !keyIndocument.contains(stateKey)
                    && !cctsDocument.getStates().get(stateKey).isEnd()){
                return CCTSStatusCode.CCTSDOCUMENT_ERROR_STATENAME_NOT_FOUND;
            }
        }
        return CCTSStatusCode.ALLGREEN;
    }

    private CCTSStatusCode verifySimpleStateProperties(SimpleState simpleState) {
        // TODO: 攔截final state

        if(simpleState.isEnd()){
            // end state
            return CCTSStatusCode.ALLGREEN;
        }else if(simpleState.getNextState() == null && simpleState.getOptions() == null){
            // no entity
            return CCTSStatusCode.CCTSDOCUMENT_ERROR;
        }else if( simpleState.getNextState() !=null && simpleState.getOptions() !=null ){
            // both entity exist in the same state
            return CCTSStatusCode.CCTSDOCUMENT_ERROR;
        }else if(simpleState.getNextState() != null && simpleState.getOptions() == null){
            // nextstate
            if(simpleState.getNextState().getTestCaseId() == null
                    || simpleState.getNextState().getStateName() == null
                    || simpleState.getNextState().getConsumer() == null
                    || simpleState.getNextState().getProvider() == null){
                return CCTSStatusCode.CCTSDOCUMENT_ERROR;
            }
        }else{
            // options
            for(String key : simpleState.getOptions().keySet()){
                if(simpleState.getOptions().get(key).getStateName() == null
                        || simpleState.getOptions().get(key).getTestCaseId() == null
                        || simpleState.getOptions().get(key).getProvider() == null
                        || simpleState.getOptions().get(key).getConsumer() == null){
                    return CCTSStatusCode.CCTSDOCUMENT_ERROR;
                }
            }
        }
        return CCTSStatusCode.ALLGREEN;
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



}
