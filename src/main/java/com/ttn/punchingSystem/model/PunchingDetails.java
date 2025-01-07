package com.ttn.punchingSystem.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "employee_punch_log")
public class PunchingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

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

    public void setDurationInHours(long durationInHours) {
        this.durationInHours = durationInHours;
    }
}

