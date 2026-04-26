package com.gymconnect.model;

import java.time.LocalDate;

public class Trainee extends User {

    private LocalDate dateOfBirth;
    private String address;

    public Trainee() {
    }

    public Trainee(String firstName, String lastName, boolean isActive,
                   LocalDate dateOfBirth, String address) {
        super(firstName, lastName, isActive);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Trainee{id=" + getId()
                + ", firstName='" + getFirstName() + '\''
                + ", lastName='" + getLastName() + '\''
                + ", username='" + getUsername() + '\''
                + ", isActive=" + isActive()
                + ", dateOfBirth=" + dateOfBirth
                + ", address='" + address + '\'' + '}';
    }
}
