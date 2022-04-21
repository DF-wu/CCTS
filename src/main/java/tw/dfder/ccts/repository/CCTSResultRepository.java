package tw.dfder.ccts.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;

@Repository
public interface CCTSResultRepository extends MongoRepository<CCTSResult, String> {
}
