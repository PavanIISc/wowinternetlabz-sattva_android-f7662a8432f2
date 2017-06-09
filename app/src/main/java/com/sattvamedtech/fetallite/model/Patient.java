package com.sattvamedtech.fetallite.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

public class Patient implements Serializable {

    @DatabaseField(id = true)
    public String id;

    @DatabaseField
    public String firstName;

    @DatabaseField
    public String lastName;

    @DatabaseField
    public long dob;

    @DatabaseField
    public String riskFactor;

    @DatabaseField
    public String gravidity;

    @DatabaseField
    public String parity;

    @DatabaseField
    public int gestationalWeeks;

    @DatabaseField
    public int gestationalDays;

    @DatabaseField(foreign = true, foreignColumnName = "id")
    public User doctor;

    public Patient(String id, String firstName, String lastName, long dob, String riskFactor, String gravidity, String parity, int gestationalWeeks, int gestationalDays, User doctor) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.riskFactor = riskFactor;
        this.gravidity = gravidity;
        this.parity = parity;
        this.gestationalWeeks = gestationalWeeks;
        this.gestationalDays = gestationalDays;
        this.doctor = doctor;
    }

    public Patient() {
    }
}