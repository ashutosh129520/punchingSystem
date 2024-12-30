package com.ttn.punchingSystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PunchData {
    private String userEmail;
    private String punchTime;

    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(String punchTime) {
        this.punchTime = punchTime;
    }

    @Override
    public String toString() {
        return "PunchData{" +
                "userEmail='" + userEmail + '\'' +
                ", punchTime='" + punchTime + '\'' +
                '}';
    }
}

