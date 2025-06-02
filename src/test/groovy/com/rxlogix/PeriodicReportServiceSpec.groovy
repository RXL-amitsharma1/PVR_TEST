package com.rxlogix

import com.rxlogix.config.GlobalDateRangeInformation
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.Tenant
import com.rxlogix.dictionary.DictionaryGroup
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.pvdictionary.config.DictionaryConfig
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

//@TestMixin(ControllerUnitTestMixin)
@ConfineMetaClassChanges([PeriodicReportConfiguration, ViewHelper, DictionaryGroup])
class PeriodicReportServiceSpec extends Specification implements DataTest, ServiceUnitTest<PeriodicReportService> {

    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()

    def setupSpec() {
        mockDomains PeriodicReportConfiguration, Role, User, Preference, UserRole, DictionaryGroup
    }

    def setup() {
        // force the tests to run in the TEST_TZ
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))
    }

    def cleanup() {
        // set the TZ back to what it was
        TimeZone.setDefault(ORIGINAL_TZ)
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
        return adminUser
    }

    def "test exportToExcel"() {
        given:
        def scheduler = "{\"startDateTime\":\"2017-06-16T00:58-05:00\",\"timeZone\":{\"text\":\"(GMT -05:00) EST\",\"selected\":true,\"offset\":\"-05:00\",\"name\":\"EST\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1\"}"
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return code }

        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet worksheet = workbook.createSheet("Data")

        // Create rows 0 and 1 (possibly unused but kept for compatibility)
        worksheet.createRow((short) 0)
        worksheet.createRow((short) 1)

        // Create header row at index 2
        XSSFRow headerRow = worksheet.createRow((short) 2)
        headerRow.createCell((short) 0).setCellValue("Report Name")
        headerRow.createCell((short) 1).setCellValue("Template")
        headerRow.createCell((short) 2).setCellValue("Product")
        headerRow.createCell((short) 3).setCellValue("Col3")
        headerRow.createCell((short) 4).setCellValue("Col4")
        headerRow.createCell((short) 5).setCellValue("Col5")
        headerRow.createCell((short) 6).setCellValue("Col6")
        headerRow.createCell((short) 7).setCellValue("Report Type")
        headerRow.createCell((short) 8).setCellValue("Data Source")
        headerRow.createCell((short) 9).setCellValue("Due In Days")
        headerRow.createCell((short) 10).setCellValue("Start Date")
        headerRow.createCell((short) 11).setCellValue("End Date")
        headerRow.createCell((short) 12).setCellValue("Primary Destination")
        headerRow.createCell((short) 13).setCellValue("Tenant Id")
        headerRow.createCell((short) 14).setCellValue("Scheduler")
        headerRow.createCell((short) 15).setCellValue("Group Enabled")
        headerRow.createCell((short) 16).setCellValue("Username")

        // Create data row at index 3
        XSSFRow dataRow = worksheet.createRow((short) 3)
        dataRow.createCell((short) 0).setCellValue("report 1")
        dataRow.createCell((short) 1).setCellValue("template 1")
        dataRow.createCell((short) 2).setCellValue("ASPIRIN ALUMINIUM,AZASPIRIUM CHLORIDE")
        dataRow.createCell((short) 3).setCellValue("")
        dataRow.createCell((short) 4).setCellValue("")
        dataRow.createCell((short) 5).setCellValue("")
        dataRow.createCell((short) 6).setCellValue("")
        dataRow.createCell((short) 7).setCellValue("PBRER")
        dataRow.createCell((short) 8).setCellValue("CUSTOM")
        dataRow.createCell((short) 9).setCellValue("1")
        dataRow.createCell((short) 10).setCellValue("2018-07-04T00:00:00")
        dataRow.createCell((short) 11).setCellValue("2018-07-13T23:59:59")
        dataRow.createCell((short) 12).setCellValue("FDA")
        dataRow.createCell((short) 13).setCellValue("2")
        dataRow.createCell((short) 14).setCellValue(scheduler)
        dataRow.createCell((short) 15).setCellValue("1")
        dataRow.createCell((short) 16).setCellValue("admin")

        Holders.config.pv.dictionary.group.enabled = true

        PeriodicReportConfiguration.metaClass.static.findByReportNameAndIsDeletedAndTenantIdAndOwner = {
            String reportName, Boolean isDeleted, Long tenantId, User owner ->
                new PeriodicReportConfiguration(
                        id: 1,
                        reportName: "test",
                        periodicReportType: PeriodicReportTypeEnum.JPSR,
                        globalDateRangeInformation: new GlobalDateRangeInformation(),
                        productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}',
                        scheduleDateJSON: 'scheduleDateJSON',
                        primaryReportingDestination: "dest",
                        dueInDays: 1
                )
        }

        DictionaryGroup.metaClass.static.getAllRecordsBySearch = {
            Integer dicType, String term, String dataSource, User user, Integer tenantId, Boolean exactSearch ->
                new Object() {
                    List<Map> list() { [] }
                }
        }

        def crudServiceMock = new MockFor(CRUDService)
        def resultConfig
        crudServiceMock.demand.save(1) { o ->
            resultConfig = o
            return o
        }
        service.CRUDService = crudServiceMock.proxyInstance()

        service.configurationService = new ConfigurationService()
        service.configurationService.metaClass.parseProducts = { r, product, columNumber, lang ->
            product["1"] = [["name": "ingr", "id": 111], ["name": "ingr2", "id": 222]]
            product["2"] = []
            product["3"] = []
            product["4"] = []
            return 5
        }

        PVDictionaryConfig.setProductConfig(
                new DictionaryConfig(
                        views: [["1", 'Ingredient'], ["2", 'Family'], ["3", 'Product Generic Name'], ["4", 'Trade Name']]
                )
        )

        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..1) { -> return new User() }
        service.userService = userMock.proxyInstance()

        def adminUser = makeAdminUser()
        User.metaClass.findByUsernameIlike = { name -> adminUser }

        when:
        def result = service.importFromExcel(workbook)

        then:
        result.size() == 3
        resultConfig.reportName == "report 1"
        resultConfig.periodicReportType == PeriodicReportTypeEnum.PBRER
        resultConfig.primaryReportingDestination == "FDA"
        resultConfig.dueInDays == 2
        resultConfig.scheduleDateJSON == scheduler
        DateUtil.toDateString(resultConfig.globalDateRangeInformation.dateRangeStartAbsolute, DateUtil.ISO_DATE_TIME_FORMAT) != null
        DateUtil.toDateString(resultConfig.globalDateRangeInformation.dateRangeEndAbsolute, DateUtil.ISO_DATE_TIME_FORMAT) != null
        resultConfig.productSelection == '{"1":[{"name":"ingr","id":111},{"name":"ingr2","id":222}],"2":[],"3":[],"4":[]}'
    }
}
