package com.rxlogix

import com.rxlogix.config.DrilldownCLLMetadata
import com.rxlogix.config.Tenant
import com.rxlogix.config.WorkflowRule
import com.rxlogix.config.WorkflowState
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import groovy.sql.Sql
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.sql.DataSource

@ConfineMetaClassChanges([AuditLogConfigUtil, ViewHelper, Tenants, DrilldownCLLMetadata, User, WorkflowRule])
class CentralControllerSpec extends Specification implements DataTest, ControllerUnitTest<CentralController> {

    public static final user = "unitTest"

    def setup() {
    }

    def cleanup() {
    }
    private mockBean(String name, def bean) {
        try {
            Holders.getApplicationContext().getBean(name)
            Holders.grailsApplication.mainContext.beanFactory.destroySingleton(name)
        } catch (Exception e) {
            //no bean registered
        }
        Holders.grailsApplication.mainContext.beanFactory.registerSingleton(name, bean)
    }
    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference
        AuditLogConfigUtil.metaClass.static.logChanges = { domain, Map newMap, Map oldMap, String eventName, String extraValue -> }
    }

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def userRole = new Role(authority: 'ROLE_PVQ_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save()
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    void "test index"() {
        when:
        controller.index()
        then:
        response.status == 200
        response.forwardedUrl == "/dashboard/index"
    }

    void "test dashboard"() {
        when:
        controller.dashboard()
        then:
        response.status == 200
        response.forwardedUrl == "/dashboard/index"

    }

    void "test newDashboard"() {
        when:
        controller.newDashboard()
        then:
        response.status == 200
        response.forwardedUrl == "/dashboard/newDashboard"

    }

    void "test removeDashboard"() {
        when:
        controller.removeDashboard()
        then:
        response.status == 200
        response.forwardedUrl == "/dashboard/removeDashboard"
    }

    void "test importRcaForm"() {
        given:
        params.cell_0_0 = "a"
        params.cell_0_1 = "b"
        params.cell_1_0 = "c"
        params.cell_1_1 = "d"
        params.total = 2
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.importRcas(0..1) { List list, Boolean submit, Boolean replace -> return false }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        controller.metaClass.getPreValidate = { List list, Boolean submit, Boolean replace -> list }
        controller.importRcaForm()

        then:
        model.rows.size() == 2
        model.rows[0][0] == "a"
    }

    void "test importSubmissionsForm"() {
        given:
        params.cell_0_0 = "a"
        params.cell_0_1 = "b"
        params.cell_1_0 = "c"
        params.cell_1_1 = "d"
        params.total = 2
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.importSubmissions(0..1) { List list, Boolean submit -> return false }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        controller.metaClass.getPreValidateSubmission = { List list, Boolean submit -> list }
        controller.importSubmissionsForm()

        then:
        model.rows.size() == 2
        model.rows[0][0] == "a"
    }

    void "test parse rca excel"() {
        given:
        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet worksheet = workbook.createSheet("Data");
        XSSFRow row = worksheet.createRow((short) 0)
        row = worksheet.createRow((short) 1)
        row.createCell((short) 0).setCellValue("16US00000000001423")
        row.createCell((short) 1).setCellValue("RK RX Partner E2B R3")
        row.createCell((short) 2).setCellValue("10-Apr-2020")
        row.createCell((short) 3).setCellValue("10-Apr-2020")
        row.createCell((short) 4).setCellValue("10-Apr-2020")
        row.createCell((short) 5).setCellValue("Exclude")
        row.createCell((short) 6).setCellValue("Exclude")
        row.createCell((short) 7).setCellValue("Exclude")
        row.createCell((short) 8).setCellValue("Exclude")
        row.createCell((short) 9).setCellValue("Exclude")
        row.createCell((short) 10).setCellValue("Case Correction")
        row.createCell((short) 11).setCellValue("Communication Awareness")
        row.createCell((short) 12).setCellValue("10-Apr-2020")
        row.createCell((short) 13).setCellValue("10-Apr-2020")
        row.createCell((short) 14).setCellValue("yes")
        row.createCell((short) 15).setCellValue(1.0)
        row.createCell((short) 16).setCellValue("Investigation")
        row.createCell((short) 17).setCellValue("Summary")
        row.createCell((short) 18).setCellValue("Action")

        when:
        List list = controller.parseFile(workbook, 19)

        then:
        list.size() == 1
        list[0].size() == 19
    }

    void "test parse offline submissions excel"() {
        given:
        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet worksheet = workbook.createSheet("Data");
        XSSFRow row = worksheet.createRow((short) 0)
        row = worksheet.createRow((short) 1)
        row.createCell((short) 0).setCellValue("16US00000000001423")
        row.createCell((short) 1).setCellValue("10-Apr-2020")
        row.createCell((short) 2).setCellValue("RK RX Partner E2B R3")
        row.createCell((short) 3).setCellValue("10-Apr-2020")
        row.createCell((short) 4).setCellValue("10-Apr-2020")
        row.createCell((short) 5).setCellValue("Periodic")
        row.createCell((short) 6).setCellValue("1")
        row.createCell((short) 7).setCellValue("CIOMS")

        when:
        List list = controller.invokeMethod('parseFile', [workbook, 8] as Object[])

        then:
        list.size() == 1
        list[0].size() == 8
    }

    void "test downloadTemplate"() {
        when:
        controller.downloadTemplate()
        then:
        response.status == 200
    }

    void "test downloadSubmissionsTemplate"() {
        when:
        controller.downloadSubmissionsTemplate()
        then:
        response.status == 200
    }

    void "test actionPlanCaseList"(){
        given:
        User adminUser=makeAdminUser()
        def mockQualityService=Mock(QualityService)
        mockQualityService.getRanges(_)>>{}
        controller.qualityService=mockQualityService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        when:
        controller.actionPlanCaseList()
        then:
        response.status==200
    }

    void "test getWhereClause"() {
        given:
        WorkflowRule.metaClass.static.findAllByConfigurationTypeEnumInListAndIsDeletedAnd = { WorkflowConfigurationTypeEnum wf, Boolean b ->
            [new WorkflowRule(name: "test", id: 1)]
        }
        when:

        String whereClause = controller.getWhereClause(responsiblePartyFilter, lateFilter, destinationFilter, workflowFilter, primaryOnly, rcFilter, classFilter, subFilter)

        then:
        whereClause == result

        where:
        responsiblePartyFilter | lateFilter       | destinationFilter  | workflowFilter | primaryOnly | rcFilter       | classFilter  | subFilter        | result
        ['1', '2']             | ["lat1", "lat2"] | ["dest1", "dest2"] | "1;2"          | true        | ["rc1", "rc2"] | ["c1", "c2"] | ["sub1", "sub2"] | " AND RESPONSIBLE_PARTY_ID in (1,2)  AND LATE_ID in (lat1,lat2)  AND DESTINATION_DESC in ('dest1','dest2')  AND ROOT_CAUSE_ID in ('rc1','rc2')  AND ROOT_CAUSE_CLASS_ID in ('c1','c2')  AND ROOT_CAUSE_SUB_CAT_ID in ('sub1','sub2')  AND FLAG_PRIMARY=1  AND WORKFLOW_STATE_ID in (1,2) "
        null                   | null             | null               | null           | false       | null           | null         | null             | ""
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getUser(0..1) { -> user }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        securityMock.demand.getCurrentUser(0..4) { -> user }
        return securityMock.proxyInstance()
    }

    void "test prepareActionPlanExcel"() {
        given:
        def normalUser = makeNormalUser()
        controller.userService = makeSecurityService(normalUser)
        Tenants.metaClass.static.currentId = { return tenant.id }
        ViewHelper.metaClass.static.getEmptyLabel = { return "(empty)" }
        DrilldownCLLMetadata.metaClass.static.findByCaseIdAndProcessedReportIdAndTenantId = { Long id, String s, Long t ->
            return new DrilldownCLLMetadata(assignedToUser: new User(fullName: "user"), workflowState: new WorkflowState(name: "wf"))
        }
        controller.dataSource_pva = Mock(DataSource)

        def mock = new MockFor(Sql)
        mock.demand.rows(1..1) { String s, Object[] params ->

            return [
                    ["CASE_ID": 1, "CASE_NUM": "CASE_NUM1", "VERSION_NUM": 1, "ROOT_CAUSE_CLASSIFICATION": "ROOT_CAUSE_CLASSIFICATION", "ROOT_CAUSE_SUB_CATEGORY": "ROOT_CAUSE_SUB_CATEGORY"],
                    ["CASE_ID": 2, "CASE_NUM": "CASE_NUM2", "VERSION_NUM": 1, "ROOT_CAUSE_CLASSIFICATION": "ROOT_CAUSE_CLASSIFICATION", "ROOT_CAUSE_SUB_CATEGORY": "ROOT_CAUSE_SUB_CATEGORY"],
                    ["CASE_ID": 3, "CASE_NUM": "CASE_NUM3", "VERSION_NUM": 1, "ROOT_CAUSE_CLASSIFICATION": "ROOT_CAUSE_CLASSIFICATION", "ROOT_CAUSE_SUB_CATEGORY": "ROOT_CAUSE_SUB_CATEGORY"],

            ]
        }
        mock.demand.close(0..3) { -> }
        mockBean("dataSource_pva", Mock(DataSource))
        when:
        List list
        mock.use {
            list = controller.fetchActonPlanCasesData((new Date() - 10), new Date(), ["resp1", "resp2"], null, null, null, null, null, null, null, false, null, null)
        }

        then:
        list.size() == 3
        list[0].size() == 19
        list[0].id == 1
        list[0].caseNumber == "CASE_NUM1"
        list[0].rootCauseClass == "ROOT_CAUSE_CLASSIFICATION"
        list[0].workFlowState == "wf"
        list[0].assignedTo == "user"

    }

    void "test prepareActionPlanExcel"() {
        given:
        def result
        params.data = "{}"
        QualityService qualityService = new QualityService()
        qualityService.userService = [getUser: {-> return normalUser}]

        qualityService.metaClass.getRanges = { def m ->
            return ["from0": new Date(), "to0": new Date()]
        }
        qualityService.metaClass.exportToExcel ={ List sheets ->
            result = sheets;
            return null
        }

        mockBean("qualityService", qualityService)
        def normalUser = makeNormalUser()
        controller.userService = makeSecurityService(normalUser)
        Tenants.metaClass.static.currentId = { return tenant.id }
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> code }
        CentralController.metaClass.static.getActionPlanData = { params, Map<String, Date> ranges ->
            [[responsibleParty  : "responsibleParty",
              responsiblePartyId: "responsiblePartyId",
              rc                : "rc",
              rcCode            : "rcCode",
              destination       : "destination",
              lastNumber0       : 1,
              lastVendor0       : 1,
              lastRc0           : 1,
              lastDestinationn0 : 1,
              completed0        : 1,
              overdue0          : 1,
              total0            : 1
             ]]
        }
        CentralController.metaClass.static.fetchActonPlanCasesData = { Date from, Date to, List responsiblePartyFilter, List lateFilter, List destinationFilter, String workflowFilter, Boolean primaryOnly, List rcFilter, List classFilter, List subFilter, boolean exactMatch, String timeZone ,Locale locale->
            [[id                : "id",
              caseNumber        : "caseNumber",
              caseVersion       : "caseVersion",
              observation       : "observation",
              assignedTo        : "assignedTo",
              rootCause         : "rootCause",
              correctiveAction  : "correctiveAction",
              responsibleParty  : "responsibleParty",
              preventativeAction: "preventativeAction",
              correctiveDate    : "correctiveDate",
              preventativeDate  : "preventativeDate",
              investigation     : "investigation",
              summary           : "summary",
              actions           : "actions",
              primary           : "primary",
              errorType         : "errorType",
              workFlowState     : "workFlowState",


             ]]
        }
        when:
        controller.prepareActionPlanExcel([data: "{}"], "Action Plan", "UTC", Locale.US)
        then:
        result.size() == 2
        result[0].data.size() == 1
        result[0].metadata.size() == 2
        result[0].metadata.sheetName == "Action Plan"
        result[0].metadata.columns.size() == 9
        result[1].metadata.sheetName.startsWith("Cases ")
        result[1].metadata.columns.size() == 18
        result[1].data.size() == 1
        result[1].data[0].size() == 18
    }
}