package tn.finix.documentaipipelinebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.finix.documentaipipelinebackend.model.Document;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    @Query(value = "SELECT * FROM documents WHERE to_tsvector('english', extracted_text) @@ plainto_tsquery('english', :query)", nativeQuery = true)
    List<Document> searchByText(@Param("query") String query);
}
