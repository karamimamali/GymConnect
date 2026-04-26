package com.gymconnect.model;

public class Trainer extends User {

    private TrainingType specialization;

    public Trainer() {
    }

    public Trainer(String firstName, String lastName, boolean isActive,
                   TrainingType specialization) {
        super(firstName, lastName, isActive);
        this.specialization = specialization;
    }

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return "Trainer{id=" + getId()
                + ", firstName='" + getFirstName() + '\''
                + ", lastName='" + getLastName() + '\''
                + ", username='" + getUsername() + '\''
                + ", isActive=" + isActive()
                + ", specialization=" + specialization + '}';
    }
}
