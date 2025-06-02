package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.GranularityEnum
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
abstract class BaseTemplateQuery {

    String header
    String title
    String footer
    boolean headerProductSelection = false
    boolean headerDateRange = false
    boolean displayMedDraVersionNumber = false
    boolean blindProtected = false // Used for CIOMS I Template.
    boolean privacyProtected = false // Used for CIOMS I Template.
    QueryLevelEnum queryLevel = QueryLevelEnum.CASE
    GranularityEnum granularity
    Date reassessListednessDate
    Date templtReassessDate
    UserGroup userGroup
    Integer dueInDays = 0
    Long issueType
    Long rootCause
    Long responsibleParty
    User assignedToUser
    UserGroup assignedToGroup
    String priority
    String summary
    String actions
    String investigation
    String summarySql
    String actionsSql
    String investigationSql

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapWith = "none"

    static mapping = {
        header column: "HEADER"
        title column: "TITLE"
        footer column: "FOOTER"
        headerProductSelection column: "HEADER_PRODUCT_SELECTION"
        headerDateRange column: "HEADER_DATE_RANGE"
        displayMedDraVersionNumber column: "DISPLAY_MEDDRA"
        privacyProtected column: "PRIVACY_PROTECTED"
        blindProtected column: "BLIND_PROTECTED"
        queryLevel column: "QUERY_LEVEL"
        granularity column: "GRANULARITY"
        responsibleParty column: "RESPONSIBLE_PARTY"
        rootCause column: "ROOT_CAUSE"
        issueType column: "ISSUE_TYPE"
        assignedToGroup column: "ASSIGNED_TO_GROUP_ID"
        assignedToUser column: "ASSIGNED_TO_USER_ID"
        priority column: "PRIORITY"
        summary column: "SUMMARY"
        investigation column: "INVESTIGATION"
        actions column: "ACTIONS"
        summarySql column: "SUMMARY_SQL"
        actionsSql column: "ACTIONS_SQL"
        investigationSql column: "INVESTIGATION_SQL"
        dueInDays column: "DUE_DAYS"
        reassessListednessDate column: "REASSESS_LISTEDNESS_DATE"
        templtReassessDate column: "TEMPLT_REASSESS_DATE", defaultValue: null
        userGroup cascade:'none'
    }

    static constraints = {
        header(nullable: true, maxSize: 255)
        title(nullable: true, maxSize: 555)
        footer(nullable: true, maxSize: 1000)
        granularity(nullable: true)
        issueType(nullable: true)
        rootCause(nullable: true)
        priority(nullable: true)
        responsibleParty(nullable: true)
        assignedToUser(nullable: true)
        assignedToGroup(nullable: true)
        userGroup(nullable: true)
        dueInDays nullable: true
        reassessListednessDate(nullable: true)
        templtReassessDate(nullable: true)
        summary(nullable: true, maxSize: 8000)
        investigation(nullable: true, maxSize: 8000)
        actions(nullable: true, maxSize: 8000)
        summarySql(nullable: true, maxSize: 32000)
        actionsSql(nullable: true, maxSize: 32000)
        investigationSql(nullable: true, maxSize:32000)
    }

    transient abstract Date getStartDate();

    transient abstract Date getEndDate();

    transient abstract BaseConfiguration getUsedConfiguration();

    transient abstract ReportTemplate getUsedTemplate();

    transient abstract SuperQuery getUsedQuery();

    transient abstract List<TemplateValueList> getUsedTemplateValueLists();

    transient abstract List<QueryValueList> getUsesQueryValueLists();

    transient abstract BaseDateRangeInformation getUsedDateRangeInformationForTemplateQuery();

    transient abstract Map<String,String> getPOIInputsKeysValues();

}

