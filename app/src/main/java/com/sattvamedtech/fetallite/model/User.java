package com.sattvamedtech.fetallite.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

public class User implements Serializable {

    public static final int TYPE_USER = 0;
    public static final int TYPE_DOCTOR = 1;

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public int id;

    @DatabaseField
    public String username;

    @DatabaseField
    public String password;

    @DatabaseField
    public String phoneNumber;

    @DatabaseField
    public String email;

    @DatabaseField
    public int type;

    @DatabaseField(foreign = true, foreignColumnName = "hospitalId")
    public Hospital hospital;

    public User(String username, String password, String phoneNumber, String email, int type) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.type = type;
        this.hospital = null;
    }

    public User(String username, String password, String phoneNumber, String email, int type, Hospital hospital) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.type = type;
        this.hospital = hospital;
    }

    public User() {
    }

    @Override
    public String toString() {
        return this.username;
    }
}
