package com.ttn.punchingSystem.model;

import com.ttn.punchingSystem.model.enums.OfficeDays;
import com.ttn.punchingSystem.model.enums.WorkShift;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "work_schedule_details", uniqueConstraints = @UniqueConstraint(columnNames = {"userEmail"}))
public class WorkScheduleDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "work_shift", nullable = false)
    private WorkShift workShift;

    @Column(name = "office_days", nullable = false)
    private OfficeDays officeDays;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false)
    private Project project;
}

