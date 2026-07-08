package com.gymconnect.workload.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB document holding a single trainer's training summary: identity fields plus
 * the accrued training duration bucketed by year and then by month.
 *
 * <p>The {@code username} is the natural key and is stored as the document {@code _id}
 * (a unique index for free). A compound index on {@code firstName + lastName} supports
 * searching trainers by name. Years and months are kept ordered so summaries read
 * naturally. Behaviour ({@link #addDuration}/{@link #subtractDuration}) lives on the
 * document itself, keeping the service thin.</p>
 */
@Document(collection = "trainer_workloads")
@CompoundIndex(name = "trainer_name_idx", def = "{'firstName': 1, 'lastName': 1}")
public class TrainerWorkload {

    @Id
    private String username;

    private String firstName;

    private String lastName;

    /** Whether the trainer is currently active. Boolean per the schema requirement. */
    private Boolean active;

    private List<YearSummary> years = new ArrayList<>();

    /** Required by Spring Data for document instantiation. */
    public TrainerWorkload() {
    }

    public TrainerWorkload(String username, String firstName, String lastName, Boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    /** Refreshes the mutable identity fields from the latest event. */
    public void updateIdentity(String firstName, String lastName, Boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    /** Accrues {@code minutes} to the given year/month, creating records as needed. */
    public void addDuration(int year, int month, long minutes) {
        YearSummary yearSummary = findYear(year).orElseGet(() -> {
            YearSummary created = new YearSummary(year);
            years.add(created);
            years.sort(Comparator.comparingInt(YearSummary::getYear));
            return created;
        });
        yearSummary.addDuration(month, minutes);
    }

    /**
     * Reverses {@code minutes} from the given year/month. Empty month and year
     * buckets are pruned so a trainer whose trainings are all removed leaves no
     * stale data behind.
     */
    public void subtractDuration(int year, int month, long minutes) {
        findYear(year).ifPresent(yearSummary -> {
            yearSummary.subtractDuration(month, minutes);
            if (yearSummary.isEmpty()) {
                years.remove(yearSummary);
            }
        });
    }

    /** @return the year record for {@code year}, if one exists. */
    public Optional<YearSummary> findYear(int year) {
        return years.stream().filter(y -> y.getYear() == year).findFirst();
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Boolean getActive() {
        return active;
    }

    public List<YearSummary> getYears() {
        return years;
    }
}
