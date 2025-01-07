package com.ttn.punchingSystem.repository;

import com.ttn.punchingSystem.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
