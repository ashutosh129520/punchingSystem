package com.ttn.punchingSystem.repository;

import com.ttn.punchingSystem.model.WorkScheduleDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkScheduleDetails, Long> {

    List<WorkScheduleDetails> findAllByUserEmailIn(List<String> userEmails);
    Optional<WorkScheduleDetails> findByUserEmail(String userEmails);
}

