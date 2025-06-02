package com.rxlogix.api

import com.rxlogix.CalendarService
import com.rxlogix.UserService
import com.rxlogix.commandObjects.CommentCommand
import com.rxlogix.config.*
import com.rxlogix.enums.CommentTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class CommentRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<CommentRestController> {

    def setup(){
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains Tenant, User, Role, UserRole,Comment,ReportResult, Configuration, SchedulerConfigParams
    }

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
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

    void "test loadComments"(){
        User normalUser = makeNormalUser("user",[])
        Comment comment_1 = new Comment(textData: "new comment")
        comment_1.save(failOnError:true,validate:false)
        CommentCommand commentCommand = new CommentCommand(ownerId: 1,commentType: CommentTypeEnum.REPORT_RESULT)
        views['/commentRest/_comments.gsp'] = 'template content'
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.fetchComments(0..1){CommentCommand commentCommandInstance -> [comment_1]}
        controller.commentService = mockCommentService.proxyInstance()
        when:
        controller.loadComments(commentCommand)
        then:
        response.text == 'template content'
    }

    void "test loadLatestComment"(){
        Comment comment_1 = new Comment(textData: "new comment")
        comment_1.save(failOnError:true,validate:false)
        Comment comment_2 = new Comment(textData: "latest comment")
        comment_2.save(failOnError:true,validate:false)
        CommentCommand commentCommand = new CommentCommand(ownerId: 1,commentType: CommentTypeEnum.REPORT_RESULT)
        views['/commentRest/_comments.gsp'] = 'template content'
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.fetchComments(0..1){CommentCommand commentCommandInstance -> [comment_1,comment_2]}
        controller.commentService = mockCommentService.proxyInstance()
        when:
        controller.loadLatestComment(commentCommand)
        then:
        response.text == 'latest comment'
    }

    void "test save"(){
        boolean run = false
        Comment comment = new Comment(textData: "new comment")
        comment.save(failOnError:true,validate:false)
        CommentCommand commentCommand = new CommentCommand(ownerId: 1,commentType: CommentTypeEnum.REPORT_RESULT,comment: comment)
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.save(0..1){Comment commentInstance, CommentTypeEnum commentType, Long ownerId -> run = true}
        controller.commentService = mockCommentService.proxyInstance()
        ReportConfiguration configuration = new Configuration(id: 2, reportName: "test2", description: "description2", dateCreated: new Date())
        SchedulerConfigParams configParams = new SchedulerConfigParams(id: 11, runDate: new Date(), configuration: configuration, comments: [], primaryPublisherContributor: makeNormalUser("user",[]))
        configParams.setId(11L)
        def mockCalendarService = Mock(CalendarService)
        mockCalendarService.getSchedulerConfigParams(_, true) >> { return configParams}
        controller.calendarService = mockCalendarService
        when:
        controller.save(commentCommand)
        then:
        response.json == [success: true]
        run == true
    }

    void "test save commmentCommand does not passes validation"(){
        boolean run = false
        Comment comment = new Comment(textData: "new comment" *8000)
        comment.save(failOnError:true,validate:false)
        CommentCommand commentCommand = new CommentCommand(ownerId: 1,commentType: CommentTypeEnum.REPORT_RESULT,comment: comment)
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.save(0..1){Comment commentInstance, CommentTypeEnum commentType, Long ownerId -> run = true}
        controller.commentService = mockCommentService.proxyInstance()
        ReportConfiguration configuration = new Configuration(id: 2, reportName: "test2", description: "description2", dateCreated: new Date())
        SchedulerConfigParams configParams = new SchedulerConfigParams(id: 11, runDate: new Date(), configuration: configuration, comments: [], primaryPublisherContributor: makeNormalUser("user",[]))
        configParams.setId(11L)
        def mockCalendarService = Mock(CalendarService)
        mockCalendarService.getSchedulerConfigParams(_, true) >> { return configParams}
        controller.calendarService = mockCalendarService
        when:
        controller.save(commentCommand)
        then:
        response.json == [msg:"com.rxlogix.config.caseDataQuality.comment.maxSize.exceeded", error:true, errors:[]]
        run == false
    }

    void "test save validation error"(){
        boolean run = false
        Comment comment = new Comment(textData: "new comment")
        comment.save(failOnError:true,validate:false)
        CommentCommand commentCommand = new CommentCommand(ownerId: 1,commentType: CommentTypeEnum.REPORT_RESULT,comment: comment)
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.save(0..1){Comment commentInstance, CommentTypeEnum commentType, Long ownerId ->
            run = true
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.commentService = mockCommentService.proxyInstance()
        ReportConfiguration configuration = new Configuration(id: 2, reportName: "test2", description: "description2", dateCreated: new Date())
        SchedulerConfigParams configParams = new SchedulerConfigParams(id: 11, runDate: new Date(), configuration: configuration, comments: [], primaryPublisherContributor: makeNormalUser("user",[]))
        configParams.setId(11L)
        def mockCalendarService = Mock(CalendarService)
        mockCalendarService.getSchedulerConfigParams(_, true) >> { return configParams}
        controller.calendarService = mockCalendarService
        when:
        controller.save(commentCommand)
        then:
        response.json.msg == "default.system.error.message"
        run == true
    }

    void "test delete"(){
        boolean run = false
        Comment comment = new Comment(textData: "new comment")
        comment.save(failOnError:true,validate:false)
        CommentCommand commentCommand = new CommentCommand(ownerId: 1,commentType: CommentTypeEnum.REPORT_RESULT,comment: comment)
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.delete(0..1){Comment commentInstance, Long ownerId, CommentTypeEnum commentType-> run = true}
        mockCommentService.demand.fetchComments(0..1){CommentCommand commentCommandInstance -> []}
        controller.commentService = mockCommentService.proxyInstance()
        ReportConfiguration configuration = new Configuration(id: 2, reportName: "test2", description: "description2", dateCreated: new Date())
        SchedulerConfigParams configParams = new SchedulerConfigParams(id: 11, runDate: new Date(), configuration: configuration, comments: [], primaryPublisherContributor: makeNormalUser("user",[]))
        configParams.setId(11L)
        def mockCalendarService = Mock(CalendarService)
        mockCalendarService.getSchedulerConfigParams(_, true) >> { return configParams}
        controller.calendarService = mockCalendarService
        when:
        controller.delete(commentCommand)
        then:
        response.json == [isCommentListEmpty:true, success:true]
        run == true
    }

    void "test delete isCommentListEmpty false"(){
        boolean run = false
        Comment comment = new Comment(textData: "new comment")
        comment.save(failOnError:true,validate:false)
        CommentCommand commentCommand = new CommentCommand(ownerId: 1,commentType: CommentTypeEnum.REPORT_RESULT,comment: comment)
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.delete(0..1){Comment commentInstance, Long ownerId, CommentTypeEnum commentType-> run = true}
        mockCommentService.demand.fetchComments(0..1){CommentCommand commentCommandInstance -> [comment]}
        controller.commentService = mockCommentService.proxyInstance()
        ReportConfiguration configuration = new Configuration(id: 2, reportName: "test2", description: "description2", dateCreated: new Date())
        SchedulerConfigParams configParams = new SchedulerConfigParams(id: 11, runDate: new Date(), configuration: configuration, comments: [], primaryPublisherContributor: makeNormalUser("user",[]))
        configParams.setId(11L)
        def mockCalendarService = Mock(CalendarService)
        mockCalendarService.getSchedulerConfigParams(_, true) >> { return configParams}
        controller.calendarService = mockCalendarService
        when:
        controller.delete(commentCommand)
        then:
        response.json == [isCommentListEmpty:false, success:true]
        run == true
    }

    void "test delete error"(){
        boolean run = false
        Comment comment = new Comment(textData: "new comment")
        comment.save(failOnError:true,validate:false)
        CommentCommand commentCommand = new CommentCommand(ownerId: 1,commentType: CommentTypeEnum.REPORT_RESULT,comment: comment)
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.delete(0..1){Comment commentInstance, Long ownerId, CommentTypeEnum commentType->
            run = true
            throw new DataIntegrityViolationException("message")
        }
        controller.commentService = mockCommentService.proxyInstance()
        ReportConfiguration configuration = new Configuration(id: 2, reportName: "test2", description: "description2", dateCreated: new Date())
        SchedulerConfigParams configParams = new SchedulerConfigParams(id: 11, runDate: new Date(), configuration: configuration, comments: [], primaryPublisherContributor: makeNormalUser("user",[]))
        configParams.setId(11L)
        def mockCalendarService = Mock(CalendarService)
        mockCalendarService.getSchedulerConfigParams(_, true) >> { return configParams}
        controller.calendarService = mockCalendarService
        when:
        controller.delete(commentCommand)
        then:
        response.json == [error: true, msg: "default.system.error.message"]
        run == true
    }
}
