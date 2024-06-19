package com.ds.dsms.repo;

import com.ds.dsms.auth.model.User;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeyStoreRepository extends JpaRepository<KeyStoreParams, Integer> {
    Boolean existsByKeyStoreNameAndUser(String name, User user);
    KeyStoreParams deleteByKeyStoreNameAndUser(String name, User user);
    Optional<KeyStoreParams> findByKeyStoreNameAndUser(String id, User user);
    List<KeyStoreParams> findByUser(User user);
}
