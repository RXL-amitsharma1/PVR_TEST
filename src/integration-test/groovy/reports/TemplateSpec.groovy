package reports

import com.rxlogix.config.*
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.User
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class TemplateSpec extends Specification {

    @Shared caseLineListingTemplate

    static private final Integer DEFAULT_TENANT_ID = 1

    def CRUDService

    def setup() {
        caseLineListingTemplate = createCaseLineListingTemplate()
    }

    def cleanup() {}

    void "Test save a Case Line Listing Template with a new Category" () {
        when: "CRUDService is called on to perform the save"
        CaseLineListingTemplate savedTemplate = (CaseLineListingTemplate) CRUDService.save(caseLineListingTemplate)

        then: "Object was created successfully"
        assert savedTemplate.id != null
        assert savedTemplate.category.name == "New Category"
    }

    private createCaseLineListingTemplate() {
        def user = User.findByFullName("Admin User")
        def reportField = ReportField.findByNameAndIsDeleted("masterCaseNum",false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: reportField, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(
                templateType: TemplateTypeEnum.CASE_LINE,
                name: 'Test template',
                category: Category.findByName("New Category") ?: (new Category(name: "New Category").save()),
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username,
                columnList: reportFieldInfoList, tenantId: DEFAULT_TENANT_ID.toLong())

        return template
    }
}


