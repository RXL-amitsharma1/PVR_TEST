package com.rxlogix

import com.rxlogix.api.CaseSeriesRestController
import com.rxlogix.config.CaseDeliveryOption
import com.rxlogix.config.CaseSeries
import com.rxlogix.config.CaseSeriesDateRangeInformation
import com.rxlogix.config.Tenant
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback

@Integration
@Rollback
class CaseSeriesRestControllerIntegrationSpec extends BaseControllerIntegrationSpec {

    String controllerName = "caseSeriesRest"

    CaseSeriesRestController controller

    static private final Integer DEFAULT_TENANT_ID = 1

    void setup() {
        controller = autowire(CaseSeriesRestController)
    }

    void "test index action"() {
        given:
        UserService mockUserService = Mock(UserService)
        controller.userService = mockUserService
        controller.params.searchString = "seriesName1"
        CaseSeriesDateRangeInformation caseSeriesDateRangeInformation = getCaseSeriesDateRangeInformation()
        CaseDeliveryOption caseDeliveryOption = createCaseDeliveryOption()
        Preference preference = createPreference()
        User owner = createUser(preference).save(failOnError: true)
        CaseSeries.saveAll(
                new CaseSeries(seriesName: "seriesName1",
                        caseSeriesDateRangeInformation: caseSeriesDateRangeInformation,
                        createdBy: 'tester', deliveryOption: caseDeliveryOption, modifiedBy: 'tester', owner: owner, tenantId: DEFAULT_TENANT_ID.toLong()).save(failOnError: true),
                new CaseSeries(seriesName: "seriesName2",
                        caseSeriesDateRangeInformation: caseSeriesDateRangeInformation,
                        createdBy: 'tester', deliveryOption: caseDeliveryOption, modifiedBy: 'tester', owner: owner, tenantId: DEFAULT_TENANT_ID.toLong()).save(failOnError: true),
                new CaseSeries(seriesName: "seriesName3",
                        caseSeriesDateRangeInformation: caseSeriesDateRangeInformation,
                        createdBy: 'tester', deliveryOption: caseDeliveryOption, modifiedBy: 'tester', owner: owner, tenantId: DEFAULT_TENANT_ID.toLong()).save(failOnError: true)
        )
        mockUserService.getUser() >> owner

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.index()
        }

        then:
        controller.response.status == 200
        controller.response.json.aaData != null
        controller.response.json.recordsTotal == 3
    }

    private CaseDeliveryOption createCaseDeliveryOption() {
        new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF], emailToUsers: ['test@rxlogix.com'])
    }

    private CaseSeriesDateRangeInformation getCaseSeriesDateRangeInformation() {
        new CaseSeriesDateRangeInformation()
    }

    private Preference createPreference() {
        new Preference(createdBy: 'tester', modifiedBy: 'tester', locale: Locale.US)
    }

    private User createUser(Preference preference) {
        def user = new User(username: 'test', createdBy: 'tester', modifiedBy: 'tester', preference: preference)
        user.addToTenants(tenant)
        return user
    }

    private Tenant getTenant() {
        def tenant = Tenant.get(DEFAULT_TENANT_ID.toLong())
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = DEFAULT_TENANT_ID.toLong()
        return tenant.save()
    }
}
