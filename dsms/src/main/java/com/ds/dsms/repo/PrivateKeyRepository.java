package com.ds.dsms.repo;

import com.ds.dsms.auth.model.User;
import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateKeyRepository extends JpaRepository<PrivateKeyParams, String> {
    Optional<PrivateKeyParams> findByAliasAndKeyStoreParams(String id, KeyStoreParams keyStoreParams);
}
