package com.ibridge.domain.entity;

public enum Gender {
    MALE, FEMALE, NONE;

    public String toString() {
        return this.name();
    }
}