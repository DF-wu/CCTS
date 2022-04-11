package tw.dfder.ccts.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.repository.CCTSDocumentRepository;
import tw.dfder.ccts.repository.EventLogRepository;

@Service("DBCleaner")
public class DBCleaner {
    private final CCTSDocumentRepository cctsDocumentRepository;
    private final EventLogRepository eventLogRepository;

    @Autowired
    public DBCleaner(CCTSDocumentRepository cctsDocumentRepository, EventLogRepository eventLogRepository) {
        this.cctsDocumentRepository = cctsDocumentRepository;
        this.eventLogRepository = eventLogRepository;
    }

    public void cleanCCTSProfileDB(){
        cctsDocumentRepository.deleteAll();
    }

    public void cleanEventLogDB(){
        eventLogRepository.deleteAll();
    }

}
