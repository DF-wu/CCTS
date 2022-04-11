package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.entity.CCTSModel.CCTSDocument;
import tw.dfder.ccts.entity.CCTSModel.NextState;
import tw.dfder.ccts.entity.ERRORCODEEnum;
import tw.dfder.ccts.entity.EventLog;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.repository.EventLogRepository;

import java.util.ArrayList;

@Service("CCTSVerifier")
public class CCTSVerifier {
    private final CCTSDocumentParser documentParser;
    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;
    private final PactBrokerBusyBox busyBox;
    @Autowired
    public CCTSVerifier(CCTSDocumentParser documentParser, CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository, PactBrokerBusyBox busyBox) {
        this.documentParser = documentParser;
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
        this.busyBox = busyBox;
    }

    public void verifyCCTSProfileSAGAFlow(){
        // retrieve needed data from db to memory for increasing speed
        ArrayList<CCTSDocument> documents = (ArrayList<CCTSDocument>) cctsDocumentRepository.findAll();
        ArrayList<EventLog> eventlogs = (ArrayList<EventLog>) eventLogRepository.findAll();
        ArrayList<ERRORCODEEnum> errors = new ArrayList<>();


        for (CCTSDocument document : documents) {
            for (NextState path : documentParser.findPathList(document)){
                // check if eventlogs match or not
                for (EventLog el : eventlogs) {
                    // verify  provider, consumer , testcaseId
                    if (path.getProvider().equals(el.getProviderName())){
                        // provider match
                        if(path.getConsumer().equals(el.getConsumerName())){
                            // consumer match
                            if(path.getTestCaseId().equals(el.gettestCaseId())){
                                // testCaseId match
                                // all condition match -> path passed

                            }else{
                                // testsCaseId not Match
                            }

                        }else{
                            //consumer not match
                            errors.add(ERRORCODEEnum.CONSUMER_ERROR);
                        }

                    }else {
                        // provider not match
                        errors.add(ERRORCODEEnum.PROVIDER_ERROR);
                    }




                }



            }
        }

    }


}

