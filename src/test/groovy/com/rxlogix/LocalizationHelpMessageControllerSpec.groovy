package com.rxlogix

import com.rxlogix.config.Tenant
import com.rxlogix.localization.*
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([MiscUtil, User, Localization, ReleaseNotesNotifier, ReleaseNotesItem])
class LocalizationHelpMessageControllerSpec extends Specification implements DataTest, ControllerUnitTest<LocalizationHelpMessageController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains LocalizationHelpMessage, Localization, ReleaseNotes, ReleaseNotesItem, User, Role, UserRole, Tenant, ReleaseNotesNotifier,
                SystemNotification, InteractiveHelp
    }

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: "admin", password: 'user', fullName: "admin", preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
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

    void "test index"() {
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test releaseNotes"() {
        when:
        controller.releaseNotes()
        then:
        response.status == 200
    }

    void "test releaseNotesItem"() {
        when:
        controller.releaseNotesItem()
        then:
        response.status == 200
    }

    void "test interactiveHelp"() {
        when:
        controller.releaseNotesItem()
        then:
        response.status == 200
    }

    void "test systemNotification"() {
        when:
        controller.systemNotification()
        then:
        response.status == 200
    }

    void "test list"() {
        given:
        Localization.metaClass.static.fetchByString = { String search, Boolean b ->
            new Object() {
                List list(Object o) {
                    [new Localization(id: 1, code: "code1", locale: "locale1", text: "text"),
                     new Localization(id: 2, code: "code2", locale: "locale2", text: "text")]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:

        controller.list()
        then:
        response.json != null
        response.json.size() == 3
        response.json.aaData[0].size() == 4
        response.json.aaData[0].code == "code1"
    }

    void "test listReleaseNotes"() {
        given:
        ReleaseNotes.metaClass.static.fetchByString = { String search ->
            new Object() {
                List list(Object o) {
                    [new ReleaseNotes(id: 1, title: "title1", releaseNumber: "locale1"),
                     new ReleaseNotes(id: 2, title: "title2", releaseNumber: "locale2")]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:

        controller.listReleaseNotes()
        then:
        response.json != null
        response.json.size() == 3
        response.json.aaData[0].size() == 3
        response.json.aaData[0].title == "title1"
    }

    void "test listReleaseNotesItem"() {
        given:
        ReleaseNotesItem.metaClass.static.fetchByString = { String search ->
            new Object() {
                List list(Object o) {
                    [new ReleaseNotesItem(id: 1, title: "title1", releaseNotes: new ReleaseNotes(releaseNumber: "releaseNumber")),
                     new ReleaseNotesItem(id: 2, title: "title2", releaseNotes: new ReleaseNotes(releaseNumber: "releaseNumber"))]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:

        controller.listReleaseNotesItem()
        then:
        response.json != null
        response.json.size() == 3
        response.json.aaData[0].size() == 3
        response.json.aaData[0].title == "title1"
    }

    void "test listSystemNotification"() {
        given:
        SystemNotification.metaClass.static.fetchByString = { String search ->
            new Object() {
                List list(Object o) {
                    [new SystemNotification(id: 1, title: "title1", description: "desc", details: "det"),
                     new SystemNotification(id: 2, title: "title2", description: "desc", details: "det")]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:

        controller.listSystemNotification()
        then:
        response.json != null
        response.json.size() == 3
        response.json.aaData[0].size() == 4
        response.json.aaData[0].title == "title1"
    }

    void "test listInteractiveHelp"() {
        given:
        InteractiveHelp.metaClass.static.fetchByString = { String search ->
            new Object() {
                List list(Object o) {
                    [new InteractiveHelp(id: 1, title: "title1"),
                     new InteractiveHelp(id: 2, title: "title2")]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:

        controller.listInteractiveHelp()
        then:
        response.json != null
        response.json.size() == 3
        response.json.aaData[0].size() == 5
        response.json.aaData[1].title == "title2"
    }

    void "test fetchInteractiveHelp"() {
        given:
        InteractiveHelp.metaClass.static.fetchByPage = { String search ->
            new Object() {
                List list(Map args) {
                    [new InteractiveHelp(id: 1, page: "test", title: "1", description: "desc")]
                }
            }
        }

        controller.params.page = "test"

        when:
        controller.fetchInteractiveHelp()

        then:
        response.json != null
        response.json.size() == 1
        response.json[0].title == "1"
        response.json[0].page == "test"
    }

    void "test releaseNoteValue"() {
        given:
        ReleaseNotes l = new ReleaseNotes(id: 1, releaseNumber: "releaseNumber").save(failOnError: true, validate: false, flush: true)

        when:
        params.id = l.id
        controller.releaseNoteValue()
        then:
        response.json != null
        response.json.size() == 2
        response.json.releaseNumber == "releaseNumber"
    }

    void "test localizationValue"() {
        given:
        Localization l = new Localization(id: 1, code: "code1", locale: "locale1", text: "text").save(failOnError: true, validate: false, flush: true)

        when:
        params.id = l.id
        controller.localizationValue()
        then:
        response.json != null
        response.json.size() == 2
        response.json.text == "text (code1_locale1)"
    }

    void "test showMessage"() {
        given:
        Localization l = new Localization(id: 1, code: "code1", locale: "locale1", text: "text").save(failOnError: true, validate: false, flush: true)
        LocalizationHelpMessage h = new LocalizationHelpMessage(message: "message", localization: l).save(failOnError: true, validate: false, flush: true)
        l.helpMessage = h
        l.save(failOnError: true, validate: false, flush: true)

        when:
        params.id = l.id
        controller.showMessage()
        then:
        response != null
        response.text == "message"
    }

    void "test showReleaseItemHelp"() {
        given:
        ReleaseNotesItem l = new ReleaseNotesItem(id: 1, description: "description").save(failOnError: true, validate: false, flush: true)

        when:
        params.id = l.id
        controller.showReleaseItemHelp()
        then:
        response != null
        response.text == "description"
    }

    void "test localizationList"() {
        given:
        Localization.metaClass.static.fetchByString = { String search, Boolean b ->
            new Object() {
                List list(Object o) {
                    [new Localization(id: 1, code: "code1", locale: "locale1", text: "text"),
                     new Localization(id: 2, code: "code2", locale: "locale2", text: "text")]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:

        controller.localizationList()
        then:
        response.json != null
        response.json.size() == 2
        response.json.items[0].size() == 2
        response.json.items[0].text == "text (code1_locale1)"
    }

    void "test releaseNotesList"() {
        given:
        ReleaseNotes.metaClass.static.fetchByString = { String search ->
            new Object() {
                List list(Object o) {
                    [new ReleaseNotes(id: 1, releaseNumber: "releaseNumber1"),
                     new ReleaseNotes(id: 2, releaseNumber: "releaseNumber2")]
                }

                int count(Object o) {
                    return 2
                }
            }
        }
        when:

        controller.releaseNotesList()
        then:
        response.json != null
        response.json.size() == 2
        response.json.items[0].size() == 2
        response.json.items[0].text == "releaseNumber1"
    }

    void "test create"() {
        when:
        controller.create()
        then:
        response.status == 200
    }

    void "test createReleaseNotes"() {
        when:
        controller.createReleaseNotes()
        then:
        response.status == 200
    }

    void "test createReleaseNotesItem"() {
        when:
        controller.createReleaseNotesItem()
        then:
        response.status == 200
    }

    void "test createInteractiveHelp"() {
        when:
        controller.createInteractiveHelp()
        then:
        response.status == 200
    }

    void "test createSystemNotification"() {
        when:
        controller.createSystemNotification()
        then:
        response.status == 200
    }

    void "test save"() {
        given:
        Localization l = new Localization(id: 1, code: "code1", locale: "locale1", text: "text").save(failOnError: true, validate: false, flush: true)
        Localization.metaClass.static.resetThis = { String s -> }
        when:
        params.message = "message"
        params.localizationId = l.id
        controller.save()
        then:
        l.helpMessage.message == "message"
        response.status == 200
    }

    void "test saveSystemNotification"() {
        given:
        params.title = "1"
        params.description = "2"
        params.details = "3"
        def result
        controller.CRUDService = [save: { Object theInstance ->
            result = theInstance
        }]
        when:

        controller.saveSystemNotification()
        then:

        result.title == "1"
        result.description == "2"
        result.details == "3"
        response.status == 302
    }

    void "test saveInteractiveHelp"() {
        given:
        params.title = "1"
        params.description = "2"
        def result
        controller.CRUDService = [save: { Object theInstance ->
            result = theInstance
        }]
        when:

        controller.saveInteractiveHelp()
        then:

        result.title == "1"
        result.description == "2"
        response.status == 302
    }


    void "test saveReleaseNotes"() {
        given:
        params.releaseNumber = "1"
        params.title = "2"
        params.description = "3"
        def result
        controller.CRUDService = [save: { Object theInstance ->
            result = theInstance
        }]
        when:
        controller.saveReleaseNotes()
        then:
        result.releaseNumber == "1"
        result.title == "2"
        result.description == "3"
        response.status == 302
    }

    void "test saveReleaseNotesItem"() {
        given:
        ReleaseNotes l = new ReleaseNotes(id: 1, notes: []).save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [save: { Object theInstance ->
            result = theInstance
        }]
        when:
        params.releaseNotesId = l.id
        params.title = "2"
        params.description = "3"
        controller.saveReleaseNotesItem()
        then:
        result.title == "2"
        result.description == "3"
        response.status == 302
    }

    void "test edit"() {
        given:
        Localization l = new Localization(id: 1, code: "code1", locale: "locale1", text: "text").save(failOnError: true, validate: false, flush: true)
        when:
        params.id = l.id
        controller.edit()
        then:
        response.status == 200
    }

    void "test editReleaseNotes"() {
        given:
        ReleaseNotes l = new ReleaseNotes(id: 1).save(failOnError: true, validate: false, flush: true)
        when:
        params.id = l.id
        controller.editReleaseNotes()
        then:
        response.status == 200
    }

    void "test viewReleaseNotes"() {
        given:
        ReleaseNotes l = new ReleaseNotes(id: 1).save(failOnError: true, validate: false, flush: true)
        when:
        params.id = l.id
        controller.viewReleaseNotes()
        then:
        response.status == 200
    }

    void "test editReleaseNotesItem"() {
        given:
        ReleaseNotesItem l = new ReleaseNotesItem(id: 1).save(failOnError: true, validate: false, flush: true)
        when:
        params.id = l.id
        controller.editReleaseNotesItem()
        then:
        response.status == 200
    }

    void "test editSystemNotification"() {
        given:
        SystemNotification l = new SystemNotification(id: 1).save(failOnError: true, validate: false, flush: true)
        when:
        params.id = l.id
        controller.editSystemNotification()
        then:
        response.status == 200
    }

    void "test editInteractiveHelp"() {
        given:
        InteractiveHelp l = new InteractiveHelp(id: 1).save(failOnError: true, validate: false, flush: true)
        when:
        params.id = l.id
        controller.editInteractiveHelp()
        then:
        response.status == 200
    }

    void "test export"() {
        given:
        Localization.metaClass.static.fetchByString = { String search, Boolean b ->
            new Object() {
                List list() {
                    [new Localization(id: 1, code: "code1", locale: "locale1", text: "text", helpMessage: new LocalizationHelpMessage(message: "message1")),
                     new Localization(id: 2, code: "code2", locale: "locale2", text: "text", helpMessage: new LocalizationHelpMessage(message: "message2"))]
                }
            }
        }
        when:

        controller.export()
        then:
        JSON.parse(response.text).size() == 2
    }

    void "test exportSystemNotification"() {
        given:
        SystemNotification.metaClass.static.fetchByString = { String search ->
            new Object() {
                List list() {
                    [new SystemNotification(id: 1, title: "title1", description: "description1", details: "details1"),
                     new SystemNotification(id: 2, title: "title2", description: "description2", details: "details2")]
                }
            }
        }
        when:

        controller.exportSystemNotification()
        then:
        JSON.parse(response.text).size() == 2
    }

    void "test exportInteractiveHelp"() {
        given:
        InteractiveHelp.metaClass.static.fetchByString = { String search ->
            new Object() {
                List list() {
                    [new InteractiveHelp(id: 1, title: "title1", description: "description1", details: "details1"),
                     new InteractiveHelp(id: 2, title: "title2", description: "description2", details: "details2")]
                }
            }
        }
        when:

        controller.exportInteractiveHelp()
        then:
        JSON.parse(response.text).size() == 2
    }

    void "test exportReleaseNotes"() {
        given:
        ReleaseNotes.metaClass.static.fetchByString = { String search ->
            new Object() {
                List list() {
                    [new ReleaseNotes(id: 1, title: "title1", description: "description1", releaseNumber: "releaseNumber",
                            notes: [
                                    new ReleaseNotesItem(title: "title1", description: "description1", summary: "summary", shortDescription: "shortDescription", invisible: false, sortNumber: 1),
                                    new ReleaseNotesItem(title: "title1", description: "description1", summary: "summary", shortDescription: "shortDescription", invisible: true, sortNumber: 2, isDeleted: true)

                            ]),
                     new ReleaseNotes(id: 1, title: "title1", description: "description1", releaseNumber: "releaseNumber",
                             notes: [
                                     new ReleaseNotesItem(title: "title1", description: "description1", summary: "summary", shortDescription: "shortDescription", invisible: false, sortNumber: 1),
                                     new ReleaseNotesItem(title: "title1", description: "description1", summary: "summary", shortDescription: "shortDescription", invisible: true, sortNumber: 2)

                             ])
                    ]

                }
            }
        }
        when:

        controller.exportReleaseNotes()
        then:
        JSON.parse(response.text).size() == 2
        JSON.parse(response.text)[0].size() == 4
        JSON.parse(response.text)[0].notes.size() == 1
        JSON.parse(response.text)[0].notes[0].size() == 6
    }

    void "test update"() {
        given:

        Localization l = new Localization(id: 1, code: "code1", locale: "locale1", text: "text").save(failOnError: true, validate: false, flush: true)
        LocalizationHelpMessage helpMessage = new LocalizationHelpMessage(message: "message", localization: l).save(failOnError: true, validate: false, flush: true)
        Localization.metaClass.static.resetThis = { String s -> }
        when:
        params.message = "message2"
        params.localizationId = l.id
        params.id = helpMessage.id
        controller.update()
        then:
        l.helpMessage.message == "message2"
        response.status == 200
    }

    void "test updateReleaseNotes"() {
        given:

        ReleaseNotes l = new ReleaseNotes(id: 1, title: "title", description: "description").save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [save: { Object theInstance ->
            result = theInstance
        }]
        when:
        params.id = l.id
        params.title = "title1"
        params.description = "description1"
        controller.updateReleaseNotes()
        then:
        result.title == "title1"
        response.status == 302
    }

    void "test updateSystemNotification"() {
        given:

        SystemNotification l = new SystemNotification(id: 1, title: "title", description: "description").save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [save: { Object theInstance ->
            result = theInstance
        }]
        when:
        params.id = l.id
        params.title = "title1"
        params.description = "description1"
        controller.updateSystemNotification()
        then:
        result.title == "title1"
        response.status == 302
    }

    void "test updateInteractiveHelp"() {
        given:

        InteractiveHelp l = new InteractiveHelp(id: 1, title: "title", description: "description").save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [save: { Object theInstance ->
            result = theInstance
        }]
        when:
        params.id = l.id
        params.title = "title1"
        params.description = "description1"
        controller.updateInteractiveHelp()
        then:
        result.title == "title1"
        response.status == 302
    }

    void "test updateReleaseNotesItem"() {
        given:

        ReleaseNotesItem l = new ReleaseNotesItem(id: 1, title: "title", description: "description", releaseNotes: new ReleaseNotes(id: 2)).save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [save: { Object theInstance ->
            result = theInstance
        }]
        when:
        params.id = l.id
        params.title = "title1"
        params.description = "description1"
        controller.updateReleaseNotesItem()
        then:
        result.title == "title1"
        response.status == 302
    }

    void "test delete"() {
        given:

        Localization l = new Localization(id: 1, code: "code1", locale: "locale1", text: "text").save(failOnError: true, validate: false, flush: true)
        LocalizationHelpMessage helpMessage = new LocalizationHelpMessage(message: "message", localization: l).save(failOnError: true, validate: false, flush: true)
        Localization.metaClass.static.resetThis = { String s -> }
        when:
        params.message = "message2"
        params.localizationId = l.id
        params.id = helpMessage.id
        controller.delete()
        then:
        l.helpMessage == null
        response.redirectUrl == '/localizationHelpMessage/index'
    }

    void "test deleteReleaseNotes"() {
        given:
        ReleaseNotes l = new ReleaseNotes(id: 1, title: "title1").save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [softDelete: { Object theInstance, String p ->
            result = theInstance
        }]
        when:
        params.id = l.id
        params.title = "title1"
        controller.deleteReleaseNotes()
        then:
        response.status == 302
    }

    void "test deleteSystemNotification"() {
        given:
        SystemNotification l = new SystemNotification(id: 1, title: "title1").save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [softDelete: { Object theInstance, String p ->
            result = theInstance
        }]
        when:
        params.id = l.id
        params.title = "title1"
        controller.deleteSystemNotification()
        then:
        response.status == 302
    }

    void "test deleteInteractiveHelp"() {
        given:
        InteractiveHelp l = new InteractiveHelp(id: 1, title: "title1").save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [softDelete: { Object theInstance, String p ->
            result = theInstance
        }]
        when:
        params.id = l.id
        params.title = "title1"
        controller.deleteInteractiveHelp()
        then:
        response.status == 302
    }

    void "test deleteReleaseNotesItem"() {
        given:
        ReleaseNotesItem l = new ReleaseNotesItem(id: 1, title: "title1", releaseNotes: new ReleaseNotes(id: 1)).save(failOnError: true, validate: false, flush: true)
        def result
        controller.CRUDService = [softDelete: { Object theInstance, String p ->
            result = theInstance
        }]
        when:
        params.id = l.id
        params.title = "title1"
        controller.deleteReleaseNotesItem()
        then:
        response.status == 302
    }


    void "test updateFileReferences"() {
        given:
        OneDriveRestService oneDriveRestService = new OneDriveRestService()
        oneDriveRestService.metaClass.listFiles = { String siteId, String folder -> return [status: 200, items: [[name: "filename1", id: "1", url: "newUrl"]]] }
        controller.oneDriveRestService = oneDriveRestService
        Localization l = new Localization(id: 1, code: "code1", locale: "*", text: "text").save(failOnError: true, flush: true)
        LocalizationHelpMessage helpMessage = new LocalizationHelpMessage(message: "<div><iframe src='http://oldUrl' title='filename1'></iframe></div>", localization: l).save(failOnError: true, validate: false, flush: true)

        when:
        params.oneDriveSiteId = "1"
        params.oneDriveFolderId = "2"

        controller.updateFileReferences()
        then:
        l.helpMessage.message.contains("newUrl")
        response.redirectUrl == '/localizationHelpMessage/index'
    }

    void "test updateFileReferencesForReleaseNotes"() {
        given:
        OneDriveRestService oneDriveRestService = new OneDriveRestService()
        oneDriveRestService.metaClass.listFiles = { String siteId, String folder -> return [status: 200, items: [[name: "filename1", id: "1", url: "newUrl"]]] }
        controller.oneDriveRestService = oneDriveRestService
        ReleaseNotes l = new ReleaseNotes(id: 1, title: "title1", releaseNumber: "123", hasDescription: true, modifiedBy: "_", createdBy: "_", description: "<div><iframe src='http://oldUrl' title='filename1'></iframe></div>").save(failOnError: true, flush: true)
        ReleaseNotesItem l2 = new ReleaseNotesItem(id: 1, title: "title1", releaseNotes: l, hasDescription: true, modifiedBy: "_", createdBy: "_", description: "<div><iframe src='http://oldUrl' title='filename1'></iframe></div>").save(failOnError: true, flush: true)


        when:
        params.oneDriveSiteId = "1"
        params.oneDriveFolderId = "2"

        controller.updateFileReferencesForReleaseNotes()
        then:
        l.description.contains("newUrl")
        l2.description.contains("newUrl")
        response.redirectUrl == '/localizationHelpMessage/releaseNotes'
    }


    void "test dontShow"() {
        given:
        controller.userService = [currentUser: makeNormalUser()]
        ReleaseNotesNotifier.metaClass.static.findByUser = { User u ->
            return null
        }
        when:
        controller.dontShow()
        then:
        response.status == 200
        response.text == "ok"
    }

    void "test remindLater"() {

        when:
        controller.remindLater()
        then:
        response.status == 200
        response.text == "ok"
    }

    void "test sendNotifications"() {
        given:
        ReleaseNotesNotifier.metaClass.static.executeUpdate = { String s -> return null }
        when:
        controller.sendNotifications()
        then:
        response.status == 302
        response.redirectUrl == '/localizationHelpMessage/releaseNotes'
    }

    void "test readReleaseNotes"() {
        given:
        controller.userService = [currentUser: makeNormalUser()]
        ReleaseNotes.metaClass.static.get = { Object u -> null }
        ReleaseNotes.metaClass.static.getAllSorted = { ->
            return [new ReleaseNotes()]
        }


        when:
        controller.readReleaseNotes()
        then:
        response.status == 200
    }

    void "test toggleVisability"() {
        given:

        ReleaseNotesItem.metaClass.static.get = { Object u -> new ReleaseNotesItem(invisible: true, releaseNotes: new ReleaseNotes(id: 1)) }

        def result
        controller.CRUDService = [update: { Object theInstance ->
            result = theInstance
        }]

        when:
        controller.toggleVisability()
        then:
        result.invisible == false
    }


}
