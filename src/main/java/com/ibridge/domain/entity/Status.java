package com.ibridge.domain.entity;

public enum Status {
    PEDING, FIRST_LOGIN, ACTIVE;

    public String toString(){ return this.name();}
}
