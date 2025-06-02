package com.rxlogix.response

enum ResponseAlertType {
    SUCCESS("success"),
    DANGER("danger"),
    WARNING("warning"),
    INFO("info")

    final String type

    ResponseAlertType(String type) {
        this.type = type
    }

    @Override
    public String toString() {
        return this.type
    }
}