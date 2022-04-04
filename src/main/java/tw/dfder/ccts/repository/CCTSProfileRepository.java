package tw.dfder.ccts.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tw.dfder.ccts.entity.CCTSModel.CCTSProfile;

@Repository
public interface CCTSProfileRepository extends MongoRepository<CCTSProfile, String> {

}
