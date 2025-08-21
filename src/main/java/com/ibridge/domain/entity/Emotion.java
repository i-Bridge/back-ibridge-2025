package com.ibridge.domain.entity;

public enum Emotion {
    ZERO, ONE, TWO, THREE, FOUR, FIVE;

    public static Emotion fromOrdinal (int ordinal) { return values()[ordinal]; }
}
