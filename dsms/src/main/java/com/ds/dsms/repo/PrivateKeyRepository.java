package com.ds.dsms.repo;

import com.ds.dsms.dss.keystore.KeyStoreParams;
import com.ds.dsms.dss.keystore.PrivateKeyParams;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateKeyRepository extends JpaRepository<PrivateKeyParams, String> {
}
