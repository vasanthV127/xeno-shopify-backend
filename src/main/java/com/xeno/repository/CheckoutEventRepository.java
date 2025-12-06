package com.xeno.repository;

import com.xeno.model.CheckoutEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckoutEventRepository extends JpaRepository<CheckoutEvent, Long> {
    List<CheckoutEvent> findByTenantId(String tenantId);
    List<CheckoutEvent> findByTenantIdAndAbandoned(String tenantId, Boolean abandoned);
    List<CheckoutEvent> findByTenantIdAndCompleted(String tenantId, Boolean completed);
}
