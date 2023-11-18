package tw.dfder.ccts.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSResult;
import tw.dfder.ccts.entity.cctsresultmodel.CCTSTest;
@Repository
public interface CCTSTestRepository extends MongoRepository<CCTSTest, String> {
}
