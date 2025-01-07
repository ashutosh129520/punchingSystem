package com.ttn.punchingSystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "reporting_manager_email", nullable = false)
    private String reportingManagerEmail;
}
