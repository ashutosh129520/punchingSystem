package com.ttn.punchingSystem.repository;
import com.ttn.punchingSystem.model.PunchLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface PunchLogRepository extends JpaRepository<PunchLog, Long> {

    List<PunchLog> findByUserEmailAndPunchDate(String userEmail, Date punchDate);
}

