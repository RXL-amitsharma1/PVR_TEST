package com.rxlogix

import com.rxlogix.config.ExecutedPublisherSource
import com.rxlogix.config.Tenant
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherTemplate
import com.rxlogix.config.publisher.PublisherTemplateParameter
import com.rxlogix.publisher.PublisherService
import com.rxlogix.publisher.PublisherSourceService
import com.rxlogix.publisher.PublisherTemplateController
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification

class PublisherTemplateControllerSpec extends Specification implements DataTest, ControllerUnitTest<PublisherTemplateController> {

    public static final user = "unitTest"

    def setupSpec() {
        mockDomains User, PublisherTemplate, PublisherTemplateParameter, Role, UserRole, Preference, Tenant,ExecutedPublisherSource, PublisherConfigurationSection
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

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    void "test index"(){
        when:
        controller.index()

        then:
        response.status == 200
    }

    void "test list"(){
        given:
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.publisherTemplateBySearchString = { String search->
            new Object() {
                List list(LinkedHashMap H) {[max: 10, offset: 0, sort: "name", order: ""]
                    [template]
                }
                int count(Object o) {
                    return 1
                }
            }
        }

        when:
        params.searchString = ''
        params.length = 10
        params.start = 0
        params.direction = 'asc'
        params.sort = 'name'
        controller.list()

        then:
        response.status == 200
        response.json.aaData[0].name == 'newPublisherTemplate'
        response.json.aaData[0].description == 'testDescription'
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test getTemplateParameters"(){
        given:
        List <PublisherTemplateParameter> parameters = [new PublisherTemplateParameter(name: 'companyName', title: 'Company', description: 'standardParameter', value: 'testValue', type:PublisherTemplateParameter.Type.TEXT),
                                                        new PublisherTemplateParameter(name: 'CurrentDate', title: 'Date', description: 'standardParameterDate', value: '(new Date()).format("dd-MMM-yyyy")', type:PublisherTemplateParameter.Type.CODE)]
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false, parameters: parameters)
        PublisherTemplate.metaClass.static.read = { Long id ->
            return template
        }

        when:
        controller.getTemplateParameters(1L)

        then:
        response.status==200
        response.json[0].name=='companyName'
        response.json[0].description=='standardParameter'
        response.json[0].title=='Company'
        response.json[0].value=='testValue'
    }

    void "test create"(){
        given:
        List <PublisherTemplateParameter> parameters = [new PublisherTemplateParameter(name: 'companyName', title: 'Company', description: 'standardParameter', value: 'testValue', type:PublisherTemplateParameter.Type.TEXT),
                                                        new PublisherTemplateParameter(name: 'CurrentDate', title: 'Date', description: 'standardParameterDate', value: '(new Date()).format("dd-MMM-yyyy")', type:PublisherTemplateParameter.Type.CODE)]
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false, parameters: parameters)

        when:
        controller.create(template)

        then:
        response.status==200
    }

    void "test save -- success"(){
        given:
        def multipartFile = new GrailsMockMultipartFile('file', 'publisherTemplateFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_) >> {return true}
        controller.CRUDService = mockCRUDService
        def mockOneDriveRestService=Mock(OneDriveRestService)
        mockOneDriveRestService.getCheckInFile(_,_) >> {return  new byte[0]}
        controller.oneDriveRestService=mockOneDriveRestService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {return makeAdminUser()}
        controller.userService = mockUserService

        when:
        params.name='testName'
        params.description='testDescription'
        params."parameters.name"=['parameterName']
        params."parameters.title"=['parameterTitle']
        params."parameters.description"=['parameterDescription']
        params."parameters.value"=['parameterValue']
        params."parameters.type"=[PublisherTemplateParameter.Type.TEXT]
        params.lockCode = lockCodeVersion
        controller.save()

        then:
        response.status==200
        response.json.message=='default.created.message'

        where:
        lockCodeVersion << ['testId', null]
    }

    void "test save -- failure"(){
        given:
        def multipartFile = new GrailsMockMultipartFile('file', 'publisherTemplateFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception",new PublisherTemplate().errors)}
        controller.CRUDService = mockCRUDService

        when:
        params.name='testName'
        params.description='testDescription'
        params."parameters.name"=['parameterName']
        params."parameters.title"=['parameterTitle']
        params."parameters.description"=['parameterDescription']
        params."parameters.value"=['parameterValue']
        params."parameters.type"=[PublisherTemplateParameter.Type.TEXT]
        controller.save()

        then:
        response.status==500
    }

    void "test edit not found"(){
        given:
        PublisherTemplate.metaClass.static.read={Long id -> return null}
        when:
        params.id=2L
        controller.edit(2L)
        then:
        response.status==302
        response.redirectedUrl=='/publisherTemplate/index'
    }

    void "test edit found"(){
        given:
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.read={Long id->template }
        when:
        controller.edit(1L)
        then:
        response.status==200
    }

    void "test update found with try with no instance parameters"(){
        given:
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.get={Long id->template }
        def multipartFile = new GrailsMockMultipartFile('file', 'publisherTemplateFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_) >> {return true}
        controller.CRUDService = mockCRUDService
        def mockOneDriveRestService=Mock(OneDriveRestService)
        mockOneDriveRestService.getCheckInFile(_,_) >> {return  new byte[0]}
        controller.oneDriveRestService=mockOneDriveRestService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        when:
        params.id=2L
        controller.update()
        then:
        response.status==200
        response.json.message=='default.updated.message'
    }

    void "test update found with try success"(){
        given:
        PublisherTemplate.metaClass.static.get={Long id-> new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false,parameters: [new PublisherTemplateParameter(name: "test1",title: "title1",description:"test description",value: "test value")])}
        def multipartFile = new GrailsMockMultipartFile('file', 'publisherTemplateFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_) >> {return true}
        controller.CRUDService = mockCRUDService
        def mockOneDriveRestService=Mock(OneDriveRestService)
        mockOneDriveRestService.getCheckInFile(_,_) >> {return  new byte[0]}
        controller.oneDriveRestService=mockOneDriveRestService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        when:
        params.id=2L
        params.name='testName'
        params.description='testDescription'
        params."parameters.name"=['parameterName']
        params."parameters.title"=['parameterTitle']
        params."parameters.description"=['parameterDescription']
        params."parameters.value"=['parameterValue']
        params."parameters.type"=[PublisherTemplateParameter.Type.TEXT]
        controller.update()
        then:
        response.status==200
        response.json.message=='default.updated.message'
    }

    void "test update validation exception"(){
        given:
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.get={Long id->template }
        def multipartFile = new GrailsMockMultipartFile('file', 'publisherTemplateFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception",template.errors)}
        controller.CRUDService = mockCRUDService
        def mockOneDriveRestService=Mock(OneDriveRestService)
        mockOneDriveRestService.getCheckInFile(_,_) >> {return  new byte[0]}
        controller.oneDriveRestService=mockOneDriveRestService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        when:
        params.id=2L
        params.name='testName'
        params.description='testDescription'
        params."parameters.name"=['parameterName']
        params."parameters.title"=['parameterTitle']
        params."parameters.description"=['parameterDescription']
        params."parameters.value"=['parameterValue']
        params."parameters.type"=[PublisherTemplateParameter.Type.TEXT]
        controller.update()
        then:
        response.status==500
    }

    void "test show not found"(){
        given:
        PublisherTemplate.metaClass.static.read={null}
        when:
        controller.show()
        then:
        response.status==302
        response.redirectedUrl=='/publisherTemplate/index'
    }

    void "test show found"(){
        given:
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.read={Long id->template }
        when:
        controller.show(1L)
        then:
        response.status==200
    }

    void "test delete not found"(){
        given:
        PublisherTemplate.metaClass.static.read={null}
        when:
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=='/publisherTemplate/index'
    }

    void "test delete found with try"(){
        given:
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.read={Long id->template }
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_) >> {return true}
        controller.CRUDService = mockCRUDService
        when:
        controller.delete(2L)
        then:
        response.status==302
        response.redirectedUrl=='/publisherTemplate/index'
    }

    void "test delete found with validation exception"(){
        given:
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.read={Long id->template }
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_) >> {throw new ValidationException("Validation Exception",template.errors)}
        controller.CRUDService = mockCRUDService
        when:
        controller.delete(2L)
        then:
        response.status==302
        response.redirectedUrl=='/publisherTemplate/index'
    }

    void "test downloadAttachment not found"(){
        given:
        PublisherTemplate.metaClass.static.get={null}
        when:
        controller.downloadAttachment()
        then:
        response.status==302
        response.redirectedUrl=='/publisherTemplate/index'
    }

    void "test downloadAttachment found"(){
        given:
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.get={Long id->template }
        when:
        params.id=2L
        controller.downloadAttachment()
        then:
        response.status==200
    }

    void "test fetchParameters"(){
        given:
        def multipartFile = new GrailsMockMultipartFile('file', 'publisherTemplateFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        List <PublisherTemplateParameter> parameters = [new PublisherTemplateParameter(name: 'companyName', title: 'Company', description: 'standardParameter', value: 'testValue', type:PublisherTemplateParameter.Type.TEXT),
                                                        new PublisherTemplateParameter(name: 'CurrentDate', title: 'Date', description: 'standardParameterDate', value: '(new Date()).format("dd-MMM-yyyy")', type:PublisherTemplateParameter.Type.CODE)]
        def mockPublisherService=Mock(PublisherService)
        mockPublisherService.fetchParameters(_)>>{["validParam":[1,2,3]]}
        controller.publisherService=mockPublisherService

        when:
        controller.fetchParameters()
        then:
        response.status==500
    }

    void "test fetchParametersFromOneDrive"(){
        given:
        def multipartFile = new GrailsMockMultipartFile('file', 'publisherTemplateFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        List <PublisherTemplateParameter> parameters = [new PublisherTemplateParameter(name: 'companyName', title: 'Company', description: 'standardParameter', value: 'testValue', type:PublisherTemplateParameter.Type.TEXT),
                                                        new PublisherTemplateParameter(name: 'CurrentDate', title: 'Date', description: 'standardParameterDate', value: '(new Date()).format("dd-MMM-yyyy")', type:PublisherTemplateParameter.Type.CODE)]
        def mockPublisherService=Mock(PublisherService)
        mockPublisherService.fetchParameters(_)>>{["validParam":[1,2,3]]}
        controller.publisherService=mockPublisherService
        def mockOneDriveRestService=Mock(OneDriveRestService)
        mockOneDriveRestService.getCheckInFile(_,_) >> {return  new byte[0]}
        controller.oneDriveRestService=mockOneDriveRestService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        when:
        controller.fetchParametersFromOneDrive()
        then:
        response.status==200
    }

    void "test testScript"(){
        given:
        ExecutedPublisherSource attachment = new ExecutedPublisherSource()
        attachment.save(failOnError:true,validate:false)
        def mockPublisherSourceService=Mock(PublisherSourceService)
        mockPublisherSourceService.runScript(_)>>{[log:1] }
        controller.publisherSourceService=mockPublisherSourceService
        when:
        params.name="test"
        params.script="test script"
        params.fileType= "WORD"
        controller.testScript()
        then:
        response.status==200
    }

    void "test publisherTemplateParameterList"() {
        given:
        PublisherTemplate template = new PublisherTemplate(id: 1L, name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false)
        PublisherTemplate.metaClass.static.read = { Long id ->
            return template
        }
        PublisherTemplateParameter.metaClass.static.publisherTemplateParameterByTemplateAndSearchString = { PublisherTemplate t, String search->
            new Object() {
                List list(LinkedHashMap H) {[max: 10, offset: 0, sort: "name", order: ""]
                    [new PublisherTemplateParameter(name: 'company_Name', title: 'test_Title', description: 'test_Description', value: 'test_Value', type: PublisherTemplateParameter.Type.TEXT),
                     new PublisherTemplateParameter(name: 'product_Name', title: 'new_Title', description: 'new_Description', value: 'new_Value', type: PublisherTemplateParameter.Type.QUESTIONNAIRE)]
                }
                int count(Object o) {
                    return 2
                }
            }
        }

        when:
        params.id = 1L
        params.searchString = ''
        params.length = 10
        params.start = 0
        params.direction = 'asc'
        params.sort = 'name'
        controller.publisherTemplateParameterList()

        then:
        response.json.recordsFiltered == 2
        response.json.recordsTotal == 2
        response.json.aaData[0].name == 'company_Name'
        response.json.aaData[1].name == 'product_Name'
    }

    def "test getTemplateNameDescription"(){
        given:
        List <PublisherTemplateParameter> parameters = [new PublisherTemplateParameter(name: 'companyName', title: 'Company', description: 'standardParameter', value: 'testValue', type:PublisherTemplateParameter.Type.TEXT),
                                                        new PublisherTemplateParameter(name: 'CurrentDate', title: 'Date', description: 'standardParameterDate', value: '(new Date()).format("dd-MMM-yyyy")', type:PublisherTemplateParameter.Type.CODE)]
        PublisherTemplate template = new PublisherTemplate(name: 'newPublisherTemplate', fileName: 'publisherTemplateFile', description: 'testDescription', isDeleted: false, parameters: parameters)
        PublisherTemplate.metaClass.static.read = { Long id ->
            return template
        }
        when:
        controller.getTemplateNameDescription(1L)
        then:
        response.status == 200
        response.json.text == 'newPublisherTemplate'
        response.json.qced == false
    }

    def "test getHiddenValues"(){
        when:
        def params = [hidden : true, name : 'parameterName', title : 'parameterTitle']
        controller.getHiddenValues(params)
        then:
        response.status==200
    }
}