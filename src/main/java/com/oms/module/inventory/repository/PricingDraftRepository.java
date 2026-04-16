package com.oms.module.inventory.repository;

import com.oms.module.inventory.entity.PricingDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingDraftRepository extends JpaRepository<PricingDraft, Long> {
    Optional<PricingDraft> findByUsername(String username);
}