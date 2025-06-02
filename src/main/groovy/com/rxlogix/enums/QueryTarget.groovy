package com.rxlogix.enums

import grails.util.Holders

enum QueryTarget {
    REPORTS ("REPORTS"),
    EVDAS ("EVDAS"),
    FAERS ("FAERS"),
    EMBASE ("EMBASE")

    final String value

    QueryTarget(String value){
        this.value = value
    }

    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.queryTarget.${this.name()}"
    }

    public static List getQueryTarget() {
        List queryTargetList = []
        if(Holders.config.getProperty('pvsignal.faers.enabled', Boolean)) {
            queryTargetList.add(FAERS)
        }
        if(Holders.config.getProperty('pvsignal.evdas.enabled', Boolean)) {
            queryTargetList.add(EVDAS)
        }
        if(Holders.config.getProperty('pvsignal.embase.enabled', Boolean)) {
            queryTargetList.add(EMBASE)
        }
        if(queryTargetList.size() > 0) {
            queryTargetList.add(REPORTS)
        }
        return queryTargetList
    }
}