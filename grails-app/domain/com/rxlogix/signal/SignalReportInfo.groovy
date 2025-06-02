package com.rxlogix.signal

import com.rxlogix.config.Configuration

class SignalReportInfo {

    Configuration configuration
    String reportName
    String userName
    String linkUrl
    long userId

    //Common parameters
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    boolean isGenerating

    static mapping = {
        table name: "SIGNAL_REPORT"
    }

    static namedQueries = {

        getScheduledSignalReportForUserForConfiguration { Configuration configuration, String reportName, String userName ->
           eq('isGenerating', true)
           eq('configuration', configuration)
           eq('reportName', reportName)
           eq('userName', userName)
        }
    }

    @Override
    String toString() {
        return "[" + "SignalReportInfo:"+this.id + "]"
    }
}
