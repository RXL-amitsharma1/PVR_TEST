package com.rxlogix.config

class DrilldownAccessTracker {

    State state = State.ACTIVE
    Long reportResultId
    Date lastAccess

    static constraints = {
        reportResultId unique: true
    }

    static mapping = {
        table('DRILLDOWN_ACCESS_TRACKER')
        state column: 'STATUS'
        reportResultId column: 'REPORT_ID'
        lastAccess column: 'LAST_ACCESS'
    }

    static enum State {
        ACTIVE, ARCHIVED, ARCHIVING, RELOADING
    }

}