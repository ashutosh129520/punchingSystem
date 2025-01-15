package com.ttn.punchingSystem.repository;
import com.ttn.punchingSystem.model.PunchingDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface PunchLogRepository extends JpaRepository<PunchingDetails, Long> {

    List<PunchingDetails> findByUserEmailAndPunchDate(String userEmail, Date punchDate);
    List<PunchingDetails> findByPunchDate(Date previousDay);
    List<PunchingDetails> findByUserEmail(String previousDay);
}

