package tw.dfder.ccts.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tw.dfder.ccts.entity.cctsdocumentmodel.CCTSDocument;

@Repository
public interface CCTSDocumentRepository extends MongoRepository<CCTSDocument, String> {

}
