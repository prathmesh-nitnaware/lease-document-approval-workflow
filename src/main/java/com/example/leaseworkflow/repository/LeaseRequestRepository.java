package com.example.leaseworkflow.repository;

import com.example.leaseworkflow.model.LeaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaseRequestRepository extends JpaRepository<LeaseRequest, Long> {
    List<LeaseRequest> findByRequesterId(String requesterId);
}
