package reports

import com.rxlogix.ExecutedConfigurationService
import com.rxlogix.ReportExecutionKillJob
import com.rxlogix.UtilService
import com.rxlogix.config.ActionItem
import com.rxlogix.config.ApplicationSettings
import com.rxlogix.config.AutoReasonOfDelay
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportTemplate
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.hibernate.EscapedRestrictions
import com.rxlogix.localization.Localization
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.core.GrailsApplication
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.orm.HibernateCriteriaBuilder
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.AuditLogListenerThreadLocal
import grails.util.Environment
import grails.util.Holders
import grails.util.Metadata
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.jasperreports.engine.DefaultJasperReportsContext
import net.sf.jasperreports.engine.export.ooxml.XlsxSheetHelper
import org.apache.commons.io.FileUtils
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.docx4j.Docx4jProperties
import org.quartz.*
import org.springframework.web.context.support.WebApplicationContextUtils

import static java.util.Locale.ENGLISH
import static java.util.Locale.JAPAN

class BootStrap {
    def messageSource
    def CRUDService
    def seedDataService
    def reportFieldService
    def ldapService
    def emailService
    Scheduler quartzScheduler
    def nonclusterdQuartzScheduler
    def executorThreadInfoService
    def applicationConfigService
    def dataSource_pva
    def refreshConfigService
    def qualityService
    UtilService utilService
    ExecutedConfigurationService executedConfigurationService


    def static USERNAME = "Application"
    GrailsApplication grailsApplication

    def init = { servletContext ->

        long startTime = System.currentTimeMillis();
        bootInit(servletContext)
        long endTime = System.currentTimeMillis();
        log.info("Bootstrap took " + ((endTime - startTime) / 60000) + " mins");
    }

    @WithoutTenant
    void bootInit(def servletContext) {
        try {
            log.info("*********************App is initialising*********************")
            log.info("CurrentEnvironment:  ${Environment.current.name}\t app.version: ${Metadata.current.getApplicationVersion()} - ${Metadata.current.get('build.git.version') ?: 'UNKNOWN'} - ${Metadata.current.get('build.time') ?: 'UNKNOWN'}")
            log.info("DataBaseUrl: ${Holders.config.getProperty('dataSource.url')}\n\t\t\t\tDataBaseMode: ${Holders.config.getProperty('dataSource.dbCreate')}")
            log.info("DataBasePVAUrl: ${Holders.config.getProperty('dataSources.pva.url')}\n\t\t\t\tDataBasePVAMode: ${Holders.config.getProperty('dataSources.pva.dbCreate')}")
            applicationConfigService.updateAppConfigurations("PVR", dataSource_pva , false)
            refreshConfigService.updateOnetimeDependentConfigurations()
            initTempSpace()
            setDefaultTimeZone()
            resetAutoRODJobIfInProgress()
            injectCustomCriteriaMethods()
            injectMessageSource()
            injectPOIFix()
            registerMarshallers(servletContext)
            reOrderFilters()
            MiscUtil.loadFontsRegexSpecificToOs()
            //Override default Jasper.properties values.
            if (Holders.config.getProperty('dynamicJasper.config')) {
                Holders.config.getProperty('dynamicJasper.config', Map).each {
                    DefaultJasperReportsContext.getInstance().setProperty(it.key.toString(), it.value?.toString())
                }
            }

            initLocalization()

            log.info("Initializing application with default settings...")

//            ReportField.withTransaction { statusTrans ->
            seedDataService.seedApplication()

            if (!System.getProperty("seeding.off")) {

                log.info("Seeding Metadata from PVA database...")
                seedDataService.seedPVAData()
                seedDataService.seedApplicationTemplatesQueriesAndOtherData()
                if (Holders.config.grails.plugin.springsecurity.ldap.active) {
                    log.info("Seeding updated data from LDAP for existing pvr users...")
                    ldapService.mirrorLdapValues()
                }
            }

            startTemplateRemediation()
//                statusTrans.flush()
//            }

            if (!MiscUtil.isLocalTestEnv()){
                loadLmValues()
            }

            seedDataService.getDatabaseVersion()

            if (!MiscUtil.isLocalEnv()) {
                executeIncompleteJobs()
                initJasperReports()
                cacheTZoneValues()
                cacheLocalizations()
            }
            //setting max row height for columns with property net.sf.jasperreports.export.xls.auto.fit.row = true
            DefaultJasperReportsContext.getInstance().setProperty(XlsxSheetHelper.PROPERTY_MAX_ROW_HEIGHT, "52")
            DefaultJasperReportsContext.getInstance().setProperty("net.sf.jasperreports.image.dpi", "300")
            fixDoc4j()
            fixNamedQueries()
            initPvqUi()
            log.info('Starting quartz jobs scheduler')
            quartzScheduler?.start()
            startKillJob()
            showStartup()
        } catch (Exception ex) {
            log.error("Unexpected Error due to seeding processes and jobs paused. Please check with support team", ex)
            showStartupFailure()
        }
    }

    void fixDoc4j() {
       // Docx4jProperties.setProperty("docx4j.openpackaging.parts.JaxbXmlPart.MarshalToOutputStreamViaXMLStreamWriter", true);
    }

    void initPvqUi() {
        qualityService.sortColumnsAndUpdateMapping()
    }

    void initLocalization() {
        Localization.load()
        grailsApplication.domainClasses.each { domainClass ->
            domainClass.metaClass.message = { Map parameters -> Localization.getMessage(parameters) }
            domainClass.metaClass.errorMessage = { Map parameters -> Localization.setError(delegate, parameters) }
        }

        grailsApplication.serviceClasses.each { serviceClass ->
            serviceClass.metaClass.message = { Map parameters -> Localization.getMessage(parameters) }
        }
    }

    void fixNamedQueries() {
     // Unclear bug - throws nullpointer exception on first call of any namedQuery for some Entities
     // so here we making this call to cause this exception and make fine all next calls
     try{ ActionItem.fetchActionItemsForPublisherExecutedReport(null, 0L, [], [], null).count() }catch(Exception e){/*no need to log or show something*/ }
     try{ ExecutedPeriodicReportConfiguration.fetchByAdvancedPublisherWidgetFilter(null, null).count() }catch(Exception e){/*no need to log or show something*/ }
    }

    void reOrderFilters() {
        if (Holders.config.getProperty('csrfProtection.enabled', Boolean)) {
            SpringSecurityUtils.clientRegisterFilter('csrfFilter', SecurityFilterPosition.LAST.order + 10)
        }
        if (!Holders.config.getProperty('singleUserSession.enabled', Boolean)) {
            SpringSecurityUtils.clientRegisterFilter('concurrentSessionFilter', SecurityFilterPosition.CONCURRENT_SESSION_FILTER)
        }
    }

    def startKillJob() {
        nonclusterdQuartzScheduler.start()
        log.info("Starting Report Execution Kill Job")
        JobKey jobKey = new JobKey("reportExecutionKillJob", "RxLogixPVR");
        JobDetail jobDetail = JobBuilder.newJob(ReportExecutionKillJob.class).withIdentity(jobKey).build()
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("reportsExecutionKillTrigger", "RxLogixPVR")
                .withSchedule(CronScheduleBuilder.cronSchedule(grailsApplication.config.getProperty('reportExecutionKillJob.cronExpression'))).build()
        nonclusterdQuartzScheduler.scheduleJob(jobDetail, trigger)
        log.info("Report Execution Kill Job started")
    }

    def resetAutoRODJobIfInProgress(){
        AutoReasonOfDelay autoReasonOfDelay = AutoReasonOfDelay?.first()
        if(autoReasonOfDelay && autoReasonOfDelay?.executing) {
            autoReasonOfDelay.executing = false
            CRUDService.save(autoReasonOfDelay);
        }
    }

    // Cache the label keys, will be added to the cache and
    // on the frequent hits, it will consult the cache instead of db
    void cacheLocalizations() {
        Locale jp_locale = JAPAN
        Locale en_locale = ENGLISH
        GParsPool.withPool(4) {
            Localization.list().eachParallel { Localization loc ->
                loc.locale == '*' ? Localization.decodeMessage(loc.code, en_locale) : Localization.decodeMessage(loc.code, jp_locale)
            }
        }
    }

    void cacheTZoneValues() {
        GParsPool.withPool(4){
            TimeZoneEnum.values().eachParallel {TimeZoneEnum timeZoneEnum->
                DateUtil.TZ_REGISTRY.getTimeZone(timeZoneEnum.timezoneId)
            }
        }
    }
    // loading of lm values in cache
    void loadLmValues() {
        reportFieldService.loadValuesToCacheFile()
    }

    def injectCustomCriteriaMethods() {
        HibernateCriteriaBuilder.metaClass.iLikeWithEscape << { String propertyName, Object propertyValue ->
//          We would need to check HibernateCriteriaBuilder ilike method implementation if we upgrade and if any change in ilike.
            if (!delegate.validateSimpleExpression()) {
                throw RuntimeException(new IllegalArgumentException("Call to [iLikeWithEscape] with propertyName [" +
                        propertyName + "] and value [" + propertyValue + "] not allowed here."));
            }

            propertyName = delegate.calculatePropertyName(propertyName)
            propertyValue = delegate.calculatePropertyValue(propertyValue)?.toString()
            delegate.addToCriteria(EscapedRestrictions.ilike(propertyName.toString(), propertyValue?.toString()));
            return delegate
        }
    }

    void executeIncompleteJobs() {
        log.debug("Triggering Incomplete Configuration jobs to start them again")
        try {
            ExecutionStatus.findAllByExecutionStatusAndEntityType(ReportExecutionStatusEnum.BACKLOG, ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION).each {
                if (!(it.id in executorThreadInfoService.totalCurrentlyRunningIds) && !(it.id in executorThreadInfoService.totalCurrentlyRunningIcsrIds)) {
                    log.debug("Sending to error as profile failed: ${it.id}")
                    it.executionStatus = ReportExecutionStatusEnum.ERROR
                    it.message = "Failed due to restart of application just before start of the batch execution"
                    CRUDService.updateWithoutAuditLog(it)
                }
            }
            def startT1 = System.currentTimeMillis()
            ExecutionStatus.findAllByExecutionStatus(ReportExecutionStatusEnum.GENERATING).each {
                if (!(it.id in executorThreadInfoService.totalCurrentlyRunningIds) && !(it.id in executorThreadInfoService.totalCurrentlyRunningIcsrIds)) {
                    if (it.entityType == ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION) {
                        log.debug("Sending to error as profile failed: ${it.id}")
                        it.executionStatus = ReportExecutionStatusEnum.ERROR
                        it.message = "Failed due to restart of application in middle of execution"
                    } else {
                        log.debug("Sending to backlog to re-execute: ${it.id}")
                        ReportConfiguration executedConfigurationForBackLog = ReportConfiguration.findById(it.entityId)
                        if(executedConfigurationForBackLog) {
                            Long executedReportId = ExecutedReportConfiguration.getLatestExecutedConfigurationForRerun(executedConfigurationForBackLog).get()
                            if (executedReportId) {
                                ExecutedReportConfiguration executedStatusChange = ExecutedReportConfiguration.findById(executedReportId)
                                executedStatusChange.status = ReportExecutionStatusEnum.ERROR
                                CRUDService.updateWithoutAuditLog(executedStatusChange)
                            }
                        }
                        it.executionStatus = ReportExecutionStatusEnum.BACKLOG
                    }

                    CRUDService.updateWithoutAuditLog(it)
                }
            }

            def endT1 = System.currentTimeMillis()
            def elapsedTimeT1 = endT1 - startT1
            log.info("Time taken to update the executed report configuration based on execution status generation:: ", elapsedTimeT1)

            // For delivering only just update to warn
            ExecutionStatus.findAllByExecutionStatus(ReportExecutionStatusEnum.DELIVERING).each {
                if (!(it.id in executorThreadInfoService.totalCurrentlyRunningIds) && !(it.id in executorThreadInfoService.totalCurrentlyRunningIcsrIds)) {
                    it.executionStatus = ReportExecutionStatusEnum.WARN
                    it.message = "This Report might not have been delivered as expected. Please check your Inbox."
                    CRUDService.updateWithoutAuditLog(it)
                    try{
                        emailService.emailFailureNotification(it)
                    }
                    catch(Exception ex){
                        log.error("Error Occurred while sending warning emails", ex)
                    }
                }
            }

// Commented out due to significant performance impact during startup and no observed functional impact.
//            ReportConfiguration.findAllByIsEnabled(true).each { reportConf ->
//                    Long executedReportId = ExecutedReportConfiguration.getLatestExecutedConfigurationForRerun(reportConf).get()
//                    if (executedReportId) {
//                        ExecutedReportConfiguration executedStatusChange = ExecutedReportConfiguration.findById(executedReportId)
//                        executedStatusChange.status = ReportExecutionStatusEnum.ERROR
//                        CRUDService.updateWithoutAuditLog(executedStatusChange)
//                    }
//            }
            def startT2 = System.currentTimeMillis()
            def reportConfigs = ReportConfiguration.findAllByIsEnabled(true)
            def reportKeyToConfigMap = reportConfigs.collectEntries { rc ->
                ["${rc.owner}::${rc.reportName}", rc]
            }
            List<List<ExecutedReportConfiguration>> executedReportConfigs = []
            executedReportConfigs = ExecutedReportConfiguration.fetchLatestExecutedConfigs(reportKeyToConfigMap)
            List<ExecutedReportConfiguration> executedReportConfigToUpdate = executedReportConfigs.flatten().findAll {
                it != null
            }
            executedReportConfigToUpdate.each {
                it.status = ReportExecutionStatusEnum.ERROR
                CRUDService.updateWithoutAuditLog(it)


            }
            def endT2 = System.currentTimeMillis()
            def elapsedTimeT2 = endT2 - startT2
            log.info("Time taken to update the executed report configuration for enabled configurations :: ", elapsedTimeT2)

        } catch (Exception ex) {
            log.error("Error Occurred while updating status of incomplete jobs", ex)
        }

    }

    void initTempSpace() {
        def tempSpace = new File(Holders.config.getProperty('tempDirectory'))
        log.info("Initializing application temp space in ${tempSpace.absolutePath}")
        try {
            tempSpace.mkdirs()
        } catch (SecurityException se) {
            log.error(se.message)
        }
    }

    //This timeZone is for TimeCategory which  will now use UTC as default.
    def setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    def injectMessageSource() {

        ViewHelper.class.metaClass.getMessageSource = {
            messageSource
        }
        ViewHelper.class.metaClass.static.getMessageSource = {
            messageSource
        }
    }

    def destroy = {
        showShutdown()
    }

    def registerMarshallers(servletContext) {
        def springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
        springContext.getBean("customMarshallerRegistry").register()
    }

    /**
     * Initialization of Dynamic Reports and Jasper Reports libraries by an empty report
     * It allows to reduce execution time for the first report after server restarting
     */
    private initJasperReports() {
        log.info("Initializing jasper reports library")
        ReportBuilder reportBuilder = new ReportBuilder()
        JasperReportBuilder report = reportBuilder.initializeNewReport()
        report.toJasperPrint()
        report.jasperParameters["REPORT_VIRTUALIZER"]?.cleanup()
    }

    private injectPOIFix(){
        //fix for PVR-6815, hack to solve POI library version conflict
        XSSFWorkbook.metaClass.setSheetNameHack={ int sheetIndex, String sheetname->
            if (sheetname == null) {
                throw new IllegalArgumentException( "sheetName must not be null" );
            }
            validateSheetIndex(sheetIndex);
            String oldSheetName = getSheetName(sheetIndex);
            if(sheetname.length() > 31) {
                sheetname = sheetname.substring(0, 31);
            }
            WorkbookUtil.validateSheetName(sheetname);
            if (sheetname.equals(oldSheetName)) {
                return;
            }
            if (containsSheet(sheetname, sheetIndex )) {
                throw new IllegalArgumentException( "The workbook already contains a sheet of this name" );
            }
            workbook.getSheets().getSheetArray(sheetIndex).setName(sheetname);
        }
    }

    private showStartup() {
        def startup = """
            MP''''''`MM   dP                       dP
            M  mmmmm..M   88                       88
            M.      `YM d8888P .d8888b. 88d888b. d8888P dP    dP 88d888b.
            MMMMMMM.  M   88   88'  `88 88'  `88   88   88    88 88'  `88
            M. .MMM'  M   88   88.  .88 88         88   88.  .88 88.  .88
            Mb.     .dM   dP   `88888P8 dP         dP   `88888P' 88Y888P'
            MMMMMMMMMMM                                          88
            """
        log.info(startup)
    }

    private showShutdown() {
        def shutdown = """
            MP'''''''`MM dP                  dP         dP
            M  mmmmm..M 88                  88         88
            M.      `YM 88d888b. dP    dP d8888P .d888b88 .d8888b. dP  dP  dP 88d888b.
            MMMMMMM.  M 88'  `88 88    88   88   88'  `88 88'  `88 88  88  88 88'  `88
            M. .MMM'  M 88    88 88.  .88   88   88.  .88 88.  .88 88.88b.88' 88    88
            Mb.     .dM dP    dP `88888P'   dP   `88888P8 `88888P' 8888P Y8P  dP    dP
            MMMMMMMMMMM
            """
        log.info(shutdown)
    }

    private showStartupFailure() {
        def failure = """
               FFFFFFF      AAA      IIIII    LL         EEEEEEE    DDDDD   
               FF          AAAAA      III     LL         EE         DD  DD  
               FFFF       AA   AA     III     LL         EEEEE      DD   DD 
               FF         AAAAAAA     III     LL         EE         DD   DD 
               FF         AA   AA    IIIII    LLLLLLL    EEEEEEE    DDDDDD  
                """
        log.error(failure)
    }

    void startTemplateRemediation() {
        int creationSuccess = 0, creationFailure = 0, templateNotFound = 0
        Sql sql = new Sql(utilService.getReportConnectionForPVR())
        String remediationFlagQuery = 'SELECT IS_REMEDIATION_REQUIRED FROM TEMPLATE_REMEDIATION'
        Integer isRemediationRequired = sql.firstRow(remediationFlagQuery).IS_REMEDIATION_REQUIRED as Integer
        if (isRemediationRequired == 1) {
            log.info("Starting Automatic Template Remediation at ${(new Date()).format(DateUtil.DATETIME_FMT)}")
            try {
                sql.rows("SELECT DISTINCT ID, RANK FROM TEMPLATE_LIST ORDER BY RANK, ID").each { row ->
                    try {
                        ReportTemplate template = ReportTemplate.get(row.ID as Long)
                        if (template) {
                            executedConfigurationService.createReportTemplate(template)
                            ReportTemplate.withSession { session ->
                                session.flush()
                            }
                            creationSuccess++
                        } else {
                            log.error("Template not found for ID: ${row.ID}")
                            templateNotFound++
                        }
                    } catch (Exception e) {
                        log.error("Error Occurred while creating executed instance of template for ID: ${row.ID} : ${e.getMessage()}")
                        creationFailure++
                    }
                }
            } catch (Exception ex) {
                log.error(ex.getMessage())
            } finally {
                sql.execute('UPDATE TEMPLATE_REMEDIATION SET IS_REMEDIATION_REQUIRED = 0, LAST_REMEDIATION_DATE = CURRENT_TIMESTAMP')
                sql?.close()
            }
            log.info("Successfully executed templates : ${creationSuccess}")
            log.info("Execution failed templates : ${creationFailure}")
            log.info("Templates not found : ${templateNotFound}")
            log.info("Automatic Template Remediation Completed at ${(new Date()).format(DateUtil.DATETIME_FMT)}")
        }
    }
}