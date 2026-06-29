package com.gymconnect.workload.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * In-memory aggregate holding a single trainer's accumulated training duration,
 * bucketed by year and then by month.
 *
 * <p>The nested {@link TreeMap}s keep years and months naturally ordered, which
 * makes building an ordered response trivial. All mutating operations are
 * synchronized on the instance so concurrent ADD/DELETE events for the same
 * trainer stay consistent.</p>
 */
public class TrainerWorkload {

    private final String username;
    private String firstName;
    private String lastName;
    private boolean active;

    /** year -> (month -> total duration in minutes). */
    private final Map<Integer, Map<Integer, Integer>> durationByYearAndMonth = new TreeMap<>();

    public TrainerWorkload(String username, String firstName, String lastName, boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    /** Refreshes the mutable identity fields from the latest event. */
    public synchronized void updateIdentity(String firstName, String lastName, boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    /** Accrues {@code minutes} to the given year/month bucket. */
    public synchronized void addDuration(int year, int month, int minutes) {
        Map<Integer, Integer> months = durationByYearAndMonth.computeIfAbsent(year, y -> new TreeMap<>());
        months.merge(month, minutes, Integer::sum);
    }

    /**
     * Reverses {@code minutes} from the given year/month bucket. The total is
     * floored at zero, and empty month/year buckets are pruned so a trainer whose
     * trainings are all removed leaves no stale data behind.
     */
    public synchronized void subtractDuration(int year, int month, int minutes) {
        Map<Integer, Integer> months = durationByYearAndMonth.get(year);
        if (months == null) {
            return;
        }
        Integer current = months.get(month);
        if (current == null) {
            return;
        }
        int remaining = current - minutes;
        if (remaining > 0) {
            months.put(month, remaining);
        } else {
            months.remove(month);
        }
        if (months.isEmpty()) {
            durationByYearAndMonth.remove(year);
        }
    }

    /** @return an ordered, read-only snapshot of the year/month totals. */
    public synchronized Map<Integer, Map<Integer, Integer>> snapshot() {
        Map<Integer, Map<Integer, Integer>> copy = new TreeMap<>();
        durationByYearAndMonth.forEach((year, months) -> copy.put(year, new TreeMap<>(months)));
        return copy;
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

    public boolean isActive() {
        return active;
    }
}
