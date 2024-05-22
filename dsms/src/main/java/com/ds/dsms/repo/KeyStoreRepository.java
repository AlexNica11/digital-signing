package com.ds.dsms.repo;

import com.ds.dsms.model.KeyStoreStorage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeyStoreRepository extends JpaRepository<KeyStoreStorage, String> {
}
