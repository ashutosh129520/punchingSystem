package com.ttn.punchingSystem.model.enums;

public enum WorkShift {
    MORNING("Morning Shift"),
    AFTERNOON("Afternoon Shift"),
    NIGHT("Night Shift");

    private final String description;

    WorkShift(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
