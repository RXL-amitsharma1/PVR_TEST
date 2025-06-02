package com.rxlogix.config.publisher

import com.google.gson.Gson
import com.rxlogix.publisher.PublisherExecutionLog

class PublisherExecutedTemplate {

    String name
    Integer numOfExecution = 0
    Status status
    ExecutionStatus executionStatus
    byte[] data
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "-"
    String modifiedBy = "-"

    static belongsTo = [publisherConfigurationSection: PublisherConfigurationSection]
    static hasOne = [publisherExecutionLog: PublisherLog]

    static mapping = {
        table name: "PUBLISHER_EX_TMPLT"
        name column: "NAME"
        publisherConfigurationSection column: "publisher_cfg_sec_id"
        data column: "DATA", lazy: true
        executionStatus column: "EXECUTION_STATUS"
    }


    static constraints = {
        data(nullable: true, maxSize: 20971520)
        publisherConfigurationSection nullable: true
        publisherExecutionLog nullable: true
        executionStatus nullable: true
        lastUpdated nullable: true
        createdBy nullable: true
        modifiedBy nullable: true
    }

    transient String getExecutionLogJson() {
        return publisherExecutionLog?.executionLogJson ?: "{}"
    }

    transient void setExecutionLog(PublisherExecutionLog log) {
        Gson gson = new Gson()
        if (!publisherExecutionLog)
            publisherExecutionLog = new PublisherLog()
        publisherExecutionLog.executionLogJson = gson.toJson(log.toMap())
        publisherExecutionLog.publisherExecutedTemplate = this
        if (log.fatal) {
            executionStatus = ExecutionStatus.FATAL_ERROR
            status = Status.EMPTY
        } else if (log.errors) {
            executionStatus = ExecutionStatus.ERRORS
            status = Status.EMPTY
        } else if (log.warnings) {
            executionStatus = ExecutionStatus.WARNINGS
            status = Status.DRAFT
        } else {
            executionStatus = ExecutionStatus.SUCCESS
            status = Status.DRAFT
        }
    }

    static enum Status {
        EMPTY,
        ARCHIVE,
        DRAFT,
        FINAL

        public getI18nKey() {
            return "app.publisherExecutionStatus.Status.${this.name()}"
        }
    }

    static enum ExecutionStatus {
        SUCCESS,
        WARNINGS,
        ERRORS,
        FATAL_ERROR

        public getI18nKey() {
            return "app.publisherExecutionStatus.${this.name()}"
        }
    }
    String toString() {
        return name
    }
}
