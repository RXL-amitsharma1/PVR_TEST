package com.rxlogix

import com.rxlogix.admin.AdminIntegrationApiService
import com.rxlogix.config.*
import com.rxlogix.config.metadata.CaseColumnJoinMapping
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.config.publisher.PublisherCommonParameter
import com.rxlogix.dto.PrivacyProfileResponseDTO
import com.rxlogix.enums.*
import com.rxlogix.localization.LocalizationHelpMessage
import com.rxlogix.mapping.DataSourceInfo
import com.rxlogix.pvdictionary.ProductDictionaryMetadata
import com.rxlogix.user.*
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.Memoized
import groovyx.gpars.GParsPool
import oracle.sql.BLOB
import org.apache.commons.io.FileUtils
import com.rxlogix.localization.Localization
import org.apache.commons.io.filefilter.WildcardFileFilter
import org.apache.http.HttpStatus
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.joda.time.DateTimeZone
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.SQLException

class SeedDataService {
    static String USERNAME = "Application"
    static String DEFAULT_USER_GROUP_NAME = "All Users"
    static String DEFAULT_TENANT_NAME = "Default Tenant"
    String metadataLocation = "metadata"
    String externalized = "externalized_data.cfg"
    GrailsApplication grailsApplication
    def userService
    def CRUDService
    def importService
    def utilService
    def qualityService
    def executorThreadInfoService
    def queryService
    def executedConfigurationService
    def PVCMIntegrationService

    DataSource dataSource_pva
    DataSource dataSource

    AdminIntegrationApiService adminIntegrationApiService

    User getApplicationUserForSeeding() {
        String seedingUser = utilService.getApplicationUserForSeeding()
        return User.findByUsername(seedingUser)
    }

    /**
     * Utility method to dump some BLOB data from the RPT_RESULT_DATA table. In this case the VALUE field is JSON that's been gziped
     */
    def dumpBlobData(DataSource targetDatasource) {
        Sql sql = new Sql(targetDatasource)
        def rowData = sql.firstRow("SELECT VALUE FROM RPT_RESULT_DATA WHERE ID = 4930") // change the query to retrieve whatever you need
        BLOB blobData = (BLOB)rowData[0]
        InputStream blobStream = blobData.getBinaryStream()

        // Originally used to dump RPT_RESULT_DATA BLOBS, so working w/ GZIP'ed JSON
        try {
            if (blobStream != null) {
                String filePath = grailsApplication.config.tempDirectory as String
                File outputFile = File.createTempFile("blob_output", ".json.gz", new File(filePath))
                FileUtils.copyInputStreamToFile(blobStream, outputFile)
                println "Wrote ${outputFile.absolutePath}"
            }
        } finally {
            blobStream?.close()
            sql?.close()
        }

    }

    @ReadOnly
    void seedApplication() {
        seedDefaultTenant()
        seedRoles()
        seedUsers()
        seedCategoriesAndTags()
        seedAICategories()

        if (!ApplicationSettings.count()) {
            seedApplicationSettings()
        }
        setRunPriorityOnlyOnInstance()

        seedDefaultUserGroup()

        if (!EtlSchedule.count()) {
            seedETLSchedule()
        }

        if(!ReportRequestType.count()){
            seedReportRequestTypes()
        }

        if (!WorkflowState.count) {
            seedWorkflowStates()
        }

        if(!WorkflowRule.isDefaultWorkRuleExists()){
            seedWorkflowRules()
        }

        if (!SourceProfile.count()) {
            seedDefaultSourceProfiles()
        }

        if (!PublisherCommonParameter.count()) {
            seedPublisherCommonParameters()
        }

        if (!LocalizationHelpMessage.count()) {
            seedLocalizationHelpMessage()
        }

        seedPrivacyFieldProfile()
    }

    protected void seedLocalizationHelpMessage() {
        User adminUser = getApplicationUserForSeeding()
        def file = grailsApplication.config.help.json.file.name
        InputStream fis = file ? getInputStreamForMetadata("$file") : null
        if (fis) {
            try {
                def json = JSON.parse(fis.text)
                int imported = 0
                json.each {
                    Localization localization = Localization.findByCodeAndLocale(it.code, it.locale)

                    if (localization) {
                        if (!localization.helpMessage) {
                            localization.helpMessage = new LocalizationHelpMessage(message: it.help)
                            localization.save(flush: true, failOnError: true)
                            imported++
                        } else {
                            log.info("Help for  ${it.code} ${it.locale} already exists.")
                        }
                    } else {
                        log.info("Label for  ${it.code} ${it.locale} was not found.")
                    }
                }
                log.info("Imported  ${imported} help messages.")
            }
            finally {
                fis?.close()
            }
        }
    }

    void seedPublisherCommonParameters() {
        log.info("Seeding Publisher Common Parameters........")
        CRUDService.save(new PublisherCommonParameter(name: "dateRangeStartAbsolute", value: "\$eval \$report.configuration.executedGlobalDateRangeInformation.dateRangeStartAbsolute.format(\"dd-MMM-yyyy\")"))
        CRUDService.save(new PublisherCommonParameter(name: "dateRangeEndAbsolute", value: "\$eval \$report.configuration.executedGlobalDateRangeInformation.dateRangeEndAbsolute.format(\"dd-MMM-yyyy\")"))
        CRUDService.save(new PublisherCommonParameter(name: "runDate", value: "\$eval \$report.configuration.lastRunDate.format(\"dd-MMM-yyyy\")"))
        CRUDService.save(new PublisherCommonParameter(name: "version", value: "\$eval \$report.configuration.numOfExecutions"))
        CRUDService.save(new PublisherCommonParameter(name: "primaryReportingDestination", value: "\$eval \$report.configuration.primaryReportingDestination"))
        CRUDService.save(new PublisherCommonParameter(name: "reportingDestinations", value: "\$eval \$report.configuration.reportingDestinations?.join(\", \")"))
        CRUDService.save(new PublisherCommonParameter(name: "products", value: "\$eval  \$report.configuration.productsString?:\"no\""))
        CRUDService.save(new PublisherCommonParameter(name: "study", value: "\$eval  \$report.configuration.studiesString?:\"no\""))
        CRUDService.save(new PublisherCommonParameter(name: "currentDate", value: "\$eval (new Date()).format(\"dd-MMM-yyyy\")"))
    }

    void seedSourceProfileCaseNumberFieldNames(){
        log.info("Seeding Source Profile case number variables........")
        SourceProfile.list().each {
            it.caseNumberFieldName = DataSourceInfo.'pva'.findBySourceAbbrev(it.sourceAbbrev)?.caseNumberFieldName ?: "masterCaseNum"
            CRUDService.update(GrailsHibernateUtil.unwrapIfProxy(it))
        }
    }

    // Its a method which is getting called for refresh cache as well so all cache related things should be here only
    void seedPVAData() {
        seedTenants()
        seedDateRangeType()
        seedMetadata()
        seedReportFieldLocalizations(null , null , null)
        seedPVDictionaryLocalications()
        seedSourceProfileTable()
        seedSourceProfileCaseNumberFieldNames()
        if (grailsApplication.config.show.xml.option) {
            seedIcsrOrganizationType()
        }
    }

    // seedApplicationTemplatesQueriesAndOtherData method would be use to put down application specific things not part of refresh cache.
    void seedApplicationTemplatesQueriesAndOtherData(){
        seedSuccessDirectory()
        if (SourceProfile.count() && !UserGroup.first()?.sourceProfiles?.size()) {
            seedUserGroupSourceProfile()
        }
        seedTemplates()
        seedQueries()
        if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
            PVCMIntegrationService.invokeRoutingConditionAPI()
        }
        seedConfiguration()
        seedDashboards()
    }

    void seedSuccessDirectory(){
        def successDir = Paths.get("${grailsApplication.config.externalDirectory}/success")
        if (Files.notExists(successDir)) {
            Files.createDirectories(successDir)
        }
    }

    private void seedDefaultSourceProfiles() {
        log.info("Seeding default Source profiles...")
        try {
            SourceProfile allSource = new SourceProfile(sourceId: 0, sourceName: 'All', sourceAbbrev: 'ALL', sourceProfileTypeEnum: SourceProfileTypeEnum.ALL, isCentral: false, isDeleted: false)
            CRUDService.save(GrailsHibernateUtil.unwrapIfProxy(allSource))
            //Choosing sourceId as 1 for Central source
            SourceProfile centralSource = new SourceProfile(sourceId: 1, sourceName: 'Argus', sourceAbbrev: 'ARG', sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE, isCentral: true, isDeleted: false)
            CRUDService.save(GrailsHibernateUtil.unwrapIfProxy(centralSource))
        } catch (ValidationException ve) {
            log.warn("Validation error while saving default source profiles")
        }
    }

// ====================  Seed user and roles ==========================
    protected void seedUsers() {
        Tenant defaulTenant = Tenant.read(Holders.config.getProperty('pvreports.multiTenancy.defaultTenant', Long))
        (readPvrExtConfig().grails.pvreports.users as List).each {
            userService.createUser (it.username, new Preference(locale: new Locale('en'), timeZone: it.timeZone ?: DateTimeZone.UTC.ID, createdBy: USERNAME, modifiedBy: USERNAME), it.roles, USERNAME, (it.tenantId ? Tenant.read(it.tenantId as Long) : defaulTenant ), UserType.valueOf(it.type ?: 'LDAP'), it.fullName ?: '')
        }
    }

    void seedDefaultTenant() {
        Long defaultTenantId = Holders.config.getProperty('pvreports.multiTenancy.defaultTenant', Long)
        if (Tenant.read(defaultTenantId)) {
            return
        }
        Tenant tenant = new Tenant(name: DEFAULT_TENANT_NAME, active: false)
        tenant.id = Holders.config.getProperty('pvreports.multiTenancy.defaultTenant', Long)
        tenant.save(flush:true)
    }

    void seedDefaultUserGroup() {
        (readPvrExtConfig().grails.pvreports.userGroups as List).each {
            new UserGroup(name: it.name, createdBy: it.createdBy, modifiedBy: it.modifiedBy).save()
        }
    }

    void seedRoles() {

        def roles = [
                [authority  : 'ROLE_ADMIN',
                 description: "Users/Groups with Administrator role can perform all operations except User/Group Management, Quality Check, Share with Users/Groups and other system administrative Operations like Application/Jobs Monitoring and custom field management"],
                [authority  : 'ROLE_DEV',
                 description: "Users/Groups with Super administrator role can perform system administrator operations like Application/Jobs Monitoring, Custom Field Management, Extract JSONs and other business operations except Quality Check and  Share with Users/Groups operations"],

                //Periodic Configurations -----------------------------------------------------------------
                [authority  : 'ROLE_PERIODIC_CONFIGURATION_CRUD',
                 description: "Create, read, update, and delete Periodic Configurations"],

                [authority  : 'ROLE_PERIODIC_CONFIGURATION_VIEW',
                 description: "View Periodic Configurations only"],


                //Configurations -----------------------------------------------------------------
                [authority  : 'ROLE_CONFIGURATION_CRUD',
                 description: "Create, read, update, and delete Configurations"],
                [authority  : 'ROLE_CONFIGURATION_VIEW',
                 description: "View Configurations only"],
                [authority  : 'ROLE_BQA_EDITOR',
                 description: "Can define QBE reports"],
                //System configuration
                [authority  : 'ROLE_SYSTEM_CONFIGURATION',
                 description: "Users/Groups with this role have access to Settings menu to general business configurations like Workflow State, Rules, Dashboards and Email configuration etc. Users/Groups with only System configuration role restricted to perform System Administrative and User/Group Management operations"],
                [authority  : 'ROLE_CONFIG_TMPLT_CREATOR',
                 description: "Configuration Templates Creator; provides access to create and edit configuration templates. "],

                //Templates ----------------------------------------------------------------------
                [authority  : 'ROLE_TEMPLATE_ADVANCED',
                 description: "Advanced Templates features"],
                [authority  : 'ROLE_TEMPLATE_CRUD',
                 description: "Create, read, update and delete CLL and DT Templates"],
                [authority  : 'ROLE_TEMPLATE_VIEW',
                 description: "View CLL and DT Templates"],
                [authority  : 'ROLE_TEMPLATE_SET_CRUD',
                 description: "Create, read, update and delete Templates Set"],
                [authority  : 'ROLE_TEMPLATE_SET_VIEW',
                 description: "View CLL and DT Templates Set"],

                //Queries ------------------------------------------------------------------------
                [authority  : 'ROLE_QUERY_ADVANCED',
                 description: "Advanced Query features"],
                [authority  : 'ROLE_QUERY_CRUD',
                 description: "Create, read, update and delete Queries"],
                [authority  : 'ROLE_QUERY_VIEW',
                 description: "View Queries only"],

                //Data Analysis  -----------------------------------------------------------------
                [authority  : 'ROLE_DATA_ANALYSIS',
                 description: "Access to Data Analysis information"],

                //Cognos Report ----------------------------------------------------------------------
                [authority  : 'ROLE_COGNOS_CRUD',
                 description: "Create, read, update and delete Cognos Reports"],
                [authority  : 'ROLE_COGNOS_VIEW',
                 description: "View Cognos Reports only"],

                // Quality Check -----------------------------------------------------------------
                [authority  : 'ROLE_QUALITY_CHECK',
                 description: "Ability to Quality Checked the Templates, Queries, Periodic and Adhoc Reports"],

                // Case Series -----------------------------------------------------------------
                [authority  : 'ROLE_CASE_SERIES_CRUD',
                 description: "Create, read, update, and delete Case Series"],
                [authority  : 'ROLE_CASE_SERIES_EDIT',
                 description: "Add and remove cases to a Case Series"],
                [authority  : 'ROLE_CASE_SERIES_VIEW',
                 description: "View Case Series only"],

                // Action Item -----------------------------------------------------------------
                [authority  : 'ROLE_ACTION_ITEM',
                 description: "Create and assign Action Items to users"],

                // Report Request -----------------------------------------------------------------
                [authority  : 'ROLE_REPORT_REQUEST_CRUD',
                 description: "Create, read, update, and delete Report Request"],
                [authority  : 'ROLE_REPORT_REQUEST_VIEW',
                 description: "View Report Request only"],
                [authority  : 'ROLE_REPORT_REQUEST_PLAN_VIEW',
                 description: "View Report Request plan only"],
                [authority  : 'ROLE_REPORT_REQUEST_PLANNING_TEAM',
                 description: "View and Edit additional fields in Report Request"],
                [authority  : 'ROLE_REPORT_REQUEST_ASSIGN',
                 description: "Can Assign Report Request"],
                // Chart Template Editor -----------------------------------------------------------------
                [authority  : 'ROLE_CHART_TEMPLATE_EDITOR',
                 description: "Customize the charts for data tabulations"],

                // Calendar -----------------------------------------------------------------
                [authority  : 'ROLE_CALENDAR',
                 description: "View Calendar"],

                // Share With -----------------------------------------------------------------
                [authority  : 'ROLE_SHARE_GROUP',
                 description: "Share templates/queries/reports  within group "],
                [authority  : 'ROLE_SHARE_ALL',
                 description: "Share templates/queries/reports with all users"],

                // PVQ -----------------------------------------------------------------
                [authority  : 'ROLE_PVQ_EDIT',
                 description: "Create and run PVQ reports, edit errors lists"],
                [authority  : 'ROLE_PVQ_VIEW',
                 description: "View PVQ errors lists"],

                // PVC -----------------------------------------------------------------
                [authority  : 'ROLE_PVC_EDIT',
                 description: "Create and run PVC reports, edit errors lists"],
                [authority  : 'ROLE_PVC_VIEW',
                 description: "View PVC  errors lists"],
                [authority : 'ROLE_USER_GROUP_RCA',
                 description: "Restrict access for assigning RCA for Reason of Delay records"],
                [authority : 'ROLE_USER_GROUP_RCA_PVQ',
                 description: "Restrict access for assigning RCA for Quality records"],


                // Custom Expression -----------------------------------------------------------------
                [authority  : 'ROLE_CUSTOM_EXPRESSION',
                 description: "Has access to Custom Expressions in New/Edit Template page"],

                // Custom Field -----------------------------------------------------------------
                [authority  : 'ROLE_CUSTOM_FIELD',
                 description: "Has access to Custom Field dictionary"],

                // ICSR Reports -----------------------------------------------------------------
                [authority  : 'ROLE_ICSR_REPORTS_EDITOR',
                 description: "Can view, create, read, update, and delete  ICSR Reports"],
                [authority  : 'ROLE_ICSR_REPORTS_VIEWER',
                 description: "View ICSR Reports only"],

                // ICSR Profile -----------------------------------------------------------------
                [authority  : 'ROLE_ICSR_PROFILE_EDITOR',
                 description: "Can view, create, read, update, and delete  ICSR Profile"],
                [authority  : 'ROLE_ICSR_PROFILE_VIEWER',
                 description: "View ICSR Profile only"],

                // User manager -----------------------------------------------------------------
                [authority  : 'ROLE_USER_MANAGER',
                 description: "Can manage users rights"],
                // DMS upload -----------------------------------------------------------------
                [authority  : 'ROLE_DMS',
                 description: "Can upload to DMS"],

                //PVP
                [authority: 'ROLE_PUBLISHER_TEMPLATE_EDITOR',
                 description:" Upload a Publisher Template, define the template parameters and make edits."],
                [authority: 'ROLE_PUBLISHER_SECTION_EDITOR',
                 description: "Modify the status of a Publisher section"],
                [authority: 'ROLE_TEMPLATE_LIBRARY_ACCESS',
                 description: "Ability to access the template library screen."],
                [authority: 'ROLE_PUBLISHER_TEMPLATE_VIEWER',
                 description: "Ability to view Publisher Template, template parameters, publisher source and common parameters."],
                [authority: 'ROLE_DOCUMENT_AUTHOR',
                 description: "Ability to author publisher document."],
                [authority: 'ROLE_DOCUMENT_REVIEWER',
                 description: "Ability to review publisher document."],
                [authority: 'ROLE_DOCUMENT_APPROVER',
                 description: "Ability to approve publisher document."],
                // Priority report -----------------------------------------------------------------
                [authority  : 'ROLE_RUN_PRIORITY_RPT',
                 description: "Has access to run the priority report"],

                //PVC Inbound Compliance Role
                [authority : 'ROLE_PVC_INBOUND_EDIT',
                 description: "Can create, view, update, delete and execute Inbound Compliance"],
                [authority  : 'ROLE_PVC_INBOUND_VIEW',
                 description: "View PVC Inbound Compliance"],

                //ICSR Tracking Role
                [authority  : "ROLE_ICSR_DISTRIBUTION",
                 description: "User can perform actions in ICSR Case Tracking"],
                [authority  : "ROLE_ICSR_DISTRIBUTION_ADMIN",
                 description: "User can perform additional actions in ICSR Case Tracking"]

        ]

        roles.each {
            new Role(authority: it.authority, description: it.description,
                    createdBy: USERNAME, modifiedBy: USERNAME).save(flush:true)
        }
    }

    void seedReportRequestTypes() {
        ReportRequestTypeEnum.values().each {
            new ReportRequestType(name: it.value, createdBy: USERNAME, modifiedBy: USERNAME).save()
        }
    }

    void seedWorkflowStates() {
        if (!WorkflowState.findByName(WorkflowState.NEW_NAME)) {
            WorkflowState defaultNewState = new WorkflowState(name: WorkflowState.NEW_NAME, description: "Default New State", createdBy: USERNAME, modifiedBy: USERNAME)
            ReportActionEnum.values().each {
                WorkflowStateReportAction action = new  WorkflowStateReportAction(reportAction:it)
                defaultNewState.addToReportActions(action)
            }
            defaultNewState.save()
        }
        if (!WorkflowState.findByName(WorkflowState.UNDER_REVIEW_NAME)) {
            WorkflowState defaultUnderReviewState = new WorkflowState(name: WorkflowState.UNDER_REVIEW_NAME, description: "Default Under Review", createdBy: USERNAME, modifiedBy: USERNAME)
            defaultUnderReviewState.save()
        }
        if (!WorkflowState.findByName(WorkflowState.REVIEWED_NAME)) {
            WorkflowState defaultReviewedState = new WorkflowState(name: WorkflowState.REVIEWED_NAME, description: "Default Reviewed", createdBy: USERNAME, modifiedBy: USERNAME)
            defaultReviewedState.save()
        }
        if (!WorkflowState.findByName(WorkflowState.INPROGRESS_NAME)) {
            WorkflowState defaultCompleteState = new WorkflowState(name: WorkflowState.INPROGRESS_NAME, description: WorkflowState.INPROGRESS_NAME, createdBy: USERNAME, modifiedBy: USERNAME)
            defaultCompleteState.save()
        }
        if (!WorkflowState.findByName(WorkflowState.NEEDCLARIFICATION_NAME)) {
            WorkflowState defaultCompleteState = new WorkflowState(name: WorkflowState.NEEDCLARIFICATION_NAME, description: WorkflowState.NEEDCLARIFICATION_NAME, createdBy: USERNAME, modifiedBy: USERNAME)
            defaultCompleteState.save()
        }
        if (!WorkflowState.findByName(WorkflowState.CLOSED_NAME)) {
            WorkflowState defaultCompleteState = new WorkflowState(name: WorkflowState.CLOSED_NAME, description: WorkflowState.CLOSED_NAME, createdBy: USERNAME, modifiedBy: USERNAME)
            defaultCompleteState.save()
        }
        if (!WorkflowState.findByName(WorkflowState.COMPLETE_NAME)) {
            WorkflowState defaultCompleteState = new WorkflowState(name: WorkflowState.COMPLETE_NAME, description: WorkflowState.COMPLETE_NAME, createdBy: USERNAME, modifiedBy: USERNAME)
            defaultCompleteState.save()
        }
        if (!WorkflowState.findByName(WorkflowState.REOPEN_NAME)) {
            WorkflowState defaultCompleteState = new WorkflowState(name: WorkflowState.REOPEN_NAME, description: WorkflowState.REOPEN_NAME, createdBy: USERNAME, modifiedBy: USERNAME)
            defaultCompleteState.save()
        }
        if (!WorkflowState.findByName(WorkflowState.TRIGGER)) {
            WorkflowState defaultTriggerState = new WorkflowState(name: WorkflowState.TRIGGER, description: WorkflowState.TRIGGER, createdBy: USERNAME, modifiedBy: USERNAME)
            defaultTriggerState.save()
        }
    }

    void seedWorkflowRules() {
        if (!WorkflowRule.findByName(WorkflowRule.NEW_TO_UNDER_REVIEW)) {
            WorkflowRule newToUnderRev = new WorkflowRule(name: WorkflowRule.NEW_TO_UNDER_REVIEW, description: "Transition from New to Under Review State", initialState: WorkflowState.findByName(WorkflowState.NEW_NAME), targetState: WorkflowState.findByName(WorkflowState.UNDER_REVIEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT, createdBy: USERNAME, modifiedBy: USERNAME)
            newToUnderRev.save()
        }
        if (!WorkflowRule.findByName(WorkflowRule.UNDER_REVIEW_TO_REVIEWED)) {
            WorkflowRule underRevToRev = new WorkflowRule(name: WorkflowRule.UNDER_REVIEW_TO_REVIEWED, description: "Transition from Under Review State to Reviewd state", initialState: WorkflowState.findByName(WorkflowState.UNDER_REVIEW_NAME), targetState: WorkflowState.findByName(WorkflowState.REVIEWED_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT, createdBy: USERNAME, modifiedBy: USERNAME)
            underRevToRev.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.REPORT_REQUEST)) {
            WorkflowRule state = new WorkflowRule(name: WorkflowRule.NEW_TO_INPROGRESS, description: WorkflowRule.NEW_TO_INPROGRESS, initialState: WorkflowState.findByName(WorkflowState.NEW_NAME), targetState: WorkflowState.findByName(WorkflowState.INPROGRESS_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.NEW_TO_NEEDCLARIFICATION, description: WorkflowRule.NEW_TO_NEEDCLARIFICATION, initialState: WorkflowState.findByName(WorkflowState.NEW_NAME), targetState: WorkflowState.findByName(WorkflowState.NEEDCLARIFICATION_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.INPROGRESS_TO_NEEDCLARIFICATION, description: WorkflowRule.INPROGRESS_TO_NEEDCLARIFICATION, initialState: WorkflowState.findByName(WorkflowState.INPROGRESS_NAME), targetState: WorkflowState.findByName(WorkflowState.NEEDCLARIFICATION_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.INPROGRESS_TO_COMPLETE, description: WorkflowRule.INPROGRESS_TO_COMPLETE, initialState: WorkflowState.findByName(WorkflowState.INPROGRESS_NAME), targetState: WorkflowState.findByName(WorkflowState.COMPLETE_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.NEEDCLARIFICATION_TO_INPROGRESS, description: WorkflowRule.NEEDCLARIFICATION_TO_INPROGRESS, initialState: WorkflowState.findByName(WorkflowState.NEEDCLARIFICATION_NAME), targetState: WorkflowState.findByName(WorkflowState.INPROGRESS_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.COMPLETE_TO_CLOSE, description: WorkflowRule.COMPLETE_TO_CLOSE, initialState: WorkflowState.findByName(WorkflowState.COMPLETE_NAME), targetState: WorkflowState.findByName(WorkflowState.CLOSED_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.COMPLETE_TO_REOPEN, description: WorkflowRule.COMPLETE_TO_REOPEN, initialState: WorkflowState.findByName(WorkflowState.COMPLETE_NAME), targetState: WorkflowState.findByName(WorkflowState.REOPEN_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.REOPEN_TO_INPROGRESS, description: WorkflowRule.REOPEN_TO_INPROGRESS, initialState: WorkflowState.findByName(WorkflowState.REOPEN_NAME), targetState: WorkflowState.findByName(WorkflowState.INPROGRESS_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.REOPEN_TO_NEEDCLARIFICATION, description: WorkflowRule.REOPEN_TO_NEEDCLARIFICATION, initialState: WorkflowState.findByName(WorkflowState.REOPEN_NAME), targetState: WorkflowState.findByName(WorkflowState.NEEDCLARIFICATION_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.PUBLISHER_SECTION)) {
            WorkflowRule state = new WorkflowRule(name: WorkflowRule.NEW_TO_UNDER_REVIEW, description: WorkflowRule.NEW_TO_UNDER_REVIEW, initialState: WorkflowState.findByName(WorkflowState.NEW_NAME), targetState: WorkflowState.findByName(WorkflowState.UNDER_REVIEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.PUBLISHER_SECTION, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.UNDER_REVIEW_TO_REVIEWED, description: WorkflowRule.UNDER_REVIEW_TO_REVIEWED, initialState: WorkflowState.findByName(WorkflowState.UNDER_REVIEW_NAME), targetState: WorkflowState.findByName(WorkflowState.REVIEWED_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.PUBLISHER_SECTION, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.PUBLISHER_FULL)) {
            WorkflowRule state = new WorkflowRule(name: WorkflowRule.NEW_TO_UNDER_REVIEW, description: WorkflowRule.NEW_TO_UNDER_REVIEW, initialState: WorkflowState.findByName(WorkflowState.NEW_NAME), targetState: WorkflowState.findByName(WorkflowState.UNDER_REVIEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.PUBLISHER_FULL, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
            state = new WorkflowRule(name: WorkflowRule.UNDER_REVIEW_TO_REVIEWED, description: WorkflowRule.UNDER_REVIEW_TO_REVIEWED, initialState: WorkflowState.findByName(WorkflowState.UNDER_REVIEW_NAME), targetState: WorkflowState.findByName(WorkflowState.REVIEWED_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.PUBLISHER_FULL, createdBy: USERNAME, modifiedBy: USERNAME)
            state.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA)) {
            WorkflowRule newToTrigger = new WorkflowRule(name: WorkflowRule.CASE_DATA_TRIGGER_TO_NEW, description: "Quality Case Data : Transition from Trigger to New", initialState: WorkflowState.findByName(WorkflowState.TRIGGER), targetState: WorkflowState.findByName(WorkflowState.NEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA, createdBy: USERNAME, modifiedBy: USERNAME)
            newToTrigger.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.QUALITY_SAMPLING)) {
            WorkflowRule newToTrigger = new WorkflowRule(name: WorkflowRule.SAMPLING_TRIGGER_TO_NEW, description: "Other Quality Type #1 : Transition from Trigger to New", initialState: WorkflowState.findByName(WorkflowState.TRIGGER), targetState: WorkflowState.findByName(WorkflowState.NEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.QUALITY_SAMPLING, createdBy: USERNAME, modifiedBy: USERNAME)
            newToTrigger.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.QUALITY_SAMPLING2)) {
            WorkflowRule newToTrigger = new WorkflowRule(name: WorkflowRule.SAMPLING2_TRIGGER_TO_NEW, description: "Other Quality Type #2 : Transition from Trigger to New", initialState: WorkflowState.findByName(WorkflowState.TRIGGER), targetState: WorkflowState.findByName(WorkflowState.NEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.QUALITY_SAMPLING2, createdBy: USERNAME, modifiedBy: USERNAME)
            newToTrigger.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.QUALITY_SAMPLING3)) {
            WorkflowRule newToTrigger = new WorkflowRule(name: WorkflowRule.SAMPLING3_TRIGGER_TO_NEW, description: "Other Quality Type #3 : Transition from Trigger to New", initialState: WorkflowState.findByName(WorkflowState.TRIGGER), targetState: WorkflowState.findByName(WorkflowState.NEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.QUALITY_SAMPLING3, createdBy: USERNAME, modifiedBy: USERNAME)
            newToTrigger.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION)) {
            WorkflowRule newToTrigger = new WorkflowRule(name: WorkflowRule.SUBMISSION_TRIGGER_TO_NEW, description: "Quality Submission : Transition from Trigger to New", initialState: WorkflowState.findByName(WorkflowState.TRIGGER), targetState: WorkflowState.findByName(WorkflowState.NEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION, createdBy: USERNAME, modifiedBy: USERNAME)
            newToTrigger.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY)) {
            WorkflowRule newToTrigger = new WorkflowRule(name: WorkflowRule.PVC_TRIGGER_TO_NEW, description: "PVC : Transition from Trigger to New", initialState: WorkflowState.findByName(WorkflowState.TRIGGER), targetState: WorkflowState.findByName(WorkflowState.NEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY, createdBy: USERNAME, modifiedBy: USERNAME)
            newToTrigger.save()
        }
        if (!WorkflowRule.findByConfigurationTypeEnum(WorkflowConfigurationTypeEnum.PVC_INBOUND)) {
            WorkflowRule newToTrigger = new WorkflowRule(name: WorkflowRule.PVC_INBOUND_TRIGGER_TO_NEW, description: "PVC Inbound : Transition from Trigger to New", initialState: WorkflowState.findByName(WorkflowState.TRIGGER), targetState: WorkflowState.findByName(WorkflowState.NEW_NAME), configurationTypeEnum: WorkflowConfigurationTypeEnum.PVC_INBOUND, createdBy: USERNAME, modifiedBy: USERNAME)
            newToTrigger.save()
        }
    }

    // ========================== Seed Metadata ======================
    void seedMetadata() {
        seedSourceTableMasterMetaTable(null , null , null)
        seedArgusCaseColumnJoinMetaTable()
        seedSourceColumnMasterMetaTable(null , null , null)
        seedReportFieldGroupsMetaTable()
        seedReportFieldsMetaTable(null , null , null)
    }

    void seedPVDictionaryLocalications() {
        log.info("Seeding PVDictionary localizations........")
        try {
            ProductDictionaryMetadata.i18nValues.each {
                String lang = it.key
                it.value.each {
                    def existingRecord = Localization.findByCodeAndLocale(it.key, lang)
                    if (existingRecord) {
                        if (!existingRecord.text.equals(it.value)) {
                            existingRecord.text = it.value
                            CRUDService.updateWithoutAuditLog(existingRecord)
                        }
                    } else {
                        def localizationsObj = new Localization(
                                version: 0,
                                locale: lang,
                                dateCreated: new Date(),
                                lastUpdated: new Date(),
                                relevance: lang?.length(),
                                code: it.key,
                                text: it.value
                        )
                        CRUDService.saveWithoutAuditLog(localizationsObj)
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception caught in seeding PVDictionary localizations", e)
        }
        log.info("Seeding PVDictionary localizations completed........")
    }

    synchronized void seedReportFieldLocalizations(String uniqueFieldId = null , Long tenantId , String langId) {
        String reportFieldLocalizationQuery = "SELECT * FROM PVR_RPT_FIELD_LABEL"
        if(!uniqueFieldId.equals(null) && tenantId!=null && langId!=null){
            reportFieldLocalizationQuery = reportFieldLocalizationQuery.concat(" WHERE UNIQUE_FIELD_ID = '${uniqueFieldId}' AND TENANT_ID = ${tenantId}")
        }

        log.info("Seeding ReportField localizations........")
        GParsPool.withPool(seedingThreadsSize, new Thread.UncaughtExceptionHandler() {
            @Override
            void uncaughtException(Thread failedThread, Throwable throwable) {
                log.error("Exception caught in seeding ReportField localizations for ${failedThread.name}", throwable)
                if (!(throwable instanceof SQLException)) {
                    throw new RuntimeException("Stopped further due to unknown failure")
                }
            }
        }, {
            Sql sql = new Sql(dataSource_pva)
            Sql pvrSql = new Sql(dataSource)
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { statement ->
                statement.fetchSize = 300
            }
            try {
                if (!uniqueFieldId && grailsApplication.config.getProperty('pvr.report.field.localization.delete', Boolean)) {
                    Localization.withNewTransaction {
                        pvrSql.execute("delete from localization where code like ('app.reportField.%')")
                    }
                }
                //Now read the localizations from the PVA database.
                List<GroovyRowResult> reportFieldLocalizationList = sql.rows(reportFieldLocalizationQuery)
                if (reportFieldLocalizationList) {
                    reportFieldLocalizationList.eachParallel { row ->
                        Localization.withTransaction {
                            def existingRecord = Localization.findByCodeAndLocale(row.CODE, row.LOC)
                            if (existingRecord) {
                                boolean override = true
                                def list = existingRecord.code.split('[.]', 3)
                                if (list && list.size() == 3) {
                                    ReportField reportField = ReportField.findByName(list[2])
                                    override = reportField ? reportField.override : true
                                }
                                if (override && !existingRecord.text.equals(row.TEXT) && existingRecord.lock()) {
                                    existingRecord.text = row.TEXT
                                    CRUDService.updateWithoutAuditLog(existingRecord)
                                }
                            } else {
                                def localizationsObj = new Localization(
                                        version: 0,
                                        locale: row.LOC,
                                        dateCreated: new Date(),
                                        lastUpdated: new Date(),
                                        relevance: row.LOC?.length(),
                                        code: row.CODE,
                                        text: row.TEXT
                                )
                                CRUDService.saveWithoutAuditLog(localizationsObj)
                            }
                        }
                    }
                } else {
                    if (uniqueFieldId) {
                        log.error("No Row Present in Localization for the mentioned unique field Id -> ${uniqueFieldId}")
                    }
                }
            } finally {
                sql.close()
            }
        })

        log.info("Seeding ReportsField localizations completed........")
    }

    synchronized void seedReportFieldsMetaTable(String uniqueFieldId, Long tenantId, String langId) {
        String reportFieldQuery = "SELECT * FROM RPT_FIELD"
        if (!uniqueFieldId.equals(null) && !tenantId.equals(null) && !langId.equals(null)) {
            reportFieldQuery = reportFieldQuery.concat(" WHERE UNIQUE_FIELD_ID = '${uniqueFieldId}' AND TENANT_ID=${tenantId}")
        }

        log.info("Seeding ReportField table........")
        GParsPool.withPool(seedingThreadsSize, new Thread.UncaughtExceptionHandler() {
            @Override
            void uncaughtException(Thread failedThread, Throwable throwable) {
                log.error("Exception caught in seeding ReportField for ${failedThread.name}", throwable)
                if (!(throwable instanceof SQLException)) {
                    throw new RuntimeException("Stopped further due to unknown failure")
                }
            }
        }, {
            Sql sql = new Sql(dataSource_pva)
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { statement ->
                statement.fetchSize = 300
            }
            try {
                List<GroovyRowResult> reportFieldList = sql.rows(reportFieldQuery)
                if (reportFieldList) {
                    reportFieldList.eachParallel { row ->
                        ReportField.withTransaction {
                            def existingRecord = ReportField.findByName(row.NAME)

                            def newRecord = new ReportField(
                                    name: row.NAME,
                                    description: row.DESCRIPTION,
                                    transform: row.TRANSFORM,
                                    fieldGroup: ReportFieldGroup.get(row.RPT_FIELD_GRPNAME),
                                    sourceColumnId: row.SOURCE_COLUMN_MASTER_ID,
                                    dataType: Class.forName(row.DATA_TYPE),
                                    isText: row.IS_TEXT?.asBoolean(),
                                    listDomainClass: row.LIST_DOMAIN_CLASS ? Class.forName((row.LIST_DOMAIN_CLASS).trim()) : null,
                                    lmSQL: row.LMSQL,
                                    querySelectable: row.QUERY_SELECTABLE?.asBoolean(),
                                    templateCLLSelectable: row.TEMPLT_CLL_SELECTABLE?.asBoolean(),
                                    templateDTRowSelectable: row.TEMPLT_DTROW_SELECTABLE?.asBoolean(),
                                    templateDTColumnSelectable: row.TEMPLT_DTCOL_SELECTABLE?.asBoolean(),
                                    isDeleted: row.IS_DELETED?.asBoolean(),
                                    dateFormatCode: row.DATE_FORMAT,
                                    dictionaryType: row.DIC_TYPE,
                                    dictionaryLevel: row.DIC_LEVEL,
                                    isAutocomplete: row.ISAUTOCOMPLETE ? row.ISAUTOCOMPLETE : 0,
                                    preQueryProcedure: row.PRE_QUERY_PROCEDURE,
                                    postQueryProcedure: row.POST_QUERY_PROCEDURE,
                                    preReportProcedure: row.PRE_REPORT_PROCEDURE,
                                    fixedWidth: row.FIXED_WIDTH,
                                    widthProportionIndex: row.WIDTH_PROPORTION_INDEX,
                                    sourceId: row.SOURCE_ID,
                                    isUrlField: row.IS_URL_FIELD ? row.IS_URL_FIELD : 0
                            )
                            if (existingRecord) {

                                //check to see if it needs to be updated
                                if (existingRecord.override && !existingRecord.equals(newRecord) && existingRecord.lock()) {
                                    ReportField.copyObj(newRecord, existingRecord)
                                    CRUDService.updateWithoutAuditLog(existingRecord)
                                }

                            } else {
                                CRUDService.saveWithoutAuditLog(newRecord)
                            }
                        }
                    }
                } else {
                    if (uniqueFieldId) {
                        log.error("No Row Present in RPT_FIELD for the mentioned unique field Id -> ${uniqueFieldId}")
                    }
                }
            } finally {
                sql?.close()
            }
        })
        log.info("Seeding ReportsField Completed........")
    }


    void seedReportFieldGroupsMetaTable() {

        Sql sql = new Sql(dataSource_pva)
        sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
        sql.withStatement { statement->
            statement.fetchSize = 300
        }
        try {

            sql.rows("SELECT * from RPT_FIELD_GROUP").each {
                def colData = ReportFieldGroup.findByName(it.NAME)
                def newVal = new ReportFieldGroup(
                        name: it.NAME,
                        isDeleted: it.IS_DELETED?.asBoolean(),
                        priority: it.RANK_ID
                )

                if (colData) {
                    if (!colData.equals(newVal)) {
                        colData.isDeleted = newVal.isDeleted
                        colData.priority = newVal.priority
                        CRUDService.updateWithoutAuditLog(colData)
                    }
                } else {
                    CRUDService.save(newVal)
                }
            }
        } catch (SQLException e) {
            log.error("Exception caught in seeding Report Field Group", e)
        } finally {
            sql?.close()
        }
    }


    def seedArgusCaseColumnJoinMetaTable() {
        GParsPool.withPool(seedingThreadsSize, new Thread.UncaughtExceptionHandler() {
            @Override
            void uncaughtException(Thread failedThread, Throwable throwable) {
                log.error("Exception caught in seeding Source Table Master for ${failedThread.name}", throwable)
                if (!(throwable instanceof SQLException)) {
                    throw new RuntimeException("Stopped further due to unknown failure")
                }
            }
        }, {
            Sql sql = new Sql(dataSource_pva)
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { statement ->
                statement.fetchSize = 300
            }
            try {
                sql.rows("SELECT * from CASE_COLUMN_JOIN_MAPPING").eachParallel { row ->
                    CaseColumnJoinMapping.withTransaction {
                        def colData = CaseColumnJoinMapping.findByTableNameAndColumnNameAndMapColumnNameAndMapTableName(SourceTableMaster.get(row.TABLE_NAME_ATM_ID), row.COLUMN_NAME,
                                row.MAP_COLUMN_NAME, SourceTableMaster.get(row.MAP_TABLE_NAME_ATM_ID))
                        def newVal = new CaseColumnJoinMapping(tableName: SourceTableMaster.get(row.TABLE_NAME_ATM_ID), columnName: row.COLUMN_NAME,
                                mapTableName: SourceTableMaster.get(row.MAP_TABLE_NAME_ATM_ID), mapColumnName: row.MAP_COLUMN_NAME, isDeleted: row.IS_DELETED?.asBoolean())

                        if (colData) {
                            if (!colData.equals(newVal) && colData.lock()) {
                                colData.isDeleted = newVal.isDeleted
                                CRUDService.updateWithoutAuditLog(colData)
                            }
                        } else {
                            CRUDService.save(newVal)
                        }
                    }
                }
            } finally {
                sql?.close()
            }

        })
    }


    synchronized void seedSourceColumnMasterMetaTable(String uniqueFieldId, Long tenantId, String langId) {
        log.info("Inside Source Column Master")
        String sourceColumnQuery = "SELECT * FROM SOURCE_COLUMN_MASTER"
        if (!uniqueFieldId.equals(null) && !tenantId.equals(null) && !langId.equals(null)) {
            sourceColumnQuery = sourceColumnQuery.concat(" WHERE UNIQUE_FIELD_ID = '${uniqueFieldId}' AND TENANT_ID = ${tenantId}")
        }

        GParsPool.withPool(seedingThreadsSize, new Thread.UncaughtExceptionHandler() {
            @Override
            void uncaughtException(Thread failedThread, Throwable throwable) {
                log.error("Exception caught in seeding Source Table Master for ${failedThread.name}", throwable)
                if (!(throwable instanceof SQLException)) {
                    throw new RuntimeException("Stopped further due to unknown failure")
                }
            }
        }, {
            Sql sql = new Sql(dataSource_pva)
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { statement ->
                statement.fetchSize = 300
            }
            try {
                List<GroovyRowResult> sourceColumnList = sql.rows(sourceColumnQuery)
                if (sourceColumnList) {
                    sourceColumnList.eachParallel { row ->
                        SourceColumnMaster.withTransaction {
                            def colData = SourceColumnMaster.getUsingReportItem(row.REPORT_ITEM, new Locale(row.LANG_ID))
                            def newVal = new SourceColumnMaster(
                                    tableName: SourceTableMaster.get(row.TABLE_NAME_ATM_ID),
                                    columnName: row.COLUMN_NAME,
                                    primaryKey: row.PRIMARY_KEY_ID,
                                    lmTableName: SourceTableMaster.get(row.LM_TABLE_NAME_ATM_ID),
                                    lmJoinColumn: row.LM_JOIN_COLUMN,
                                    lmDecodeColumn: row.LM_DECODE_COLUMN,
                                    columnType: row.COLUMN_TYPE,
                                    reportItem: row.REPORT_ITEM,
                                    lmJoinType: row.LM_JOIN_EQUI_OUTER,
                                    isDeleted: row.IS_DELETED?.asBoolean(),
                                    concatField: row.CONCATENATED_FIELD,
                                    lang: row.LANG_ID
                            )

                            if (colData) {
                                if (!colData.equals(newVal) && colData.lock()) {
                                    SourceColumnMaster.copyObj(newVal, colData)
                                    CRUDService.updateWithoutAuditLog(colData)
                                }
                            } else {
                                CRUDService.save(newVal)
                            }
                        }
                    }
                } else {
                    if (uniqueFieldId) {
                        log.error("No Row Present in Source_Column_Master for the mentioned unique field Id -> ${uniqueFieldId}")
                    }
                }
            } finally {
                sql?.close()
            }
        })
    }


    synchronized void seedSourceTableMasterMetaTable(String uniqueFieldId = null, Long tenantId, String langId) {
        String sourceTableMasterQuery = "SELECT * FROM SOURCE_TABLE_MASTER"
        if (!uniqueFieldId.equals(null) && tenantId != null && langId != null) {
            sourceTableMasterQuery = sourceTableMasterQuery.concat(" WHERE TABLE_NAME IN (SELECT LM_TABLE_NAME_ATM_ID FROM SOURCE_COLUMN_MASTER WHERE UNIQUE_FIELD_ID = '${uniqueFieldId}' and TENANT_ID = ${tenantId})")
        }
        log.info("Seeding Source Table Master")
        GParsPool.withPool(seedingThreadsSize, new Thread.UncaughtExceptionHandler() {
            @Override
            void uncaughtException(Thread failedThread, Throwable throwable) {
                log.error("Exception caught in seeding Source Table Master for ${failedThread.name}", throwable)
                if (!(throwable instanceof SQLException)) {
                    throw new RuntimeException("Stopped further due to unknown failure")
                }
            }
        }, {
            Sql sql = new Sql(dataSource_pva)
            sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
            sql.withStatement { statement ->
                statement.fetchSize = 300
            }
            try {
                List<GroovyRowResult> sourceTableMasterList = sql.rows(sourceTableMasterQuery)
                if (sourceTableMasterList) {
                    sourceTableMasterList.eachParallel { row ->
                        SourceTableMaster.withTransaction {
                            def colData = SourceTableMaster.findByTableName(row.TABLE_NAME)
                            def newVal = new SourceTableMaster(tableName: row.TABLE_NAME, tableAlias: row?.TABLE_ALIAS, tableType: row.TABLE_TYPE, caseJoinOrder: row.CASE_JOIN_ORDER, caseJoinType: row.CASE_JOIN_EQUI_OUTER, versionedData: row.VERSIONED_DATA, hasEnterpriseId: row.HAS_ENTERPRISE_ID, isDeleted: row.IS_DELETED?.asBoolean())

                            if (colData) {
                                if (!colData.equals(newVal) && colData.lock()) {
                                    SourceTableMaster.copyObj(newVal, colData)
                                    CRUDService.updateWithoutAuditLog(colData)
                                }

                            } else {
                                CRUDService.save(newVal)
                            }
                        }
                    }
                } else {
                    if (uniqueFieldId) {
                        log.error("No Row Present in Source Table Master for the mentioned unique field Id -> ${uniqueFieldId}")
                    }
                }
            } finally {
                sql?.close()
            }
        })
    }
// ===================== Seed category and tags ===================
    @Transactional
    def void seedCategoriesAndTags() {
        (readPvrExtConfig().grails.pvreports.categories as List).each {
            if (!Category.findByName(it.name)) {
                new Category(name: it.name, defaultName: it.defaultName).save()
            }
        }
        List pvcmTags = [[:]]
        if ((Holders.config.getProperty('safety.source')).equals(Constants.PVCM)) {
            pvcmTags = [[name: Holders.config.getProperty('pvcm.workflowTag')]]
        }

        ((readPvrExtConfig().grails.pvreports.tags as List) + pvcmTags).each {
            if (!Tag.findByName(it.name)) {
                new Tag(name: it.name).save()
            }
        }
        qualityService.appendQualityTypesToLeftMenu()
    }

    void seedAICategories() {
        (readPvrExtConfig().grails.pvreports.actionItemCategories as List).each {
            if (!ActionItemCategory.countByKey(it.key)) {
                new ActionItemCategory(name: it.name, description: it.description, key: it.key, forPvq: it.forPvq ?: false).save()
            }
        }
    }

    void seedIcsrOrganizationType(){
        Sql sql = new Sql(dataSource_pva)
        sql.setResultSetType(ResultSet.TYPE_FORWARD_ONLY)
        log.info("Seeding Icsr Organization Type")
        try {
            sql.rows("SELECT * from VW_SENDER_TYPE").each { row ->
                IcsrOrganizationType.withTransaction {
                    def colData = IcsrOrganizationType.findByName(row.SENDER_TYPE)
                    def newVal = new IcsrOrganizationType(org_name_id: row.ID, name: row.SENDER_TYPE, description: "", e2bR2: row.E2B_R2, e2bR3: row.E2B_R3, display: row.DISPLAY, isActive: row.IS_ACTIVE, tenantId: row.TENANT_ID, langId: row.LANG_ID)

                    if (colData) {
                        if (!colData.equals(newVal) && colData.lock()) {
                            IcsrOrganizationType.copyObj(newVal, colData)
                            CRUDService.updateWithoutAuditLog(colData)
                        }
                    } else {
                        CRUDService.save(newVal)
                    }
                }
            }

        } finally {
            sql?.close()
        }
    }

    def readPvrExtConfig() {
        ConfigSlurper config = new ConfigSlurper()
        config.parse(getURLForMetadata("$externalized"))
    }

    def InputStream getInputStreamForMetadata(String dataFilePath) {
        def rcs = grailsApplication.getMainContext().getResource(
                "$grailsApplication.config.grails.pvreports.config.root/$dataFilePath")

        //if file exists in home directory, load that
        if (rcs.exists()) {
            return rcs.getInputStream()
        }

        //load internally stored file
        if (dataFilePath == "chart_default_options.json" || MiscUtil.isLocalEnv()) {
            return new FileInputStream(grailsApplication.getMainContext().getResource(
                    "classpath:$metadataLocation/${dataFilePath}")?.file)
        }
    }

    def URL getURLForMetadata(String dataFilePath) {
        def rcs = grailsApplication.getMainContext().getResource(
                "$grailsApplication.config.grails.pvreports.config.root/$dataFilePath")
        if (rcs.exists())
            rcs.getURL()
        else {
            grailsApplication.getMainContext().getResource("classpath:$metadataLocation/$dataFilePath")?.getURL()
        }
    }

    void moveSeededFilesToSuccess(Path filePath){
        Path successPath = Paths.get("${grailsApplication.config.externalDirectory}/success")
        Path targetPath = successPath.resolve(filePath.getFileName())
        if(Files.exists(filePath) && Files.exists(successPath)) {
            Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
        } else{
            log.warn("Could not find json file or success directory")
        }
    }

    // should the "builders" be put into the domain classes, force their maintenance/upkeep there?
    protected void seedQueries() {
        User adminUser = getApplicationUserForSeeding()
        if (!adminUser) {
            log.error("Couldn't load queries as No Application seeding user does exist")
            return
        }
        Constants.Queries.each {
            if(it.key == 'pvr' || grailsApplication.config.getProperty("pv.app.${it.key}.enabled", Boolean)){
                log.info("Seeding queries for ${it.key}")
                InputStream fis = getInputStreamForMetadata("$it.value")
                try {
                    List<String> list = fetchSuperQuery(fis, adminUser)
                    if (list?.size()==0) {
                        Path filePath = Paths.get("$grailsApplication.config.externalDirectory/$it.value")
                        moveSeededFilesToSuccess(filePath)
                    }
                } catch(Exception e){
                    log.error("Error while seeding queries", e)
                } finally {
                    fis?.close()
                }
            }
        }
    }

    protected void seedConfiguration() {
        User adminUser = getApplicationUserForSeeding()
        if (!adminUser){
            log.error("Couldn't load configurations as No Application seeding user does exist")
            return
        }
        Constants.Configurations.each {
            if(it.key == 'pvr' || grailsApplication.config.getProperty("pv.app.${it.key}.enabled", Boolean)){
                log.info("Seeding configurations for ${it.key}")
                InputStream fis = getInputStreamForMetadata("$it.value")
                if(fis){
                    try{
                        List<String> list = fetchReportConfiguration(fis, adminUser)
                        if (list?.size()==0) {
                            Path filePath = Paths.get("$grailsApplication.config.externalDirectory/$it.value")
                            moveSeededFilesToSuccess(filePath)
                        }
                    } catch(Exception e){
                        log.error("Error while seeding configurations", e)
                    } finally {
                        fis?.close()
                    }
                }
            }
        }
    }

    protected void seedDashboards() {
        User adminUser = getApplicationUserForSeeding()
        if (!adminUser) {
            log.error("Couldn't load dashboards as No Application seeding user does exist")
            return
        }
        Constants.Dashboards.each{
            if(it.key == 'pvr' || grailsApplication.config.getProperty("pv.app.${it.key}.enabled", Boolean)){
                log.info("Seeding dashboards for ${it.key}")
                InputStream fis = getInputStreamForMetadata("$it.value")
                if (fis) {
                    try {
                        List<String> list = fetchDashboards(fis, adminUser)
                        if (list?.size()==0) {
                            Path filePath = Paths.get("$grailsApplication.config.externalDirectory/$it.value")
                            moveSeededFilesToSuccess(filePath)
                        }
                    } catch(Exception e){
                        log.error("Error while seeding dashboards", e)
                    } finally {
                        fis?.close()
                    }
                }
            }
        }
    }

    def fetchReportConfiguration(InputStream fis, User adminUser, boolean isUserLoggedIn = false) {
        Tuple2<List<String>, List<String>> domainInstanceList = createDomainFromJSON(ReportConfiguration.name, fis, false) { fields ->
            ReportConfiguration reportConfiguration = importService.getReportConfigurationFromJson(new JSONObject(fields), adminUser)
            return reportConfiguration
        }
        if (isUserLoggedIn) {
            return domainInstanceList
        } else {
            return domainInstanceList.getV2()
        }
    }

    def fetchDashboards(InputStream fis, User adminUser, boolean isUserLoggedIn = false) {
        Tuple2<List<String>, List<String>> domainInstanceList = createDomainFromJSON(Dashboard.name, fis, false) { d ->
            Dashboard dashboard = importService.getDashboardFromJSON(new JSONObject(d), adminUser)
            return dashboard
        }
        if (isUserLoggedIn) {
            return domainInstanceList
        } else {
            return domainInstanceList.getV2()
        }
    }

    def fetchSuperQuery(InputStream fis, User adminUser, boolean isUserLoggedIn = false) {
        Tuple2<List<String>, List<String>> domainInstanceList = createDomainFromJSON(SuperQuery.name, fis, true) { fields ->
            SuperQuery superQuery
            //                Added logic here as QuerySet Import is not supportive properly for User
            if (fields && fields.queryType?.name == QueryTypeEnum.SET_BUILDER.name()) {
                superQuery = importService.getQuerySetFromJson(new JSONObject(fields), adminUser)
            } else {
                superQuery = importService.getSuperQueryFromJson(new JSONObject(fields), adminUser)
            }
            //                Added logic here as nonValidCases is not supportive properly for User
            if (fields && Boolean.valueOf(fields.nonValidCases)) {
                superQuery.nonValidCases = true
            }
            if (fields && Boolean.valueOf(fields.icsrPadderAgencyCases)) {
                superQuery.icsrPadderAgencyCases = true
            }
            if (fields && Boolean.valueOf(fields.deletedCases)) {
                superQuery.deletedCases = true
            }
            return superQuery
        }
        if (isUserLoggedIn) {
            return domainInstanceList
        } else {
            return domainInstanceList.getV2()
        }
    }

    def fetchReportTemplate(InputStream fis, User adminUser, boolean isUserLoggedIn = false) {
        Tuple2<List<String>, List<String>> domainInstanceList = createDomainFromJSON(ReportTemplate.name, fis, false) { fields ->
//                Added logic here as TemplateSet Import is not supportive properly for User
            if (fields && fields.templateType?.name == TemplateTypeEnum.TEMPLATE_SET.name()) {
                return importService.getTemplateSetFromJson(new JSONObject(fields), adminUser)
            }
            ReportTemplate template
            Boolean isReferenced = false
            (template, isReferenced) = importService.getReportTemplateFromJSON(new JSONObject(fields), adminUser)
            if (isReferenced){
                log.warn("FetchReportTemplate - Custom Report Field already exists and is referenced in the template being imported.")
            }
//                Only Allowing CIOMS I at seeding time and making them non editable
            if (template?.name == ReportTemplate.CIOMS_I_TEMPLATE_NAME && fields?.ciomsI) {
                template.ciomsI = true
            }
            if (template?.name == ReportTemplate.MEDWATCH_TEMPLATE_NAME && fields?.medWatch) {
                template.medWatch = true
            }
            return template
        }
        if (isUserLoggedIn) {
            return domainInstanceList
        } else {
            return domainInstanceList.getV2()
        }
    }

    protected void seedTemplates() {
        User adminUser = getApplicationUserForSeeding()
        if (!adminUser) {
            log.error("Couldn't load templates as No Application seeding user does exist")
            return
        }
        Constants.Templates.each{
            if(it.key == 'pvr' || (['linelistingICSR', 'xmlICSR'].contains(it.key) && grailsApplication.config.getProperty("show.xml.option", Boolean) && grailsApplication.config.getProperty("icsr.profiles.execution", Boolean)) || grailsApplication.config.getProperty("pv.app.${it.key}.enabled", Boolean)){
                //Seeds only if the relevant properties are enabled for the module
                log.info("Seeding templates for ${it.key}")
                if(['linelistingICSR', 'xmlICSR'].contains(it.key)){
                    String externalDirectory = grailsApplication.config.externalDirectory as String
                    File[] files = new File("$externalDirectory").listFiles(new WildcardFileFilter("$it.value") as FileFilter)
                    //linelistingICSR and xmlICSR can have multiple json files that need to be seeded
                    files.each {file ->
                        InputStream fis = getInputStreamForMetadata(file.getName())
                        try {
                            List<String> list = fetchReportTemplate(fis, adminUser)
                            if (list?.size()==0) {
                                Path filePath = file.toPath()
                                moveSeededFilesToSuccess(filePath)
                            }
                        } catch(Exception e){
                            log.error("Error while seeding templates", e)
                        } finally {
                            fis?.close()
                        }
                    }
                } else{
                    InputStream fis = getInputStreamForMetadata("$it.value")
                    try {
                        List<String> list = fetchReportTemplate(fis, adminUser)
                        if (list?.size()==0) {
                            Path filePath = Paths.get("$grailsApplication.config.externalDirectory/$it.value")
                            moveSeededFilesToSuccess(filePath)
                        }
                    } catch(Exception e){
                        log.error("Error while seeding templates", e)
                    } finally {
                        fis?.close()
                    }
                }
            }
        }
    }

    @Transactional
    protected void seedETLSchedule() {
        try {
            String emailList = grailsApplication.config.etl.schedule.admin.emails
            EtlSchedule eTLSchedule =
                    new EtlSchedule(scheduleName: "ETL", startDateTime: "2015-03-31T03:23+02:00", repeatInterval: "FREQ=DAILY;INTERVAL=1",
                            isDisabled: true, isInitial: false, emailToUsers: emailList, createdBy: "bootstrap", modifiedBy: "bootstrap")
            eTLSchedule.save(failOnError: true)
        } catch (Exception e) {
            log.error("Error while seeding ETLScheduler. Please correct after startup.", e)
        }
    }

    def seedApplicationSettings() {
        ApplicationSettings applicationSettings = new ApplicationSettings()
        applicationSettings.save(failOnError: true, flush: true)
    }

    def setRunPriorityOnlyOnInstance() {
        ApplicationSettings applicationSettings = ApplicationSettings.first()
        executorThreadInfoService.addStatusOfRunPriorityOnly(applicationSettings.runPriorityOnly)
    }

    Tuple2<List<String>, List<String>> createDomainFromJSON(String clazz, InputStream dataInputStream, boolean isQuery, Closure objectBuilder) {
        int counter = 0
        String instanceName = null
        String instanceType = null
        List<String> failureList = []
        List<String> successList = []
        def domainInstance
        if (dataInputStream) {
            JSONArray json
            try {
                json = loadJSONFile(dataInputStream)
            } catch (Exception ex){
                failureList.add("Failed to parse JSON")
                log.error("Unable to parse JSON")
                log.error(ex.getMessage())
                return failureList
            }
            json.each {
                try {
                    if (clazz == ReportConfiguration.name) {
                        instanceName = it.reportName
                        instanceType = "configuration"
                    } else if (clazz == Dashboard.name) {
                        instanceName = it.label
                        instanceType = "dashboard"
                    } else {
                        instanceName = it.name
                        instanceType = isQuery ? "query" : "template"
                    }
                    domainInstance = objectBuilder(it)
                    if(!domainInstance){
                        log.error("Object (${it.name}) not loaded to app due to errors.")
                        return
                    }
                    CRUDService.save(domainInstance)
                    successList.add(instanceName)
                    if(domainInstance instanceof SuperQuery) {
                        if(!(domainInstance instanceof QuerySet)) {
                            queryService.createExecutedQuery(domainInstance)
                        }
                    }else if(domainInstance instanceof ReportTemplate) {
                        if (!(domainInstance instanceof ITemplateSet)) {
                            executedConfigurationService.createReportTemplate(domainInstance)
                        }
                    }
                    counter++
                } catch (ValidationException ve) {
                    log.warn("Unable to build ${domainInstance?.class?.name} with name : ${it.name} due to validation errors")
                    ve.errors.allErrors.each { log.error(it.defaultMessage) }
                    failureList.add(instanceName)
                } catch (Exception ex){
                    failureList.add(instanceName)
                    log.error("Unable to load ${instanceType} : ${instanceName}")
                    log.error(ex.getMessage())
                }
            }
            log.info("Read JSON ${instanceType}, added ${counter} records")
        }
        return [successList, failureList]
    }

    private JSONArray loadJSONFile(InputStream datStream) {
        def content = datStream.text
        if (!(content.trim().startsWith("[") && content.trim().endsWith("]"))) {
            content = "[${content}]"
        }
        return JSON.parse(content) as JSONArray
    }

    void seedTenants() {
        Sql sql = new Sql(dataSource_pva)
        log.info("Seeding Tenants")
        try {
            sql.rows("SELECT * from VW_TENANTS").each {
                upsertTenant(it.TENANT_ID as Long, it.TENANT_NAME, it.ACTIVE?.asBoolean())
            }
        } catch (SQLException e) {
            log.error("Exception caught in seeding tenants", e)
        } finally {
            sql?.close()
        }
    }

    private void upsertTenant(Long tenantId, String name, boolean isActive = false) {
        Tenant existingRecord = Tenant.get(tenantId)
        Tenant newRecord = new Tenant(name: name, active: isActive)
        newRecord.id = tenantId
        if (existingRecord) {
            //check to see if it needs to be updated
            if (!existingRecord.equals(newRecord)) {
                Tenant.copyObj(newRecord, existingRecord)
                CRUDService.updateWithoutAuditLog(existingRecord)
            }
        } else {
            CRUDService.saveWithoutAuditLog(newRecord)
        }
    }

    void seedDateRangeType() {
        Sql sql = new Sql(dataSource_pva)
        log.info("Seeding Date Range Type")

        try {
            sql.rows("SELECT * from pvr_date_type_values").each {
                upsertDateRangeType(it.NAME, it.SORT_ORDER as Integer, it.IS_DELETED?.asBoolean())
            }
        } catch (SQLException e) {
            log.error("Exception caught in seeding Date Range Type", e)
        } finally {
            sql?.close()
        }
    }

    private void upsertDateRangeType(String name, Integer sortOrder, boolean isDeleted = false) {
        DateRangeType existingRecord = DateRangeType.findByName(name)
        DateRangeType newRecord = new DateRangeType(name: name, sortOrder: sortOrder, isDeleted: isDeleted)
        if (existingRecord) {
            //check to see if it needs to be updated
            if (!existingRecord.equals(newRecord)) {
                DateRangeType.copyObj(newRecord, existingRecord)
                CRUDService.updateWithoutAuditLog(existingRecord)
            }
        } else {
            CRUDService.saveWithoutAuditLog(newRecord)
        }
    }

    void seedSourceProfileTable() {
        Sql sql = new Sql(dataSource_pva)
        log.info("Seeding Source Profile")
        try {
            SourceProfile newRecord
            sql.rows("SELECT * from vw_data_sources").each {
                newRecord = new SourceProfile(sourceId: it.DATA_SOURCE, sourceName: it.SOURCE_NAME, sourceAbbrev: it.SOURCE_ABBREVIATION, isCentral: it.CENTRAL_SOURCE?.asBoolean(), isDeleted: it.IS_DELETED?.asBoolean())
                SourceProfile existingRecord = SourceProfile.findBySourceId(it.DATA_SOURCE)
                if (existingRecord) {
                    if (existingRecord && !existingRecord.equals(newRecord)) {
                        existingRecord.sourceId = it.DATA_SOURCE
                        existingRecord.sourceName = it.SOURCE_NAME
                        existingRecord.sourceAbbrev = it.SOURCE_ABBREVIATION
                        existingRecord.isCentral= it.CENTRAL_SOURCE
                        existingRecord.isDeleted = it.IS_DELETED
                        CRUDService.update(GrailsHibernateUtil.unwrapIfProxy(existingRecord))
                    }
                } else {
                    CRUDService.save(GrailsHibernateUtil.unwrapIfProxy(newRecord))
                }
            }
            log.info("Seeding Source Profile Completed.")
        } catch (SQLException e) {
            log.error("Exception caught in seeding Source Profile\n", e)
        } finally {
            sql?.close()
        }
    }

    @Transactional
    void seedUserGroupSourceProfile() {
        log.info("Seeding User Group Source Profile")
        try {
            List<UserGroup> userGroups = UserGroup.findAllByIsDeleted(false)
            SourceProfile sourceProfile = SourceProfile.findByIsDeletedAndIsCentral(false, true)
            userGroups.each { UserGroup userGroup ->
                userGroup.addToSourceProfiles(sourceProfile)
                userGroup.save(flush: true)
            }
        }
        catch (Exception ex) {
            log.error("Unexpected error in seedDataService -> seedUserGroupSourceProfile", ex)
        }
    }

    @Transactional
    void copyUserGroups(String currentUserName, Map userGroups, Map params) {
        SourceProfile defaultProfile = SourceProfile.central
        userGroups.each {
            Map data = it.value
            UserGroup userGroup = UserGroup.findByName(it.key) ?: new UserGroup(name: it.key, description: data.description, createdBy: currentUserName, sourceProfiles: (defaultProfile ? [defaultProfile] : []))
            userGroup.modifiedBy = currentUserName
            userGroup.isDeleted = false

            Set<Long> selectedUsers = []
            if (userGroup.id) {
                selectedUsers = UserGroupUser.findAllByUserGroup(userGroup)*.user.collect { it.id }
            }
            data.users.each {
                User user = User.findByUsername(it)
                if(user) {
                    selectedUsers.add(user.id)
                }
            }
            params.put("selectedUsers",selectedUsers)

            CRUDService.saveOrUpdate(userGroup, [failOnError: true])
//              UserGroupUser.removeAll(userGroup, true)
            data.users.each {
                User user = User.findByUsername(it)
                if (user && userGroup && !UserGroupUser.exists(userGroup.id, user.id)) {
                    UserGroupUser.create(userGroup, user, false)
                }
            }
            params.remove("selectedUsers")
            log.info("User Group ${userGroup.name} Added Successfully")
        }

    }

    @Transactional
    void copyUsers(String currentUserName, Map users, Map params) {
        List<Role> rolesToManage = Role.findAllByAuthorityInList(['ROLE_SHARE_ALL', 'ROLE_SHARE_GROUP'])
        Tenant defaultTenant = Tenant.read(grailsApplication.config.pvreports.multiTenancy.defaultTenant)
        users.each {
            Map data = it.value
            User user = User.findByUsernameIlike(it.key)
            List<String> tenants = user ? user.tenants.collect { it.toString() } : null
            params.put("existingTenants", tenants)

            if(!user) {
                user = new User(username: it.key, fullName: data.fullName, email: data.email, createdBy: currentUserName,type: data.type)
                Locale locale = data.lang == "ja" ? Locale.JAPANESE : Locale.ENGLISH
                user.preference = new Preference(timeZone: data.timezone, locale: locale, createdBy: currentUserName)
                user.preference.modifiedBy = currentUserName
                if (data.tenants) {
                    data.tenants.each {
                        user.addToTenants(Tenant.read(it))
                    }
                } else {
                    user.addToTenants(defaultTenant)
                }

                user.modifiedBy = currentUserName

                Set rolesToBeAdded = []
                data.roles.each { role ->
                    if(role in rolesToManage*.authority) {
                        rolesToBeAdded.add(role)
                        params.put(role, "on")
                    }
                }

                CRUDService.saveOrUpdate(user, [failOnError: true])
                data.roles.each { role ->
                    if (role in rolesToManage*.authority) {
                        UserRole.create(user, rolesToManage.find { it.authority == role })
                    }
                }
                data.groups.each {
                    UserGroup userGroup = UserGroup.findByName(it)
                    if (user && userGroup && !UserGroupUser.exists(userGroup.id, user.id)) {
                        UserGroupUser.create(userGroup, user, false)
                    }
                }
                params.clear()
                log.info("User ${user.username} added successfully")
            }else {
                data.roles.each { role ->
                    if (!(role in rolesToManage*.authority)) {
                        user.authorities.each { userRole ->
                            if (userRole.authority in rolesToManage*.authority && !(userRole.authority in data.roles)) {
                                UserRole.remove(user, userRole)
                                log.info("Existing  ${user.username} Updated Successfully")
                            }
                        }
                    }
                }
            }
        }
    }

    private Integer getSeedingThreadsSize() {
        return Holders.config.getProperty('seeding.threads.pool.size', Integer, 5)
    }

    @Memoized
    String getDatabaseVersion() {
        def sql = new Sql(dataSource_pva)
        String db_version = ""
        try {
            def query = "SELECT * FROM (SELECT * FROM PVR_APP_CONSTANTS WHERE KEY_ID like 'PVR%VERSION' order by key_id) WHERE KEY_VALUE is not null and rownum = 1"
            sql.eachRow(query) {
                row ->
                    db_version = row.KEY_VALUE
            }
        }
        catch(Exception ex){
            log.error("Exception caught in extracting the DataBase version",ex)
        }
        finally{
            sql?.close()
        }
        return db_version
    }

    /**
     * This method is used for syncing Privacy Profile data from PV Admin to PVR during application startup. This method calls a PV Admin API
     * <code>/fetchPrivacyFieldProfile</code> to fetch the Privacy Profile data and update the PVR Privacy Profile.
     * <p>
     * <b>Request Attributes :</b><br>
     * <ul>
     *   <li>Type : GET</li>
     *   <li>Header : <code>PVR_PUBLIC_TOKEN</code> – Authentication token</li>
     * </ul>
     * <b>Response Attributes :</b><br>
     * <ul>
     *   <li>Body : A JSON containing the data of the report fields marked as privacy protected in PV Admin. It has the following attributes ->
     *     <ul>
     *       <li><code>code</code> – Status code</li>
     *       <li><code>data</code> – JSON containing report field data</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return void
     */
    void seedPrivacyFieldProfile() {
        String privacyProfileName = Holders.config.getProperty("pvadmin.privacy.field.profile")
        if (privacyProfileName) {
            if (Holders.config.getProperty("app.pvadmin.url")) {
                try {
                    Map responseMap = adminIntegrationApiService.get(Holders.config.getProperty("app.pvadmin.url"), "/fetchPrivacyFieldProfile", [:])
                    if (responseMap?.status == HttpStatus.SC_OK) {
                        FieldProfile fieldProfile = FieldProfile.findByNameAndIsDeleted(privacyProfileName, false)

                        if (fieldProfile) {
                            String missingKey = utilService.validateFieldJSON(responseMap.data.privacyFieldList as List<Map>)
                            if (missingKey != null) {
                                log.error("Report Field JSON is Invalid, ${missingKey} is missing from the JSON")
                            } else {
                                PrivacyProfileResponseDTO updateResponse = utilService.updatePrivacyFieldProfile(responseMap.data as Map<String, List<Map>>, fieldProfile)
                                log.info("Privacy Field Profile update called - status: " + updateResponse.status)
                            }
                        } else {
                            log.error("Privacy Field Profile does not exist so no sync with PV Admin Privacy Profiles will be performed")
                        }
                    } else {
                        log.info("Privacy Field Profile Update - status: " + responseMap.status)
                    }
                } catch (ValidationException | SQLException ex) {
                    log.error("Error while fetching Privacy Field Profile Data from PV Admin", ex)
                }
            }
        } else {
            log.error("Value not set for config pvadmin.privacy.field.profile")
        }
    }
}
