package com.ibridge.domain.entity;

public enum Emotion {
    HAPPY, SAD;

    public Emotion fromOrdinal (int ordinal) { return values()[ordinal]; }
}
