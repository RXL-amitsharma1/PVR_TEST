package com.rxlogix

interface Constants {

    //Using interfaces for namespacing separation
    String SESSION_TIME_OUT = "sessionTimeOut"
    String USER_GROUP_TOKEN = "UserGroup_"
    String USER_TOKEN = "User_"
    String SAML_ERROR_MSG = "Error validating SAML message"

    String GROUP_MANAGER = "MANAGER_"

    String OWNER_SELECT_VALUE = "owner"
    String TEAM_SELECT_VALUE = "team"
    String SHARED_WITH_ME_SELECT_VALUE = "sharedWithMe"

    String DEFAULT_SELECTED_TIMEZONE = "UTC"
    String DAY_START_TIME_AM_PM = "12:00:00 AM"
    String DAY_END_TIME_AM_PM = "11:59:59 PM"
    //In case of ajaxified multiple select2
    String MULTIPLE_AJAX_SEPARATOR = "@!"
    String PREVIEW_QUERY = "Preview of Query "
    String SQL_DML_PATTERN_REGEX = /((^|(.*?(\s|\(|\))))(insert|use|alter|desc|create|drop|delete|update)\s.*?$)|.*?;.*?$/
    String SQL_DML_SELECT_PATTERN_REGEX = /((^|(.*?(\s|\(|\))))(select|insert|use|alter|desc|create|drop|delete|update)\s.*?$)|.*?;.*?$/
    String POI_INPUT_PATTERN_REGEX = /&\S+[.]/
    String RPT_INPUT_PATTERN_REGEX = /''#_\S+/
    String RPT_INPUT_PREFIX = '#_'
    String CUSTOM_SQL_VALUE_REGEX_CONSTANT = /:([a-zA-Z][a-zA-Z0-9_-]*)(?=(?:[^']|'[^']*')*+$)/
    String NUMBER_INPUT_PATTERN_REGEX = /(.)*(\d)(.)*/
    String CSV_INPUT_PATTERN_REGEX = /^[=-@+].*/
    String CSV_REGEX_CONSTANT = /^[=-@+]/
    String ICSR_UNIT_CONF_REGULATORY_AUTHORITY="Regulatory Authority"

    String SIGNAL_REPORT_TAG_NAME = "Signal Report"
    String EVDAS_DATA_SOURCE = "EVD"
    String AFFLIATE_DATA_SOURCE = "AFF"
    String SINGLE_DATASOURCE_QUERY = "select cm.tenant_id, cm.case_id, cm.version_num from V_C_IDENTIFICATION cm "
    String MULTIPLE_DATASOURCE_QUERY = "select cm.tenant_id, cm.case_id, cm.version_num FROM gtt_query_case_list gt LEFT OUTER JOIN v_c_identification cm ON ( cm.tenant_id = gt.tenant_id AND cm.version_num = gt.version_num AND cm.case_id = gt.case_id ) LEFT OUTER JOIN v_a_identification cmaff ON ( cmaff.tenant_id = gt.src_2_tenant_id AND cmaff.version_num = gt.src_2_version_num AND cmaff.case_id = gt.src_2_case_id) "

    //For Case series integration
    String PVR_CASE_SERIES_OWNER = "PVR"
    String PVS_CASE_SERIES_OWNER = "PVS"
    String CASE_LIST_COLUMN = "CASE_LIST"
    String CASE_COUNT_COLUMN = "CASE_COUNT"
    String INTERVAL_CASE_COUNT_COLUMN = "INTERVAL_CASE_COUNT"
    String JSON_ITERATOR_REGEX = /(\w+)_(\d+)_(\w+)/
    String JSON_HEADER_REGEX = "[\\r\\n]+"
    String PERCENTAGE_COLUMN = "P"
    String CHART_TYPE = "column"
    String AUTO_REASON_OF_DELAY = "Auto Reason Of Delay Job"
    String NOT_APPLICABLE = "Not Applicable"

    String REPORTING_PERIOD_START_DATE = "<Reporting Period Start Date>"
    String REPORTING_PERIOD_END_DATE = "<Reporting Period End Date>"

    String EMPTY = "(empty)"
    String BLANK_STRING = ""
    String SPACE_STRING = " "
    String ALL = "ALL"
    String NONE = "NONE"
    String NA = "N/A"
    String CREATE = 'create'
    String UPDATE = 'update'
    String DELETE = 'delete'
    String ASSIGNED_TO = 'assignedTo'
    String ASSIGNED_TO_GROUP = 'assignedToGroup'
    String WORKFLOW_CHANGES = 'workflowChanges'
    String WORKFLOW_UPDATE = 'workflowUpdate'
    String JOB = 'job'

    String GENERAL_REPORT = "GENERAL"
    String VIP_REPORT = "VIP"
    String ICSR_PROFILE = "ICSR_PROFILE"
    String CASE_VERSION_SEPARATOR = '##'
    String FDA = "FDA"
    String FDA_22 = "FDAv2_2"
    String FDA_21 = "FDAv2_1"
    String EMDR = "EMDR"
    String SWISSMEDIC = "SWISSMEDIC"
    String HEALTH_CANADA = "HEALTH_CANADA"
    String ICH_21 = "ICHv2_1"
    String MIR = "MIR"
    String PMDA = "PMDA"
    String ADD_SIMPLE_FOR_R2 = "_Base"
    String ADD_PDF_FOR_R3_PDF = "_PDF"
    String ADD_PR_FOR_PAPER_REPORT = "_PR"
    String PVCM = "pvcm"
    String PVI_PUBLIC_TOKEN = 'zn9MrreyDiATUdoUs/FMmw70qMDExQOya/9LFs1uE5lCp2eCxNeOZCdTgubUCdbYWpLu3bRJRL5zD79iOm+sewLbXnt9r1KbSBNJhWd9BKhbGFhpYPVodA5J7P87aUnXfHLSSXB1F5xTJkCjyMszHA=='
    String EVENT_RECEIPT_DATE = 'dcdtEventReceiptdate'
    String EVENT_RECEIPT_DATE_PVR = 'dcdtDatecol14'
    String FOLLOWUP_QUERY_BODY_PVCM = "pcfqBody"
    List duplicateE2BTagParents = ["ichicsrtransmissionidentification","additionaldocuments"]
    List duplicateE2BTags = ["messagesenderidentifier","messagereceiveridentifier","additionaldocument"]
    String PVCM_WORKFLOW = "PVCM_WORKFLOW"
    String E2B_DRUG_TAG_START = "<drug>"
    String E2B_DRUG_TAG_END = "</drug>"
    String R3_SECONDARY_TAG = "<PORR_IN049016UV>"
    String ATTACH_FOLDER="Attach_Folder"
    String SPOTFIRE_ANALYSIS="pvr_new_analysis"
    String SPOTFIRE_CASE_SERIES="pvr_new_case_series"
    String UTF8 = "UTF-8"
    String DUE_DATE_COMPLETED="COMPLETED"
    String DUE_DATE_NOT_QUALIFIED="NOT_QUALIFIED"
    String DUE_DATE_PARTIAL_COMPLETED="PARTIAL_COMPLETED"
    String DUE_DATE_ERROR="ERROR"
    String DUE_DATE_KEY="DUE_DATE"
    String LOCAL_DUE_DATE_KEY="LOCAL_DUE_DATE"
    String DUE_DATE_TYPE_MANUAL="Manual"
    String DUE_DATE_TYPE_DELETE="Delete"
    String DUE_DATE_TYPE_SRN="Submission Not Required"

    String PDF_EXT = ".pdf"
    String PQC_CASE_NUM = "PQC_CASE_NUM"
    String LOC_CP_COMPLETE = "LOCAL_CP_COMPLETED"

    String APPROVED_BY = "Approved By : "
    String APPROVED_ON = "Approved On : "

    String OTHER_STRING = "Log In"

    //audit log events
    String AUDIT_LOG_INSERT = "INSERT"
    String AUDIT_LOG_UPDATE = "UPDATE"
    String AUDIT_LOG_DELETE = "DELETE"
    String AUDIT_LOG_EXPORT = "EXPORT"

    String HYPHEN = " - "
    String ADDED = "added"
    String UPDATED = "updated"
    String DELETED = "deleted"
    String USER_DEFINED_FIELD = "selectUDField"

    String PASSED = "passed"
    String FAILED = "failed"
    String PMDA_NUM = "PMDA_NUM"
    String FOLLOWUP = "Followup"
    String NULLIFICATION = "Nullification"

    String CIOMS_I_JRXML_FILENAME = 'CIOMS_I.jrxml'
    String MEDWATCH_JRXML_FILENAME = 'MEDWATCH.jrxml'
    String SUCCESS = "SUCCESS"
    String FAILURE = "FAILURE"
    String EDIT_METHOD = "edit"
    String DELETE_METHOD = "delete"

    int MAX_OFFICE_FILE_NAME_LENGTH = 195
    int ICSR_BULK_DOWNLOAD_MAX_LIMIT = 1000
    int PVSIGNAL_EMBASE_SOURCE_ID = 99
    int MAX_LIST_SIZE_DB = 999
    int COLLATE_LENGTH = 999

    interface Central {
        int RCA_COLUMN_NUMBER = 19
        int SUBMISSION_COLUMN_NUMBER = 8
    }

    interface Search {
        Long MAX_SEARCH_RESULTS = 25
    }

    interface JSONColumns {

        String COLUMNS = "columns"
        String SEQUENCE = "seq"
        String RENAMED_COLUMN = "newName"

    }

    interface DateFormat {
        String WITH_TZ = "yyyy-MM-dd'T'HH:mm:ssZZ"
        String WITHOUT_SECONDS = "yyyy-MM-dd'T'HH:mmZ"
        String SIMPLE_DATE = "MMM-dd-yyyy"
        String BASIC_DATE = "yyyyMMdd"
        String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm.SSZZ"
        String NO_TZ = "yyyy-MM-dd HH:mm:ss"
        String CSV_JASPER = "yyyy-MM-dd'T'HH:mm:ssZ"
        String WITHOUT_SEC_TZ = "yyyy-MM-dd'T'HH:mm'Z'"
        String REGULAR_DATE = "dd-MMM-yyyy"
        String SCHEDULE_DATE = "yyyy-MM-dd'T'HH:mm"
        String REGULAR_DATETIME = "dd-MMM-yyyy HH:mm:ss"
    }

    interface DynamicReports {
        String ADVANCED_OPTIONS_SUFFIX = "-adv"
    }

    interface AuditLog {
        String NO_VALUE = "AUDIT_LOG_NO_VALUE"
        String DELETED_VALUE = "AUDIT_LOG_DELETED_VALUE"
        String FIELDS_ADDED = "FIELDS_ADDED"
        String FIELDS_REMOVED = "FIELDS_REMOVED"
        String INSERT_FLAG = "I"
        String UPDATE_FLAG = "U"
        String DELETE_FLAG = "D"
    }

    interface Scheduler {
        String RUN_ONCE = "FREQ=DAILY;INTERVAL=1;COUNT=1"
        String HOURLY = "FREQ=HOURLY"
        String MINUTELY = "FREQ=MINUTELY"
    }

    interface ExcelConstants {
        int MAX_CELL_LENGTH_XLSX = 32767
        String TRUNCATE_TEXT_XLSX = "...(truncated)"
    }

    interface Roles {
        String CONFIG_TMPLT_CREATOR = "ROLE_CONFIG_TMPLT_CREATOR"
        String ADMIN = "ROLE_ADMIN"
        String SUPER_ADMIN = "ROLE_DEV"
    }

    LinkedHashMap Templates = [
            pvr : "templates.json",
            pvcentral : "templates_pvc.json",
            pvquality : "templates_pvq.json",
            pvpublisher : "templates_pvp.json",
            linelistingICSR : "templates_listing_ICSR*.json",
            xmlICSR : "templates_xml_ICSR*.json"
    ]

    Map Queries = [
            pvr : "queries.json",
            pvcentral : "queries_pvc.json",
            pvquality : "queries_pvq.json",
            pvpublisher : "queries_pvp.json"
    ]

    Map Dashboards = [
            pvr : "dashboards.json",
            pvcentral : "dashboards_pvc.json",
            pvquality : "dashboards_pvq.json",
            pvpublisher : "dashboards_pvp.json"
    ]

    Map Configurations = [
            pvr : "configurations.json",
            pvcentral : "configurations_pvc.json",
            pvquality : "configurations_pvq.json",
            pvpublisher : "configurations_pvp.json"
    ]

    Map icsrActionDropdownMap = [
            "SCHEDULED"                : ["localCPCompleted", "generateReport", "submissionNotRequired", "delete"],
            "READY_FOR_LOCAL_CP"       : ["transmit", "submit", "download", "emailTo"],
            "GENERATED"                : ["transmit", "submit", "Re-Generate", "download", "emailTo"],
            "GENERATION_ERROR"         : ["Re-Generate", "submissionNotRequired"],
            "SUBMISSION_NOT_REQUIRED"  : ["download"],
            "SUBMISSION_NOT_REQUIRED_FINAL": [],
            "SUBMITTED"                : ["download", "markNullification", "emailTo"],
            "TRANSMITTING"             : ["download", "emailTo"],
            "TRANSMITTED"              : ["submit", "markNullification", "download", "emailTo"],
            "TRANSMISSION_ERROR"       : ["Re-Generate", "transmit", "submissionNotRequired", "download", "emailTo"],
            "COMMIT_RECEIVED"          : ["submit", "download", "emailTo"],
            "COMMIT_ACCEPTED"          : ["submit", "download", "emailTo"],
            "COMMIT_REJECTED"          : ["Re-Generate", "submissionNotRequired", "download"],
            "PARSER_REJECTED"          : ["Re-Generate", "submissionNotRequired", "download"],
            "TRANSMITTING_ATTACHMENT"  : ["download", "emailTo"],
            "TRANSMITTED_ATTACHMENT"   : ["submit", "download", "emailTo"],
            "ERROR"                    : ["Re-Generate", "submissionNotRequired"]
    ]

    List ALLOWED_ASSIGNED_TO_ROLES_PVQ = ["ROLE_ADMIN", "ROLE_DEV", "ROLE_PVQ_EDIT", "ROLE_PVQ_VIEW", "ROLE_PVQ_ADMIN"]
    List ALLOWED_ASSIGNED_TO_ROLES_PVC = ["ROLE_ADMIN", "ROLE_DEV", "ROLE_PVC_VIEW", "ROLE_PVC_EDIT"]
    List ALLOWED_ASSIGNED_TO_ROLES_PVC_INB = ["ROLE_ADMIN", "ROLE_DEV", "ROLE_PVC_INBOUND_EDIT", "ROLE_PVC_INBOUND_VIEW"]

    Map validAuthCategories = [
            "Drug"       : ["Marketed Drug", "Investigational Drug", "市販薬", "治験薬"],
            "Device"     : ["Marketed Device", "Investigational Device", "市販用医療機器", "治験用医療機器"],
            "Vaccine"    : ["Marketed Vaccine", "Investigational Vaccine", "市販用ワクチン", "治験用ワクチン"],
            "Biological" : ["Marketed Biologic", "Investigational Biologic", "市販用生物製剤", "治験用生物製剤"]
    ]

    Map icsrSubmissionStatus = [
            "submissionNotRequired"     : "ICSRSubmissionNotRequired",
            "delete"                    : "ICSRMarkReportDeletion",
            "submit"                    : "ICSRSubmit"
    ]

}