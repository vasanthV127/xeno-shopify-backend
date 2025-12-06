package com.xeno.repository;

import com.xeno.model.CartEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartEventRepository extends JpaRepository<CartEvent, Long> {
    List<CartEvent> findByTenantId(String tenantId);
    List<CartEvent> findByTenantIdAndIsAbandoned(String tenantId, Boolean isAbandoned);
}
