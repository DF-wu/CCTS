package tw.dfder.ccts.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tw.dfder.ccts.entity.EventLog;

@Repository
public interface EventLogRepository extends MongoRepository<EventLog, String> {

}
