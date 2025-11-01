package com.ibridge.domain.entity;

public enum Status {
    PENDING, FIRST_LOGIN, ACTIVE;

    public String toString(){ return this.name();}
}
