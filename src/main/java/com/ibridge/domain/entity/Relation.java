package com.ibridge.domain.entity;

public enum Relation {
    FATHER, MOTHER;

    public String toString() {
        return this.name();
    }
}