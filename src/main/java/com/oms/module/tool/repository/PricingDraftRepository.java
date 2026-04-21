package com.oms.module.tool.repository;

import com.oms.module.tool.entity.PricingDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingDraftRepository extends JpaRepository<PricingDraft, Long> {
    Optional<PricingDraft> findByUsername(String username);

}