package com.ds.dsms.repo;

import com.ds.dsms.model.SignedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
public interface SignedDocumentRepository extends JpaRepository<SignedDocument, String> {

    List<SignedDocument> findAllByCreationDateBefore(Date creationDate);
    List<SignedDocument> findByUsername(String username);
    Optional<SignedDocument> findByJobId(String jobId);
    Integer deleteByJobId(String jobId);
}
