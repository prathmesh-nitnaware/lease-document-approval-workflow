package com.example.leaseworkflow.repository;

import com.example.leaseworkflow.model.ReviewAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewActionRepository extends JpaRepository<ReviewAction, Long> {
    List<ReviewAction> findByRequestId(Long requestId);
}
