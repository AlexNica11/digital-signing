package com.ds.dsms.repo;

import com.ds.dsms.auth.model.User;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeyStoreRepository extends JpaRepository<KeyStoreParams, String> {
    Optional<KeyStoreParams> findByKeyStoreNameAndUser(String id, User user);
}
