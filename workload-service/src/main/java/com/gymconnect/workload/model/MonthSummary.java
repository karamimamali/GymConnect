package com.gymconnect.workload.model;

/**
 * A single month's entry inside a {@link YearSummary}: the month number and the
 * summed training duration accrued in that month.
 *
 * <p>Persisted as a sub-document of the {@code trainer_workloads} collection.</p>
 */
public class MonthSummary {

    /** Month number, 1 (January) – 12 (December). */
    private int month;

    /** Summed training duration (minutes) for the month. A numeric value. */
    private long trainingSummaryDuration;

    /** Required by Spring Data for document instantiation. */
    public MonthSummary() {
    }

    public MonthSummary(int month, long trainingSummaryDuration) {
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
    }

    /** Adds {@code minutes} to this month's running total. */
    public void add(long minutes) {
        this.trainingSummaryDuration += minutes;
    }

    /**
     * Subtracts {@code minutes} from this month's total, flooring the result at
     * zero so a duration can never go negative.
     *
     * @return the remaining duration after the subtraction
     */
    public long subtract(long minutes) {
        this.trainingSummaryDuration = Math.max(0L, this.trainingSummaryDuration - minutes);
        return this.trainingSummaryDuration;
    }

    public int getMonth() {
        return month;
    }

    public long getTrainingSummaryDuration() {
        return trainingSummaryDuration;
    }
}
