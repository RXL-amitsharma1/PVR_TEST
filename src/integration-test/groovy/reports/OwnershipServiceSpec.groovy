package reports


import com.rxlogix.config.Configuration
import com.rxlogix.config.Query
import com.rxlogix.config.ReportTemplate
import com.rxlogix.enums.TransferTypeEnum
import com.rxlogix.user.User
import grails.testing.mixin.integration.Integration
import org.junit.Ignore;
import spock.lang.Specification;

/**
 * Created by glennsilverman on 8/31/15.
 */

@Integration
@Ignore
public class OwnershipServiceSpec extends Specification {

    def setup() {}
    def cleanup() {}
    def userService
    def ownershipService


    void "test updateOwners"(){
        when:
        Configuration.withNewSession {
            def domainList = [Configuration, Query, ReportTemplate];
            def adminUser = User.findByUsername("admin")
            def otherUser = User.findByUsername("jgriffin")
            ownershipService.updateOwners([newUser: adminUser, previousOwner: otherUser, transferType: TransferTypeEnum.OWNERSHIP]);
        }
        then:
        noExceptionThrown()


    }
}
