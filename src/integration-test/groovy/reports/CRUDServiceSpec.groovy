package reports

import com.rxlogix.config.Query
import com.rxlogix.config.QueryExpressionValue
import com.rxlogix.config.ReportField
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.user.User
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import grails.validation.ValidationException
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class CRUDServiceSpec extends Specification {

    @Shared query
    @Shared adminUser

    def CRUDService
    def customMessageService

    static private final Integer DEFAULT_TENANT_ID = 1

    def setup() {

        adminUser = User.findByFullName("Admin User")

        /*
        Note:  JSONQuery is not derived from QueryExpressionValues added below, though it should be.  JSONQuery is
        constructed via Javascript and not via any server side code, so there isn't a convenient and quick means
        to create JSONQuery that would be accurate and match up with QueryExpressionValues.
        */
        def JSONQuery = """{ "all": { "containerGroups": [
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""

        query = new Query(name: "Test Query",
                queryType: QueryTypeEnum.QUERY_BUILDER,
                JSONQuery: JSONQuery,
                owner: adminUser,
                createdBy: adminUser.username,
                modifiedBy: adminUser.username, tenantId: DEFAULT_TENANT_ID.toLong()
        )
        query.addToQueryExpressionValues(
                new QueryExpressionValue(key: 1,
                        reportField: ReportField.findByNameAndIsDeleted("masterCountryId",false),
                        operator: QueryOperatorEnum.EQUALS, value: ''))

    }

    def cleanup() {}

    void "Test save" () {
        given: "An object that can be saved/created"
        //Obtained via setup() and @Shared object

        when: "CRUDService is called on to perform the save"
        def savedQuery = CRUDService.save(query)

        then: "Object was created successfully"
        assert savedQuery.id != null
        assert savedQuery.name == query.name
    }

    void "Test update" () {
        given: "An object that can be saved/updated"
        //Obtained via setup() and @Shared object
        def newName = "New Query Name"
        query.name = newName

        when: "CRUDService is called on to perform the update"
        def savedQuery = CRUDService.update(query)

        then: "Object was created successfully"
        assert savedQuery.id != null
        assert savedQuery.name == newName
    }

    void "Test soft delete"() {
        given: "An object that can be soft deleted"
        //Obtained via setup() and @Shared object
        def savedQuery = CRUDService.save(query)

        when:  "CRUDService is called on to perform the soft delete"
        def deletedQuery = CRUDService.softDelete(savedQuery, savedQuery.name)

        then: "Object was soft deleted correctly"
        assert deletedQuery.isDeleted == true
    }

    void "Test delete" () {
        given: "An object that can be deleted"
        //Obtained via setup() and @Shared object
        def savedQuery = CRUDService.save(query)

        when:  "CRUDService is called on to perform the delete"
        def queryId = savedQuery.id
        CRUDService.delete(savedQuery)

        then: "Object was deleted correctly"
        def query = Query.get(queryId)
        assert query == null
    }

    void "Test save with validation error" () {
        given: "An object that can be saved/created, but will fail validation"
        //Obtained via setup() and @Shared object
        query.name = null

        when: "CRUDService is called on to perform the save"
        def savedQuery = CRUDService.save(query)

        then: "Object was not created; ValidationException returned"
        def ve = thrown(ValidationException)
        assert savedQuery == null
    }

    void "Test update with validation error" () {
        given: "An object that can be saved/updated, but will fail validation"
        //Obtained via setup() and @Shared object
        query.name = null

        when: "CRUDService is called on to perform the update"
        def updatedQuery = CRUDService.update(query)

        then: "Object was not updated; ValidationException returned"
        def ve = thrown(ValidationException)
        assert updatedQuery == null
    }

}
