package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class ReportExecutorServiceSpec extends Specification implements DataTest, ServiceUnitTest<ReportExecutorService> {

    @Shared User normalUser
    @Shared Preference preference
    @Shared Configuration configuration

    def setupSpec() {
        mockDomains CaseLineListingTemplate, Tag, SourceColumnMaster, Configuration, ReportResult, ReportFieldInfoList, ReportFieldInfo, User, Role, UserRole, Preference, Tenant, TemplateQuery, SharedWith, ExecutedConfiguration, ExecutedPeriodicReportConfiguration, ExecutedTemplateQuery, DeliveryOption, ExecutedDeliveryOption, ReportTemplate, ReportField, TemplateQuery, Query, TemplateSet, ExecutedIcsrProfileConfiguration
    }

    def setup() {
        def username = "unitTest"
        preference = new Preference(locale: new Locale("en"), createdBy: username, modifiedBy: username)
        normalUser = createUser(username, "ROLE_TEMPLATE_VIEW")
        configuration = createConfiguration(normalUser)
    }
    

    }
