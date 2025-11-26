package com.oneonone.common.enums;

public enum UserRole {
    MASTER, // 마스터
    USER; // 일반 유저

    public boolean isMaster() {
        return this == MASTER;
    }

    public boolean isUser() {
        return this == USER;
    }
}