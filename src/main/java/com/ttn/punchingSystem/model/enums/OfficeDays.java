package com.ttn.punchingSystem.model.enums;

public enum OfficeDays {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday");

    private final String description;

    OfficeDays(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
