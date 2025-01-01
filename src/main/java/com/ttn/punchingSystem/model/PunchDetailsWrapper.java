package com.ttn.punchingSystem.model;

import java.util.Date;

public class PunchDetailsWrapper {
    private String userEmail;
    private Date punchIn;
    private Date punchOut;

    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Date getPunchIn() {
        return punchIn;
    }

    public void setPunchIn(Date punchIn) {
        this.punchIn = punchIn;
    }

    public Date getPunchOut() {
        return punchOut;
    }

    public void setPunchOut(Date punchOut) {
        this.punchOut = punchOut;
    }
}

