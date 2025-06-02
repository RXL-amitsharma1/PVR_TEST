package reports

import com.rxlogix.config.*
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.test.TestUtils
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class UserServiceSpec extends Specification {

    static private final Long DEFAULT_TENANT_ID = 1

    def userService
    def CRUDService

    @Shared
    User user
    @Shared
    Preference preference

    def setup() {
        user = User.findByFullName("Admin User")
        preference = new Preference(locale: new Locale("en"), createdBy: user.username, modifiedBy: user.username)

        if (!Tenant.read(DEFAULT_TENANT_ID)) {
            Tenant tenant = new Tenant(name: "Default Tenant", active: false)
            tenant.id = DEFAULT_TENANT_ID
            tenant.save()
        }

        if (Configuration.countByReportName('Test Configuration')) {
            return
        }

        def template = ReportTemplate.findByName(ReportTemplate.CIOMS_I_TEMPLATE_NAME)
        def deliveryOption = new DeliveryOption()
        deliveryOption.addToSharedWith(user)
        Configuration configuration = new Configuration(reportName: "Test Configuration", owner: user, deliveryOption: deliveryOption,tenantId: DEFAULT_TENANT_ID)
        Date next = new Date()
        configuration.setNextRunDate(next)
        TemplateQuery tq1 = new TemplateQuery(template: template,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: user.username, modifiedBy: user.username)
        configuration.addToTemplateQueries(tq1)
        configuration.createdBy = user.username
        configuration.modifiedBy = user.username
        SourceProfile sourceProfile = SourceProfile.findAll()[0]
        configuration.sourceProfile = (sourceProfile ?: TestUtils.createSourceProfile())
        configuration.save(failOnError: true)
    }

    def cleanup() {

    }

    def "Test getUser() - logged in"() {
        when:
        SpringSecurityUtils.reauthenticate "admin", "admin"
        def user = userService.getUser()

        then:
        assert user != null
    }

    def "Test getUser() - not logged in"() {
        when:
        SecurityContextHolder.clearContext()
        def user = userService.getUser()

        then:
        assert user == null
    }

    def "Test setOwnershipAndModifier() - object has modifiers"() {
        given:
        ReportFieldInfo column = new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterRptTypeId", false), argusName: "cm.REPORT_TYPE_ID")
        ReportFieldInfoList columns = new ReportFieldInfoList()
        columns.addToReportFieldInfoList(column)
        def template = new CaseLineListingTemplate(name: "CLL",
                templateType: TemplateTypeEnum.CASE_LINE,
                columnList: columns,
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username
        )


        when:
        userService.setOwnershipAndModifier(template)

        then:
        assert template.createdBy == user.username
        assert template.modifiedBy == user.username
    }

    def "Test setOwnershipAndModifier() - object does not have modifiers"() {
        given:
        ReportFieldInfo column = new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterRptTypeId", false), argusName: "cm.REPORT_TYPE_ID")

        when:
        userService.setOwnershipAndModifier(column)

        then:
        /*This is basically a no-op.  Just looking to confirm that a call to setOwnershipAndModifier() for an object
          that can't make use of it doesn't throw an exception and break.
        */

        noExceptionThrown()
    }

    def "Test getAllEmails(Configuration config) -- unique email added to deliveryOption.emailToUsers"() {
        given:
        Configuration configuration = Configuration.findByReportName('Test Configuration')
        //This email should not exist in DB, thereby increasing count of expected emails by 1
        configuration.deliveryOption.addToEmailToUsers("5e4fc90rw@testemail.com")
        configuration.deliveryOption.addToAttachmentFormats(ReportFormatEnum.PDF)
        CRUDService.save(configuration)

        Set uniqueEmails = User.findAllByEnabledAndEmailIsNotNull(true).collect { it.email }.toSet() as SortedSet

        when:
        //allEmails will include manually added emails to deliveryOption.emailToUsers
        def allEmails = userService.getAllEmails(configuration)

        then:
        //The addition of + 1 is to account for the unique email added to deliveryOption?.emailToUsers which is not in DB
        assert allEmails.size() == uniqueEmails.size() + 1

        cleanup:
        //(Some cleanup needed due to bug described below)
        //http://stackoverflow.com/questions/27059236/grails-integrationspec-rollback-after-each-test-case-possible
        configuration.deliveryOption.emailToUsers.clear()
        configuration.deliveryOption.addToEmailToUsers(user.email)
        CRUDService.update(configuration)
    }

    def "Test getAllEmails(Configuration config) -- existing email added to deliveryOption.emailToUsers"() {
        given:
        Configuration configuration = Configuration.findByReportName('Test Configuration')
        //This email should exist in DB and thus be filtered out
        //todo:  this should be prevented via a unique constraint, not filtered out afterwards.
        //todo:  see comments on "Test getAllEmails()" below.
        configuration.deliveryOption.addToAttachmentFormats(ReportFormatEnum.PDF)
        configuration.deliveryOption.addToEmailToUsers(configuration.owner.email)  //Added for finding email of existing user (To remove hardcoding).
        CRUDService.save(configuration)

        Set uniqueEmails = User.findAllByEnabledAndEmailIsNotNull(true).collect { it.email }.toSet() as SortedSet

        when:
        //allEmails will include manually added emails to deliveryOption.emailToUsers, unless they are duplicates
        def allEmails = userService.getAllEmails(configuration)

        then:
        /*
        In this case, we're expecting a duplicate email added to deliveryOption.emailToUsers, so the final list will
        have it filtered out
         */
        assert allEmails.size() == uniqueEmails.size()
    }

    def "Test getAllEmails()"() {
        //todo:  This test surfaced a bug -- we don't have a unique constraint on emailToUsers to prevent duplicates.
        /*
            Not only do emails need to be unique in the DB, but they need be unique amongst the manually entered emails
            in isolation, as well as when those emails are combined with emails in the DB.
        */

        /*
           PVR-2681 added a change to getAllEmails() to make them unique before sorting them.
           The real issue is not making them unique in a method call -- the real issue is that
           we need them to be always unique.
        */

        given:
        //Note:  we never want to bring back records where email is null.
        Set uniqueEmails = User.findAllByEnabledAndEmailIsNotNull(true).collect { it.email }.toSet() as SortedSet

        when:
        def allEmails = userService.getAllEmails()

        then:
        assert allEmails.size() == uniqueEmails.size()
    }

    def "Test getActiveUsers()"() {
        when:
        User.list().each { it.enabled = false }

        User user1 = new User(username: "newUser1", preference: preference, createdBy: user.username, modifiedBy: user.username)
        user1.addToTenants(Tenant.get(DEFAULT_TENANT_ID))
        user1.save(flush: true, failOnError: true)
        User user2 =new User(username: "newUser2", preference: preference, createdBy: user.username, modifiedBy: user.username)
        user2.addToTenants(Tenant.get(DEFAULT_TENANT_ID))
        user2.save(flush: true, failOnError: true)

        then:
        assert userService.getActiveUsers().size() == 2
    }

    def "Test getAdminUsers()"() {
        when:
        //Reset all existing users that might be admin to test with a known baseline
        def role = Role.findByAuthority("ROLE_ADMIN")
        role.authority = "ROLE_OLD_ADMIN"
        role.save(flush: true, failOnError: true)

        role = Role.findByAuthority("ROLE_DEV")
        role.authority = "ROLE_OLD_DEV"
        role.save(flush: true, failOnError: true)

        def newAdminRole = new Role(authority: "ROLE_ADMIN", createdBy: user.username, modifiedBy: user.username).save(flush: true, failOnError: true)
        def newDevRole = new Role(authority: "ROLE_DEV", createdBy: user.username, modifiedBy: user.username).save(flush: true, failOnError: true)

        def newUser1 = new User(username: "newAdminUser1", preference: preference, createdBy: user.username, modifiedBy: user.username)
        newUser1.addToTenants(Tenant.get(DEFAULT_TENANT_ID))
        newUser1.save(flush: true, failOnError: true)
        def newUser2 = new User(username: "newDevUser2", preference: preference, createdBy: user.username, modifiedBy: user.username)
        newUser2.addToTenants(Tenant.get(DEFAULT_TENANT_ID))
        newUser2.save(flush: true, failOnError: true)

        UserRole.create(newUser1, Role.findByAuthority(newAdminRole.authority), true)
        UserRole.create(newUser2, Role.findByAuthority(newDevRole.authority), true)

        then:
        //Only the two users created should be found
        assert userService.getAdminUsers().size() == 2
    }

    def "Test createUser() - user doesn't exist"() {
        when:
        def adminRole = Role.findByAuthority("ROLE_ADMIN")
        def tenant = Tenant.get(DEFAULT_TENANT_ID)
        def user = userService.createUser("createUserTest", preference, [adminRole.authority], user.username, tenant)

        then:
        assert User.findByUsername("createUserTest")
        assert UserRole.findByUser(user).role == adminRole
    }

    def "Test createUser() - user already exists"() {
        when:
        def userCount = User.count
        def adminRole = Role.findByAuthority("ROLE_ADMIN")
        def tenant = Tenant.get(DEFAULT_TENANT_ID)
        userService.createUser("admin", preference, [adminRole.authority], user.username, tenant)

        then:
        //The user can't/won't be added and should exit gracefully
        assert User.count == userCount
    }

}
