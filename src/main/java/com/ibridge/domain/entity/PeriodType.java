package com.ibridge.domain.entity;

public enum PeriodType {
    DAY, WEEK, MONTH, YEAR, CUMULATIVE;

    public String toString() {
        return this.name();
    }

    public PeriodType fromOrdinal(int ordinal) { return values()[ordinal]; }
}
