package com.oms.module.maintenance.repository;

import com.oms.module.maintenance.entity.MaintenanceTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long> {
    Optional<MaintenanceTicket> findByTicketCode(String ticketCode);

    boolean existsByTicketCode(String ticketCode);
}