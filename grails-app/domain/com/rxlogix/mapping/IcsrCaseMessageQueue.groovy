package com.rxlogix.mapping

class IcsrCaseMessageQueue implements Serializable {

    Long caseId
    String key
    String value
    Date dateCreated
    Date lastUpdated
    String type
    String status
    boolean isRead = 0

    public IcsrCaseMessageQueue() {}

    public IcsrCaseMessageQueue(Long caseId, String key, String value, Date dateCreated, Date lastUpdated, String type, String status, boolean isRead) {
        this.caseId = caseId
        this.key = key
        this.value = value
        this.dateCreated = dateCreated
        this.lastUpdated = lastUpdated
        this.type = type
        this.status = status
        this.isRead = isRead
    }

    static constraints = {
        caseId(nullable: false)
        key(nullable: false )
        value(nullable: true)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        type(nullable: true)
        status(nullable: true)
    }

    static mapping = {
        datasource "pvcm"
        table "PVCM_APP_MESSAGE_QUEUE"
        id column: 'ID', generator: "sequence", params: [sequence: "SEQ_APP_MESSAGE_QUEUE"]
        version false
        caseId column: 'INTAKE_CASE_ID'
        key column: 'KEY'
        value column: 'VALUE'
        dateCreated column: 'CREATED_DATE'
        lastUpdated column: 'UPDATE_DATE'
        type column: 'CASE_CREATED_TYPE'
        status column: 'STATUS'
        isRead column: 'MESSAGE_READ'
    }
}
