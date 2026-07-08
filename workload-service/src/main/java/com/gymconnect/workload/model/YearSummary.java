package com.gymconnect.workload.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A single year's entry inside a {@link TrainerWorkload}: the year and its ordered
 * list of {@link MonthSummary} records.
 *
 * <p>Persisted as a sub-document of the {@code trainer_workloads} collection. Months
 * are kept sorted by month number so responses come out in a natural order.</p>
 */
public class YearSummary {

    private int year;

    private List<MonthSummary> months = new ArrayList<>();

    /** Required by Spring Data for document instantiation. */
    public YearSummary() {
    }

    public YearSummary(int year) {
        this.year = year;
    }

    /** Accrues {@code minutes} to the given month, creating the record if absent. */
    public void addDuration(int month, long minutes) {
        MonthSummary monthSummary = findMonth(month).orElseGet(() -> {
            MonthSummary created = new MonthSummary(month, 0L);
            months.add(created);
            months.sort(Comparator.comparingInt(MonthSummary::getMonth));
            return created;
        });
        monthSummary.add(minutes);
    }

    /**
     * Reverses {@code minutes} from the given month (floored at zero) and prunes the
     * month record if it drops to zero, so emptied buckets leave no stale data.
     */
    public void subtractDuration(int month, long minutes) {
        findMonth(month).ifPresent(monthSummary -> {
            long remaining = monthSummary.subtract(minutes);
            if (remaining <= 0L) {
                months.remove(monthSummary);
            }
        });
    }

    /** @return the month record for {@code month}, if one exists. */
    public Optional<MonthSummary> findMonth(int month) {
        return months.stream().filter(m -> m.getMonth() == month).findFirst();
    }

    /** @return {@code true} when this year holds no month records. */
    public boolean isEmpty() {
        return months.isEmpty();
    }

    public int getYear() {
        return year;
    }

    public List<MonthSummary> getMonths() {
        return months;
    }
}
