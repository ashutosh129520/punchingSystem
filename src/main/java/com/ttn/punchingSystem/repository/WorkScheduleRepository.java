package com.ttn.punchingSystem.repository;

import com.ttn.punchingSystem.model.WorkScheduleDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkScheduleDetails, Long> {

    Set<WorkScheduleDetails> findAllByUserEmailIn(Set<String> userEmails);
}

