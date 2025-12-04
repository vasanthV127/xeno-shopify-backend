package com.xeno.service;

import com.xeno.model.Tenant;
import com.xeno.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncSchedulerService {

    private final TenantRepository tenantRepository;
    private final ShopifyService shopifyService;

    @Scheduled(cron = "${shopify.sync.cron}")
    public void scheduledSync() {
        log.info("Starting scheduled Shopify data sync for all tenants");
        
        List<Tenant> activeTenants = tenantRepository.findAll().stream()
                .filter(Tenant::getActive)
                .toList();
        
        for (Tenant tenant : activeTenants) {
            try {
                log.info("Syncing data for tenant: {}", tenant.getTenantId());
                shopifyService.syncShopifyData(tenant);
            } catch (Exception e) {
                log.error("Error syncing tenant: {}", tenant.getTenantId(), e);
            }
        }
        
        log.info("Scheduled sync completed for {} tenants", activeTenants.size());
    }
}
