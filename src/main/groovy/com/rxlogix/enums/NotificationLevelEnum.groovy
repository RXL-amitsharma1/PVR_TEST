package com.rxlogix.enums

public enum NotificationLevelEnum {
    INFO("Information"),
    WARN("Warning"),
    ERROR("Error")

    String name

    NotificationLevelEnum(String name) {
        this.name = name
    }

}