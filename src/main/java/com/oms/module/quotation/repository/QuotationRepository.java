package com.oms.module.quotation.repository;

import com.oms.module.quotation.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    Optional<Quotation> findByQuotationCode(String quotationCode);

    boolean existsByQuotationCode(String quotationCode);
}