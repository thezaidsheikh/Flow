package com.project.flow.credential.repository;

import com.project.flow.credential.domain.Credential;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Credential entity persistence.
 */
@Repository
public interface CredentialRepository extends JpaRepository<Credential, String> {
    
    /**
     * Find all credentials belonging to a user.
     *
     * @param userId the ID of the user owning the credentials
     * @return list of credentials
     */
    List<Credential> findAllByUserId(String userId);

    /**
     * Find a specific credential by ID and verify ownership.
     *
     * @param id the credential ID
     * @param userId the owner user ID
     * @return optional containing credential if found and owned, empty otherwise
     */
    Optional<Credential> findByIdAndUserId(String id, String userId);
}
