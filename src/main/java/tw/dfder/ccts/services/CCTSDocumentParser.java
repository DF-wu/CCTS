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
import tw.dfder.ccts.entity.cctsresultmodel.CCTSDocumentError;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
import tw.dfder.ccts.repository.CCTSDocumentRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;


@Service
public class CCTSDocumentParser {
    private final ServiceConfigure serviceConfigure;
    private final CCTSDocumentRepository repo;

    @Autowired
    public CCTSDocumentParser(ServiceConfigure serviceConfigure, CCTSDocumentRepository repo) {
        this.serviceConfigure = serviceConfigure;
        this.repo = repo;
    }


    public CCTSStatusCode parseAllCCTSProfilesAndSave2DB(CCTSTest cctsTest) {
        boolean isValid = true;
        ArrayList<CCTSDocument> cctsDocuments = new ArrayList<>();

        for (Resource r : serviceConfigure.cctsFiles) {
            // may have exception
            try {
                cctsDocuments.add(parse2CCTSProfile(r));
            }catch (Exception e) {
                isValid = false;
                cctsTest.addDocumentError(new CCTSDocumentError(
                        r.getFilename(),
                        CCTSStatusCode.CCTSDOCUMENT_PARSE_ERROR,
                        e.getStackTrace().toString())
                );
                System.out.println("Document exception: " + r.getFilename());
                e.printStackTrace();
            }
        }

        if (isValid) {
            repo.saveAll(cctsDocuments);
            return CCTSStatusCode.ALLGREEN;
        } else {
            return CCTSStatusCode.CCTSDOCUMENT_PARSE_ERROR;
        }
    }


    private CCTSDocument parse2CCTSProfile(Resource fileResource) throws IOException {
        Yaml yaml = new Yaml(new Constructor(CCTSDocument.class));
        Map<String, Object> profileProperties = null;

        CCTSDocument c = null;

        c = yaml.load(fileResource.getInputStream());
        return c;
    }


    /*
        TODO:
        find path(case sequence) from start state to end state.
     */
    public static CCTSStatusCode pathFinder(CCTSDocument document, SimpleState state, ArrayList<NextState> path, ArrayList<ArrayList<NextState>> finalLists) {
        // nextState and options should be mutually exclusive
        if (state.getNextState() != null && state.getOptions() == null && !state.isEnd()) {
            // nextState is not null, but options is null.
            // should not be end state
            // valid nextState branch
            ArrayList<NextState> newPath = new ArrayList<>(path);
            if(newPath.contains(state.getNextState())){
                // already in path
                // avoid loop
                return CCTSStatusCode.CIRCULATED_PATH_FOUND;
            }else{
                newPath.add(state.getNextState());
                pathFinder(document, document.findSimpleState(state.getNextState().getStateName()), newPath, finalLists);
            }

        } else if (state.getNextState() == null && state.getOptions() != null && !state.isEnd()) {
            // options is not null, but nextState is null
            // should not be end state
            // valid options branch

            for (NextState ns : state.getOptions()) {
                ArrayList<NextState> newPath = new ArrayList<>(path);
                if(newPath.contains(ns)){
                    // already in path
                    // avoid loop
                    return CCTSStatusCode.CIRCULATED_PATH_FOUND;
                }else{
                    newPath.add(ns);
                    SimpleState nxt = document.findSimpleState(ns.getStateName());
                    pathFinder(document, document.findSimpleState(ns.getStateName()), newPath, finalLists);
                }
            }

        } else if (state.getNextState() == null && state.getOptions() == null && state.isEnd()) {
            // final state
            finalLists.add(path);
        } else {
            //WTF
            System.out.println("Error when parsing document path");
            System.out.println("CCTS document states have invalid state!");
            return CCTSStatusCode.NO_VALID_PATH_FOUND;
        }

        return CCTSStatusCode.ALLGREEN;


    }

    /*
        @Deprecated
     */
    private CCTSStatusCode verifyStateNameLogically(CCTSDocument cctsDocument) {
//         NextState should be found in document's states
//        Set<String> keyIndocument = cctsDocument.getStates();
//        HashSet<String> keyInAllNextStates =  new HashSet<>();
//        for (NextState nextState : findDeliveryList(cctsDocument)){
//            keyInAllNextStates.add(nextState.getStateName());
//        }
//        for(String stateKey: keyInAllNextStates){
//            if( !keyIndocument.contains(stateKey)
//                    && !cctsDocument.getStates().get(stateKey).isEnd()){
//                return CCTSStatusCode.CCTSDOCUMENT_ERROR_STATENAME_NOT_FOUND;
//            }
//        }
        return CCTSStatusCode.ALLGREEN;
    }

    /*
        @Deprecated
     */
    private CCTSStatusCode verifySimpleStateProperties(SimpleState simpleState) {


//        if(simpleState.isEnd()){
//            // end state
//            return CCTSStatusCode.ALLGREEN;
//        }else if(simpleState.getNextState() == null && simpleState.getOptions() == null){
//            // no entity
//            return CCTSStatusCode.CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;
//        }else if( simpleState.getNextState() !=null && simpleState.getOptions() !=null ){
//            // both entity exist in the same state
//            return CCTSStatusCode.CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;
//        }else if(simpleState.getNextState() != null && simpleState.getOptions() == null){
//            // nextstate
//            if(simpleState.getNextState().getTestCaseId() == null
//                    || simpleState.getNextState().getStateName() == null
//                    || simpleState.getNextState().getConsumer() == null
//                    || simpleState.getNextState().getProvider() == null){
//                return CCTSStatusCode.CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;
//            }
//        }else{
//            // options
//            for(String key : simpleState.getOptions().keySet()){
//                if(simpleState.getOptions().get(key).getStateName() == null
//                        || simpleState.getOptions().get(key).getTestCaseId() == null
//                        || simpleState.getOptions().get(key).getProvider() == null
//                        || simpleState.getOptions().get(key).getConsumer() == null){
//                    return CCTSStatusCode.CCTSDOCUMENT_REQUIRED_PROPERTIES_NULL_ERROR;
//                }
//            }
//        }
        return CCTSStatusCode.ALLGREEN;
    }


    public static HashSet<String> findAllParticipants(CCTSDocument cctsDocument) {
        HashSet<String> participantsSet = new HashSet<>();
        for (NextState delivery : CCTSDocumentParser.findDeliveryList(cctsDocument)) {
            participantsSet.add(delivery.getProvider());
            participantsSet.add(delivery.getConsumer());
        }
        return participantsSet;
    }

    // To store as key
    public static String path2StringName(ArrayList<NextState> path) {
        String s = "";
        for (NextState nextState : path) {
            s += nextState.getStateName() + " -> ";
        }

        // remove last " -> "
        s = s.substring(0, s.length() - 4);
        return s;
    }


    public static ArrayList<NextState> findDeliveryList(CCTSDocument cctsDocument){
        ArrayList<NextState> delieveryList = new ArrayList<NextState>();
        // iterate all possible state
        for (SimpleState state : cctsDocument.getStates()) {
            // specify a simpleState
            switch (stateChecker(state)){
                case "nextState" -> {
                    // nextSate exist -> add to path list
                    delieveryList.add(state.getNextState());
                }
                case "options" -> {
                    // options exist
                    delieveryList.addAll(state.getOptions());
                }
                case "exception" -> {
                    System.out.println("exception!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
                case "end" -> {
                    // passed
                }
            }
        }
        return delieveryList;
    }

    private static String stateChecker(SimpleState simpleState){

        // nextState and options should be mutually exclusive
        if (simpleState.getNextState() != null && simpleState.getOptions() == null && !simpleState.isEnd()) {
            // nextState is not null, but options is null.
            // should not be end state
            // valid nextState branch
            return "nextState";
        } else if (simpleState.getNextState() == null && simpleState.getOptions() != null && !simpleState.isEnd()) {
            // options is not null, but nextState is null
            // should not be end state
            // valid options branch
            return "options";
        } else if (simpleState.getNextState() == null && simpleState.getOptions() == null && simpleState.isEnd()) {
            // end state
            // should not hava nextState and options
            return "end";
        } else {
            //WTF
            System.out.println("CCTS document states have invalid state!");
            return "exception";
        }

    }



}
