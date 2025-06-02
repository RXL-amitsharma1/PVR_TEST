import com.rxlogix.Constants
import com.rxlogix.ExecutedConfigurationService
import com.rxlogix.RxCodec
import com.rxlogix.SqlService
import com.rxlogix.config.Configuration
import com.rxlogix.config.CustomSQLTemplate
import com.rxlogix.config.DrilldownCLLMetadata
import com.rxlogix.config.GlobalDateRangeInformation
import com.rxlogix.config.ITemplateSet
import com.rxlogix.config.MailOAuthToken
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.QualityCaseData
import com.rxlogix.config.QualitySampling
import com.rxlogix.config.QualitySubmission
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportRequest
import com.rxlogix.config.ReportRequestType
import com.rxlogix.config.ReportSubmission
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.WorkflowJustification
import com.rxlogix.config.WorkflowRule
import com.rxlogix.config.WorkflowState
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportRequestTypeEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.UserType
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.user.Role
import com.rxlogix.user.UserRole
import com.rxlogix.util.MiscUtil
import groovy.sql.Sql
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import com.rxlogix.user.User
import grails.util.Holders

databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1473684391568-21") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                changeSetExecuted(id: '1473684391568-1')
            }
            not {
                changeSetExecuted(id: '1473684391568-13')
            }
            columnExists(tableName: "REPORT_REQUEST", columnName: "REPORT_TYPE")
        }
        grailsChange {
            change {
                try {
                    ctx.getBean('seedDataService').seedReportRequestTypes()
                    List reportRequestTypes = ReportRequestType.list()
                    reportRequestTypes.each { ReportRequestType reportRequestType ->
                        sql.executeUpdate("update REPORT_REQUEST set REPORT_REQUEST_TYPE_ID = ? where REPORT_TYPE = ?", [reportRequestType.id, reportRequestType.name])
                    }
                    ReportRequestType reportRequestType = ReportRequestType.findByName(ReportRequestTypeEnum.AD_HOC_REPORT.value)
                    if (reportRequestType) {
                        sql.executeUpdate("update REPORT_REQUEST set REPORT_REQUEST_TYPE_ID = ? where REPORT_REQUEST_TYPE_ID is null", [reportRequestType.id])
                    }

                } catch (Exception ex) {
                    println "##### Error Occurred while updating old records for ReportRequestType liquibase change-set 1473684391568-21 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1473684391568-14") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'table is empty or already executed', onErrorMessage: 'table is empty or already executed') {
            sqlCheck(expectedResult: 'Y', "SELECT Nullable FROM user_tab_columns WHERE table_name = 'REPORT_REQUEST' AND column_name = 'REPORT_REQUEST_TYPE_ID';")
        }
        addNotNullConstraint(tableName: "REPORT_REQUEST", columnName: "REPORT_REQUEST_TYPE_ID")
    }

    changeSet(author: "forxsv (generated)", id: "1535881335124-3") {
        grailsChange {
            change {
                try {
                    ctx.getBean('seedDataService').seedWorkflowStates()
                    ctx.getBean('seedDataService').seedWorkflowRules()

                    WorkflowState newState = WorkflowState.findByName(WorkflowState.NEW_NAME)
                    if (newState) {
                        sql.executeUpdate("update REPORT_REQUEST set WORKFLOW_STATE_ID = ? where WORKFLOW_STATE_ID is null and status = 'OPEN'", [newState.id])
                        sql.executeUpdate("update EX_RCONFIG set WORKFLOW_STATE_ID = ? where WORKFLOW_STATE_ID is null and class = 'com.rxlogix.config.ExecutedConfiguration'", [newState.id])
                    }

                    WorkflowState inProgressState = WorkflowState.findByName(WorkflowState.INPROGRESS_NAME)
                    if (inProgressState) {
                        sql.executeUpdate("update REPORT_REQUEST set WORKFLOW_STATE_ID = ? where WORKFLOW_STATE_ID is null and status = 'IN_PROGRESS'", [inProgressState.id])
                    }

                    WorkflowState closedState = WorkflowState.findByName(WorkflowState.CLOSED_NAME)
                    if (closedState) {
                        sql.executeUpdate("update REPORT_REQUEST set WORKFLOW_STATE_ID = ? where WORKFLOW_STATE_ID is null and status = 'CLOSED'", [closedState.id])
                    }

                    WorkflowState needState = WorkflowState.findByName(WorkflowState.NEEDCLARIFICATION_NAME)
                    if (needState) {
                        sql.executeUpdate("update REPORT_REQUEST set WORKFLOW_STATE_ID = ? where WORKFLOW_STATE_ID is null and status = 'NEED_CLARIFICATION'", [needState.id])
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating Report Request statuses, liquibase change-set 1535881335123-3 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "ShubhamRx(generated)", id: "20210119055510-2") {
        grailsChange {
            change {
                    ReportSubmission.list().each { reportSubmission ->
                        try {
                            reportSubmission.tenantId = reportSubmission.executedReportConfiguration.tenantId
                            reportSubmission.save(failOnError: true, flush: true, validate: false)
                        } catch (Exception ex){
                            println "#### Error while updating Report Submission for : "+reportSubmission.id +"####"
                            println (ex.getMessage())
                        }
                    }
            }
        }
    }

    changeSet(author: "sargam", id: "20210422081510-1") {
        grailsChange {
            change {
                try {
                    List<ReportConfiguration> reportConfigurations = ReportConfiguration.findAllByGlobalDateRangeInformationIsNullAndIsDeleted(false)
                    reportConfigurations.each { configuration ->
                        configuration.globalDateRangeInformation = new GlobalDateRangeInformation()
                        configuration.globalDateRangeInformation.reportConfiguration = configuration
                        configuration.globalDateRangeInformation.save(flush: true)
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating Global Date Range Information, liquibase change-set 20210422081510-1 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: 'ShubhamRx', id: '202212090344-1') {
        grailsChange {
            change {
                try {
                    ReportTemplate.findAllByIsDeletedAndOriginalTemplateIdAndTemplateTypeInList(false, 0l, [TemplateTypeEnum.CASE_LINE, TemplateTypeEnum.DATA_TAB, TemplateTypeEnum.CUSTOM_SQL, TemplateTypeEnum.NON_CASE]).each {
                        ctx.getBean('executedConfigurationService').createReportTemplate(it)
                    }
                    ReportTemplate.withSession {
                        it.flush()
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while creating executed instances of templates, liquibase change-set 202212090344-1 ####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: 'ShubhamRx', id: '202212090344-2') {
        grailsChange {
            change {
                try {
                    SuperQuery.findAllByIsDeletedAndOriginalQueryIdAndQueryTypeInList(false, 0l, [QueryTypeEnum.QUERY_BUILDER, QueryTypeEnum.CUSTOM_SQL]).each {
                        ctx.getBean('queryService').createExecutedQuery(it)
                    }
                    SuperQuery.withSession {
                        it.flush()
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while creating executed instances of queries, liquibase change-set 202212090344-2 ####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: 'sergey', id: '202308180344-1') {
        grailsChange {
            change {
                try {
                    if(Holders.config.qualityModule) {
                        List qualityTags = [[tag: Holders.config.qualityModule.qualityTagName, name: PvqTypeEnum.CASE_QUALITY.name()],
                                            [tag: Holders.config.qualityModule.submissionTagName, name: PvqTypeEnum.SUBMISSION_QUALITY.name()]]
                        Holders.config.qualityModule.additional.each {
                            qualityTags.add([tag: it.tag, name: it.name])
                        }
                        List tags = qualityTags.collect { it.tag }.findAll{it}
                        if(tags) {
                            Map tagsMap = qualityTags.collectEntries { [(it.tag): it.name] }
                            String sql = "from Configuration where id in " +
                                    "(select distinct(ex.id) from Configuration ex " +
                                    "inner join ex.tags as tag where ex.isDeleted = false and tag.name in (:tags) )"
                            Configuration.executeQuery(sql, [tags: tags]).each { Configuration cfg ->
                                List pvqTags = cfg.tags.findAll { it.name in tags }
                                cfg.pvqType = pvqTags.collect { tagsMap[it.name] }.join(";") + ";"
                                pvqTags.each { cfg.removeFromTags(it) }
                                cfg.save(failOnError: true, flush: true)
                            }
                        }
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while updating PVQuality tags 202308180344-2 ####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }
//When updating CIOMS Select Statement, the changeSet ID needs to be updated
    changeSet(author: "Siddharth", id: "20250502693737-07") {
        sqlFile(path: "liquibase/CIOMS_SELECT_STMT.SQL")
        grailsChange {
            change {
                try {
                    println "##### Executing CIOMS update ####"
                    ExecutedConfigurationService executedConfigurationService = ctx.getBean('executedConfigurationService')
                    SqlService sqlService = ctx.getBean('sqlService')
                    ReportTemplate.findAllByIsDeletedAndNameAndCiomsIAndOriginalTemplateId(false, "CIOMS I Template", true, 0L).each {
                        it = GrailsHibernateUtil.unwrapIfProxy(it)
                        String toValidate = CustomSQLTemplate.getSqlQueryToValidate(it)
                        it.columnNamesList = sqlService.getColumnsFromSqlQuery(toValidate, false, false).toListString()
                        MiscUtil.linkFixedTemplate(Constants.CIOMS_I_JRXML_FILENAME, "/jrxml", it)
                        executedConfigurationService.createReportTemplate(it)
                    }
                    ReportTemplate.withSession {
                        it.flush()
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while creating executed template in liquibase change-set 20250502193736-07 ####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }
    changeSet(author: "Siddharth", id: "20250502193808-07") {
        sqlFile(path: "liquibase/MEDWATCH_SELECT_STMT.SQL")
        grailsChange {
            change {
                try {
                    println "##### Executing Medwatch update ####"
                    ExecutedConfigurationService executedConfigurationService = ctx.getBean('executedConfigurationService')
                    SqlService sqlService = ctx.getBean('sqlService')
                    ReportTemplate.findAllByIsDeletedAndNameAndMedWatchAndOriginalTemplateId(false, "Medwatch Template", true, 0L).each {
                        it = GrailsHibernateUtil.unwrapIfProxy(it)
                        String toValidate = CustomSQLTemplate.getSqlQueryToValidate(it)
                        it.columnNamesList = sqlService.getColumnsFromSqlQuery(toValidate, false, false).toListString()
                        MiscUtil.linkFixedTemplate(Constants.MEDWATCH_JRXML_FILENAME, "/jrxml", it)
                        executedConfigurationService.createReportTemplate(it)
                    }
                    ReportTemplate.withSession {
                        it.flush()
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while creating executed template in liquibase change-set 20250502193806-07 ####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }


    changeSet(author: "Gunjan", id: "202303041611-2", runOnChange: "true") {
        grailsChange {
            change {
                try {
                    List<User> newUsers = User.findAllByTypeIsNull()
                    if (newUsers) {
                        List<String> providerNames = Holders.config.grails.plugin.springsecurity.providerNames
                        if (providerNames.contains('ldapAuthProvider')) {
                            newUsers*.type = UserType.LDAP
                        } else {
                            newUsers*.type = UserType.NON_LDAP
                        }
                        newUsers*.save(failOnError: true, flush: true, validate: false)
                    }

                } catch (Exception e) {
                    println "##### Error Occurred while updating UserType in liquibase change-set 202303041611-1 ####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "Siddharth", id: "20241118063054-7") {
        grailsChange {
            change {
                try {
                    String pvrMartUser = Holders.config.dataSources.pva.username
                    if (pvrMartUser) {
                        sql.execute("GRANT SELECT ON super_query TO " + pvrMartUser)
                        sql.execute("GRANT SELECT ON pvs_cached_query_sets TO " + pvrMartUser)
                        sql.execute("GRANT SELECT ON pvs_cached_query_details TO " + pvrMartUser)
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while executing grants; change-set 20241118063054-7####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "ShubhamRx", id: "202501311518-02") {
        grailsChange {
            change {
                try {
                    new Role(authority: 'ROLE_ICSR_DISTRIBUTION', description: 'User can perform actions in ICSR Case Tracking',
                            createdBy: 'Application', modifiedBy: 'Application').save(flush:true)

                    new Role(authority: 'ROLE_ICSR_DISTRIBUTION_ADMIN', description: 'User can perform additional actions in ICSR Case Tracking',
                            createdBy: 'Application', modifiedBy: 'Application').save(flush:true)

                } catch (Exception e) {
                    println "##### Error Occurred while Creating role ROLE_ICSR_DISTRIBUTION and ROLE_ICSR_DISTRIBUTION_ADMIN; change-set 202501311518-02####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "ShubhamRx", id: "202501311519-02") {
        grailsChange {
            change {
                try {
                    Role icsrViewerRole = Role.findByAuthority("ROLE_ICSR_PROFILE_VIEWER")
                    Role icsrDistributionRole = Role.findByAuthority("ROLE_ICSR_DISTRIBUTION")
                    List<User> users = UserRole.findAllByRole(icsrViewerRole)*.user
                    users.each {
                        UserRole.create(it, icsrDistributionRole)
                    }
                    UserRole.withSession {
                        it.flush()
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while providing role ROLE_ICSR_DISTRIBUTION; change-set 202501311519-02####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "Sachin Verma", id: "202504131155-01") {
        grailsChange {
            change {
                try {
                    MailOAuthToken.withNewSession {
                        MailOAuthToken existingToken = MailOAuthToken.first()
                        if (!existingToken) {
                            return
                        }
                        existingToken.with {
                            accessToken = RxCodec.encode(existingToken.accessToken)
                            refreshToken = RxCodec.encode(existingToken.refreshToken)
                        }
                        existingToken.save(flush: true, failOnError: true)
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while encoding mail on auth token; change-set 202504131155-01####"
                    e.printStackTrace(System.out)
                }

            }
        }
    }

    changeSet(author: "ShubhamRx", id: "202503201152-01") {
        grailsChange {
            change {
                try {
                    List<Long> drugAuthorizationList = []
                    List<Long> deviceAuthorizationList = []
                    AuthorizationType.withNewSession {
                        drugAuthorizationList = AuthorizationType.findAllByNameInList(['Marketed Drug', 'Investigational Drug']).collect {
                            it.id as long
                        }
                        deviceAuthorizationList = AuthorizationType.findAllByNameInList(['Marketed Device', 'Investigational Device']).collect {
                            it.id as long
                        }
                    }
                    IcsrProfileConfiguration.findAllByIsJapanProfile(false).each {
                        try {
                            if (it.authorizationTypes?.size() == 0) {
                                if (it.deviceReportable) {
                                    it.authorizationTypes.addAll(deviceAuthorizationList)
                                } else {
                                    it.authorizationTypes.addAll(drugAuthorizationList)
                                }
                                it.save()
                            }
                        } catch (Exception e1) {
                            println "##### Error Occurred while saving profile ${it.reportName} with Id ${it.id}; change-set 202503201152-01####"
                            e1.printStackTrace(System.out)
                        }
                    }
                    IcsrProfileConfiguration.withSession {
                        it.flush()
                    }
                } catch (Exception e) {
                    println "##### Error Occurred while migrating legacy profiles for authorization type; change-set 202503201152-01####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }
    changeSet(author: "Sergey", id: "202504041520-03") {
        grailsChange {
            change {
                try {
                    ReportRequest.withNewSession {
                        ReportRequest.findAllByIsDeleted(false).each { ReportRequest reportRequest ->
                            reportRequest.requestorsNames = reportRequest.getRequestorList()?.join(" ,")
                            reportRequest.save(flush:true)
                        }
                    }

                } catch (Exception e) {
                    println "##### Error Occurred while updating reportRequest.requestorsNames; change-set 202504041520-02####"
                    e.printStackTrace(System.out)
                }
            }
        }
    }
}