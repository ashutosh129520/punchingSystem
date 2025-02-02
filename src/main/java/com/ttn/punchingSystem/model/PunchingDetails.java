package com.ttn.punchingSystem.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "employee_punch_log", uniqueConstraints = @UniqueConstraint(columnNames = {"userEmail", "punchDate"}))
public class PunchingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String userEmail;

    @NotNull
    @Temporal(TemporalType.DATE)
    private Date punchDate;

    @Temporal(TemporalType.TIME)
    private Date punchInTime;

    @Temporal(TemporalType.TIME)
    private Date punchOutTime;

    private long durationInHours;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Date getPunchDate() {
        return punchDate;
    }

    public void setPunchDate(Date punchDate) {
        this.punchDate = punchDate;
    }

    public Date getPunchInTime() {
        return punchInTime;
    }

    public void setPunchInTime(Date punchInTime) {
        this.punchInTime = punchInTime;
    }

    public Date getPunchOutTime() {
        return punchOutTime;
    }

    public void setPunchOutTime(Date punchOutTime) {
        this.punchOutTime = punchOutTime;
    }

    public long getDurationInHours() {
        return durationInHours;
    }

    @Override
    public String toString() {
        return "PunchingDetails{" +
                "userEmail='" + userEmail + '\'' +
                ", punchDate=" + punchDate +
                ", punchInTime=" + punchInTime +
                ", punchOutTime=" + punchOutTime +
                ", durationInHours=" + durationInHours +
                '}';
    }

    public void setDurationInHours(long durationInHours) {
        this.durationInHours = durationInHours;
    }
}

