databaseChangeLog = {

    changeSet(author: "Sachin (generated)", id: "1474691025599-2") {
        sql("update REPORT_REQUEST set STATUS = 'OPEN' where STATUS = 'Open'")
        sql("update REPORT_REQUEST set STATUS = 'CLOSED' where STATUS = 'Closed'")
        sql("update REPORT_REQUEST set STATUS = 'IN_PROGRESS' where STATUS = 'In Progress'")
        sql("update REPORT_REQUEST set STATUS = 'NEED_CLARIFICATION' where STATUS = 'Need Clarification'")

        sql("update ACTION_ITEM set STATUS = 'OPEN' where STATUS = 'Open'")
        sql("update ACTION_ITEM set STATUS = 'CLOSED' where STATUS = 'Closed'")
        sql("update ACTION_ITEM set STATUS = 'IN_PROGRESS' where STATUS = 'In Progress'")
        sql("update ACTION_ITEM set STATUS = 'NEED_CLARIFICATION' where STATUS = 'Need Clarification'")

        sql("update ACTION_ITEM set COMPLETION_DATE = DUE_DATE where STATUS = 'CLOSED' and COMPLETION_DATE is null")
        sql("update REPORT_REQUEST set COMPLETION_DATE = DUE_DATE where STATUS = 'CLOSED' and COMPLETION_DATE is null")
    }
}