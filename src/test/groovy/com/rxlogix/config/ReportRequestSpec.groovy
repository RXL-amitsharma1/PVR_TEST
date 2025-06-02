package com.rxlogix.config


import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class ReportRequestSpec extends Specification implements DomainUnitTest<ReportRequest> {
    public static final user = "unitTest"

    def setupSpec() {
        mockDomains User, Role, UserRole, ReportRequestPriority, ReportRequestType, DateRangeType, ReportRequestAttachment, Tenant
    }

    def setup() {
    }

    def cleanup() {
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
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Admin User", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save()
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    private User makeAssignedToUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'dev', password: 'dev', fullName: "DEV User", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.save()
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    void "test getEmptyAttachmentsString when one attachment has empty string"() {
        given:
        ReportRequestPriority priority = new ReportRequestPriority(id: 1, name: "High", description: "High priority", createdBy: user, modifiedBy: user)
        ReportRequestType type = new ReportRequestType(id: 1, name: "Adhoc Report", description: "Adhoc Report Type", createdBy: user, modifiedBy: user)
        DateRangeType dateRangeType = new DateRangeType(id: 1, name: "Version Date")
        priority.save(flush: true)
        type.save(flush: true)
        dateRangeType.save(flush: true)
        ReportRequestAttachment attachment1 = new ReportRequestAttachment(id: 1, name: "attachment-1", createdBy: user, modifiedBy: user)
        ReportRequestAttachment attachment2 = new ReportRequestAttachment(id: 2, name: "", createdBy: user, modifiedBy: user)
        ReportRequest reportRequest = new ReportRequest(id: 1, reportName: "Test RR-1", priority: priority, description: "Test RR-1 request", reportRequestType: type, assignedTo: makeAssignedToUser(), owner: makeAdminUser(), dueDate: new Date(), dateRangeType: dateRangeType, asOfVersionDate: new Date(), createdBy: user, modifiedBy: user)
        reportRequest.save(flush: true,validate:false)
        attachment1.reportRequest = reportRequest
        attachment2.reportRequest = reportRequest
        attachment1.save(flush: true)
        attachment2.save(flush: true)

        when:
        String attachmentsName = reportRequest.getAttachmentsString()

        then:
        attachmentsName == "attachment-1"
    }

    void "test getEmptyAttachmentsString when both attachments has empty string"() {
        given:
        ReportRequestPriority priority = new ReportRequestPriority(id: 1, name: "High", description: "High priority", createdBy: user, modifiedBy: user)
        ReportRequestType type = new ReportRequestType(id: 1, name: "Adhoc Report", description: "Adhoc Report Type", createdBy: user, modifiedBy: user)
        DateRangeType dateRangeType = new DateRangeType(id: 1, name: "Version Date")
        priority.save(flush: true)
        type.save(flush: true)
        dateRangeType.save(flush: true)
        ReportRequestAttachment attachment1 = new ReportRequestAttachment(id: 1, name: "", createdBy: user, modifiedBy: user)
        ReportRequestAttachment attachment2 = new ReportRequestAttachment(id: 2, name: "", createdBy: user, modifiedBy: user)
        ReportRequest reportRequest = new ReportRequest(id: 1, reportName: "Test RR-1", priority: priority, description: "Test RR-1 request", reportRequestType: type, assignedTo: makeAssignedToUser(), owner: makeAdminUser(), dueDate: new Date(), dateRangeType: dateRangeType, asOfVersionDate: new Date(), createdBy: user, modifiedBy: user)
        reportRequest.save(flush: true,validate:false)
        attachment1.reportRequest = reportRequest
        attachment2.reportRequest = reportRequest
        attachment1.save(flush: true)
        attachment2.save(flush: true)

        when:
        String attachmentsName = reportRequest.getAttachmentsString()

        then:
        attachmentsName == ''
    }

    void "test getAttachmentsString"() {
        given:
        ReportRequestPriority priority = new ReportRequestPriority(id: 1, name: "High", description: "High priority", createdBy: user, modifiedBy: user)
        ReportRequestType type = new ReportRequestType(id: 1, name: "Adhoc Report", description: "Adhoc Report Type", createdBy: user, modifiedBy: user)
        DateRangeType dateRangeType = new DateRangeType(id: 1, name: "Latest Date")
        ReportRequestAttachment attachment1 = new ReportRequestAttachment(id: 1, name: "attachment-1", createdBy: user, modifiedBy: user, )
        ReportRequestAttachment attachment2 = new ReportRequestAttachment(id: 2, name: "attachment-2", createdBy: user, modifiedBy: user)
        priority.save(flush: true)
        type.save(flush: true)
        dateRangeType.save(flush: true)
        ReportRequest reportRequest = new ReportRequest(id: 1, reportName: "Test RR-1", priority: priority, description: "Test RR-1 request", reportRequestType: type, owner: makeAdminUser(), assignedTo: makeAssignedToUser(), asOfVersionDate: new Date(), dateRangeType: dateRangeType, createdBy: user, modifiedBy: user)
        reportRequest.dueDate = new Date()
        reportRequest.addToAttachments(attachment1)
        reportRequest.addToAttachments(attachment2)
        reportRequest.save(flush: true)
        when:
        String attachmentsName = reportRequest.getAttachmentsString()

        then:
        attachmentsName == "attachment-1,attachment-2"
    }
}
