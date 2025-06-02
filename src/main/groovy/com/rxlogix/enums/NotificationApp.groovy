package com.rxlogix.enums

/**
 * Created by Chetan on 3/4/2016.
 */
enum NotificationApp {

    ACTIONITEM("actionItem"),
    COMMENTS("comments"),
    REPORTREQUEST("reportRequest"),
    CASESERIES("caseSeries"),
    AGGREGATE_REPORT("aggregateReport"),
    ADHOC_REPORT("adhocReport"),
    ICSR_REPORT("icsrReport"),
    QUALITY("quality"),
    INBOUNDCOMPLIACE("inboundCompliance"),
    DOWNLOAD("download"),
    PVC_REPORT("pvcReport"),
    EXPORT("export"),
    ERROR("error")

    String name

    NotificationApp(String name) {
        this.name = name
    }

    public getI18nKeyNotificationMessage() {
        return "app.NotificationApp.notificationMessge.${this.name()}"
    }
}