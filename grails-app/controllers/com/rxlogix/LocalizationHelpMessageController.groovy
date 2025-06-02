package com.rxlogix


import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.localization.HelpLink
import com.rxlogix.localization.InteractiveHelp
import com.rxlogix.localization.Localization
import com.rxlogix.localization.LocalizationHelpMessage
import com.rxlogix.localization.ReleaseNotes
import com.rxlogix.localization.ReleaseNotesItem
import com.rxlogix.localization.ReleaseNotesNotifier
import com.rxlogix.localization.SystemNotification
import com.rxlogix.localization.SystemNotificationNotifier
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.web.multipart.MultipartFile

import java.util.regex.Pattern

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class LocalizationHelpMessageController implements SanitizePaginationAttributes {

    OneDriveRestService oneDriveRestService
    def CRUDService
    def userService

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def index() {

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def helpLink() {
        if (params.link) {
            HelpLink.setDefaultHelpLink(params.link)
            flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.localizationHelp.helpLink', default: 'Help Message'), ""])}"
        }
        render view: "helpLink"
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def releaseNotes() {
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def systemNotification() {
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def interactiveHelp() {
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def releaseNotesItem() {
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def list() {
        params.sort == "dateCreated" ? params.sort = "dateCreated" : params.sort
        List<Localization> list = Localization.fetchByString(params.searchString, true).list([max: params.length, offset: params.start, sort: params.sort, order: params.direction])
        render([aaData         : list.collect { [id: it.id, code: it.code, locale: it.locale, text: it.text] },
                recordsTotal   : Localization.fetchByString(null, true).count(),
                recordsFiltered: Localization.fetchByString(params.searchString, true).count()] as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def listReleaseNotes() {
        params.sort == "releaseNumber" ? params.sort = "releaseNumber" : params.sort
        List<ReleaseNotes> list = ReleaseNotes.fetchByString(params.searchString).list([max: params.length, offset: params.start, sort: params.sort, order: params.direction])
        render([aaData         : list.collect { [id: it.id, title: it.title, releaseNumber: it.releaseNumber] },
                recordsTotal   : ReleaseNotes.fetchByString(null).count(),
                recordsFiltered: ReleaseNotes.fetchByString(params.searchString).count()] as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def listSystemNotification() {
        List<SystemNotification> list = SystemNotification.fetchByString(params.searchString).list([max: params.length, offset: params.start, sort: params.sort, order: params.direction])
        render([aaData         : list.collect { [id: it.id, title: it.title, published: it.published, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_FORMAT)] },
                recordsTotal   : SystemNotification.fetchByString(null).count(),
                recordsFiltered: SystemNotification.fetchByString(params.searchString).count()] as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def listInteractiveHelp() {
        List<InteractiveHelp> list = InteractiveHelp.fetchByString(params.searchString).list([max: params.length, offset: params.start, sort: params.sort, order: params.direction])
        render([aaData         : list.collect { [id: it.id, page: it.page, title: it.title, published: it.published, dateCreated: it.dateCreated.format(DateUtil.DATEPICKER_FORMAT)] },
                recordsTotal   : InteractiveHelp.fetchByString(null).count(),
                recordsFiltered: InteractiveHelp.fetchByString(params.searchString).count()] as JSON)
    }

    def fetchInteractiveHelp() {
        List<InteractiveHelp> list = InteractiveHelp.fetchByPage(params.page).list([max: 1])
        render((list?.collect { [id: it.id, page: it.page, title: it.title, description: it.description] } as JSON))
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def listReleaseNotesItem() {
        params.sort == "dateCreated" ? params.sort = "dateCreated" : params.sort
        List<ReleaseNotesItem> list = ReleaseNotesItem.fetchByString(params.searchString).list([max: params.length, offset: params.start, sort: params.sort, order: params.direction])
        render([aaData         : list.collect { [id: it.id, title: it.title, releaseNumber: it.releaseNotes.releaseNumber] },
                recordsTotal   : ReleaseNotesItem.fetchByString(null).count(),
                recordsFiltered: ReleaseNotesItem.fetchByString(params.searchString).count()] as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def localizationValue() {
        Localization localization = Localization.get(params.long("id"))
        render([id: localization.id, text: (localization.text + " (" + localization.code + "_" + localization.locale + ")")] as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def releaseNoteValue() {
        ReleaseNotes releaseNotes = ReleaseNotes.get(params.long("id"))
        render([id: releaseNotes.id, releaseNumber: releaseNotes.releaseNumber] as JSON)
    }

    def showMessage() {
        LocalizationHelpMessage helpMessage
        if (params.localizationId && (params.localizationId != "undefined")) {
            Localization localization = Localization.get(params.long("localizationId"))
            helpMessage = localization.helpMessage
        } else {
            helpMessage = LocalizationHelpMessage.get(params.long("id"))
        }
        render helpMessage.message
    }

    def showReleaseItemHelp() {
        render ReleaseNotesItem.get(params.long("id")).description
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def localizationList() {
        forSelectBox(params)
        List<Localization> list = Localization.fetchByString(params.term, false).list([max: params.max, offset: params.offset, sort: "text", order: "asc"])
        def result = [items      : list.collect {
            [id: it.id, text: (it.text + " (" + it.code + "_" + it.locale + ")")]
        },
                      total_count: Localization.fetchByString(params.term, false).count()]
        render(result as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def releaseNotesList() {
        forSelectBox(params)
        List<ReleaseNotes> list = ReleaseNotes.fetchByString(params.term).list([max: params.max, offset: params.offset, sort: "id", order: "desc"])
        def result = [items      : list.collect {
            [id: it.id, text: it.releaseNumber]
        },
                      total_count: ReleaseNotes.fetchByString(params.term).count()]
        render(result as JSON)
    }


    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def create() {
        render view: "create", model: [localization: null, helpMessage: new LocalizationHelpMessage()]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def createReleaseNotes() {
        [instance: new ReleaseNotes()]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def createSystemNotification() {
        [instance: new SystemNotification()]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def createInteractiveHelp() {
        [instance: new InteractiveHelp()]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def createReleaseNotesItem() {
        ReleaseNotes releaseNotes = ReleaseNotes.get(params.long("id"))
        [instance: new ReleaseNotesItem(releaseNotes: releaseNotes)]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def save() {
        LocalizationHelpMessage instance = new LocalizationHelpMessage(message: params.message)
        Localization localization = Localization.get(params.long("localizationId"))
        if (!localization || !params.message) {
            render view: "create", model: [localization: localization, helpMessage: instance]
            flash.error = message(code: 'app.label.localizationHelp.pleaseSelect')
            return
        }
        if (LocalizationHelpMessage.findAllByLocalization(localization)?.size() > 0) {
            render view: "create", model: [localization: localization, helpMessage: instance]
            flash.error = message(code: 'app.label.localizationHelp.unique')
            return
        }
        try {
            instance.localization = localization
            localization.helpMessage = instance
            instance.save(flush: true, failOnError: true)
            Localization.resetThis(localization.code)
        } catch (ValidationException ve) {
            render view: "create", model: [localization: localization, helpMessage: instance]
            return
        }
        redirect(action: "index")
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.localizationHelp.appName', default: 'Help Message'), localization.text])}"
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def saveReleaseNotes() {
        ReleaseNotes instance = new ReleaseNotes()
        try {
            bindData(instance, params)
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "createReleaseNotes", model: [instance: instance]
            return
        }
        redirect(action: 'viewReleaseNotes', params: [id: instance.id])
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.localizationHelp.releaseNote'), instance.releaseNumber])}"
    }

    private void bindUserGroups(SystemNotification instance, def params) {
        if (instance.userGroups) {
            instance.userGroups.removeAll(instance.userGroups.collect())
            instance.userGroups?.clear()
        }
        if (params.userGroups && (params.userGroups instanceof String)) {
            instance.addToUserGroups(UserGroup.get(params.userGroups as Long))
        } else {
            params.userGroups?.each {
                instance.addToUserGroups(UserGroup.get(it as Long))
            }
        }
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def saveSystemNotification() {
        SystemNotification instance = new SystemNotification()
        try {
            bindData(instance, params, ["userGroups"])
            bindUserGroups(instance, params)
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "createSystemNotification", model: [instance: instance]
            return
        }
        redirect(action: 'viewSystemNotification', params: [id: instance.id])
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.systemNotification.systemNotification'), instance.title])}"
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def saveInteractiveHelp() {
        InteractiveHelp instance = new InteractiveHelp()
        try {
            bindData(instance, params)
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "createInteractiveHelp", model: [instance: instance]
            return
        }
        redirect(action: 'viewInteractiveHelp', params: [id: instance.id])
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.interactiveHelp.interactiveHelp'), instance.title])}"
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def saveReleaseNotesItem() {
        ReleaseNotes releaseNotes = ReleaseNotes.get(params.long("releaseNotesId"))
        ReleaseNotesItem instance = new ReleaseNotesItem()
        try {
            bindData(instance, params, ["releaseNotesId"])
            instance.releaseNotes = releaseNotes
            instance.hasDescription = !!instance.description
            instance.sortNumber = (releaseNotes.notes?.max { it.sortNumber }?.sortNumber ?: 0) + 1
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "createReleaseNotesItem", model: [instance: instance]
            return
        }
        redirect(action: 'viewReleaseNotes', params: [id: instance.releaseNotes.id])
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'app.label.localizationHelp.releaseNoteItem'), instance.title])}"
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def edit(Long id) {
        Localization localization = Localization.read(id)
        if (!localization) {
            notFound()
        }
        render view: "edit", model: [localization: localization, helpMessage: localization.helpMessage]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def editReleaseNotes(Long id) {
        ReleaseNotes instance = ReleaseNotes.read(id)
        if (!instance) {
            notFound()
        }
        [instance: instance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def editSystemNotification(Long id) {
        SystemNotification instance = SystemNotification.read(id)
        if (instance.published) {
            flash.warn = "${message(code: 'app.label.systemNotification.noeEditable')}"
            redirect(action: "systemNotification")
            return
        }
        if (!instance) {
            notFound()
        }
        [instance: instance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def editInteractiveHelp(Long id) {
        InteractiveHelp instance = InteractiveHelp.read(id)
        if (!instance) {
            notFound()
        }
        [instance: instance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def viewReleaseNotes(Long id) {
        ReleaseNotes instance = ReleaseNotes.read(id)
        if (!instance) {
            notFound()
        }
        [instance: instance]
    }


    def viewSystemNotification(Long id) {
        SystemNotification instance = SystemNotification.read(id)
        if (!instance) {
            notFound()
        }
        if (instance.published) new SystemNotificationNotifier(user: userService.currentUser, systemNotification: instance).save(flush: true)
        [instance: instance]
    }

    def viewInteractiveHelp(Long id) {
        InteractiveHelp instance = InteractiveHelp.read(id)
        if (!instance) {
            notFound()
        }
        [instance: instance]
    }


    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def editReleaseNotesItem(Long id) {
        ReleaseNotesItem instance = ReleaseNotesItem.read(id)
        if (!instance) {
            notFound()
        }
        [instance: instance]
    }


    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def export() {

        List<Localization> list = Localization.fetchByString(null, true).list()
        if (!list) {
            redirect(action: 'index')
            flash.error = "${message(code: 'app.label.localizationHelp.nothingToExport')}"
            return
        }
        List data = list.collect {
            [code: it.code, locale: it.locale, help: it.helpMessage.message]
        }
        response.setHeader("Content-Disposition", "attachment;filename=HelpMessages.json")
        render(contentType: "application/octet-stream", text: data as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def exportSystemNotification() {

        List<SystemNotification> list = SystemNotification.fetchByString(null).list()
        if (!list) {
            redirect(action: 'systemNotification')
            flash.error = "${message(code: 'app.label.localizationHelp.nothingToExport')}"
            return
        }
        List data = list.collect {
            [title: it.title, description: it.description, published: it.published]
        }
        response.setHeader("Content-Disposition", "attachment;filename=SystemNotification.json")
        render(contentType: "application/octet-stream", text: data as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def exportInteractiveHelp() {

        List<InteractiveHelp> list = InteractiveHelp.fetchByString(null).list()
        if (!list) {
            redirect(action: 'systemNotification')
            flash.error = "${message(code: 'app.label.localizationHelp.nothingToExport')}"
            return
        }
        List data = list.collect {
            [title: it.title, page: it.page, description: it.description, published: it.published]
        }
        response.setHeader("Content-Disposition", "attachment;filename=InteractiveHelp.json")
        render(contentType: "application/octet-stream", text: data as JSON)
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def exportReleaseNotes() {

        List<ReleaseNotes> list = ReleaseNotes.fetchByString(null).list()
        if (!list) {
            redirect(action: 'releaseNotes')
            flash.error = "${message(code: 'app.label.localizationHelp.nothingToExport')}"
            return
        }
        List data = list.collect {
            [releaseNumber: it.releaseNumber, title: it.title, description: it.description,
             notes        : it.notes?.findAll { !it.isDeleted }?.collect { n ->
                 ["title"           : n.title,
                  "description"     : n.description,
                  "summary"         : n.summary,
                  "shortDescription": n.shortDescription,
                  "invisible"       : n.invisible,
                  "sortNumber"      : n.sortNumber
                 ]
             }]
        }
        response.setHeader("Content-Disposition", "attachment;filename=ReleaseNotes.json")
        render(contentType: "application/octet-stream", text: data as JSON)
    }


    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def importJson() {
        StringBuilder sb = new StringBuilder()
        int imported = 0
        try {
            MultipartFile file = request.getFile('file')
            def json = JSON.parse(file.getInputStream().text)
            json.each {
                Localization localization = Localization.findByCodeAndLocale(it.code, it.locale)

                if (localization) {
                    if (!localization.helpMessage) {
                        localization.helpMessage = new LocalizationHelpMessage(message: it.help)
                        localization.save(flush: true, failOnError: true)
                        imported++
                    } else {
                        sb.append("Help for  ${it.code} ${it.locale} already exists.\n\n")
                    }
                } else {
                    sb.append("Label for  ${it.code} ${it.locale} was not found.\n\n")
                }
            }
            String importedMessage = "${message(code: 'app.load.import.success', args: [imported])}"
            redirect(action: 'index')
            if (sb.size() > 0) {
                flash.warn = importedMessage + "\n\n" + sb.toString()
            } else {
                flash.message = importedMessage
            }
        } catch (Exception e) {
            log.error("Error occurred importing help messages", e)
            flash.error = "${message(code: 'app.label.localizationHelp.noFiles')}"
            redirect(action: 'index')
        }

    }


    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def importSystemNotification() {
        StringBuilder sb = new StringBuilder()
        int imported = 0
        try {
            MultipartFile file = request.getFile('file')
            def json = JSON.parse(file.getInputStream().text)
            json.each {
                SystemNotification item = SystemNotification.findByTitleAndIsDeleted(it.title, false)
                if (!item) {
                    SystemNotification notification = new SystemNotification(title: it.title, description: it.description, published: it.published)
                    CRUDService.save(notification)
                    imported++
                } else {
                    sb.append("System Notification ${it.title} already exists.\n\n")
                }

            }
            String importedMessage = "${message(code: 'app.load.import.success', args: [imported])}"
            redirect(action: 'systemNotification')
            if (sb.size() > 0) {
                flash.warn = importedMessage + "\n\n" + sb.toString()
            } else {
                flash.message = importedMessage
            }
        } catch (Exception e) {
            log.error("Error occurred importing system notifications", e)
            flash.error = "${message(code: 'app.label.localizationHelp.noFiles')}"
            redirect(action: 'systemNotification')
        }

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def importInteractiveHelp() {
        StringBuilder sb = new StringBuilder()
        int imported = 0
        try {
            MultipartFile file = request.getFile('file')
            def json = JSON.parse(file.getInputStream().text)
            json.each {
                InteractiveHelp item = InteractiveHelp.findByTitleAndPageAndIsDeleted(it.title, it.page, false)
                if (!item) {
                    InteractiveHelp notification = new InteractiveHelp(title: it.title, description: it.description, page: it.page, published: it.published)
                    CRUDService.save(notification)
                    imported++
                } else {
                    sb.append("Interactive Help ${it.title} already exists.\n\n")
                }

            }
            String importedMessage = "${message(code: 'app.load.import.success', args: [imported])}"
            redirect(action: 'interactiveHelp')
            if (sb.size() > 0) {
                flash.warn = importedMessage + "\n\n" + sb.toString()
            } else {
                flash.message = importedMessage
            }
        } catch (Exception e) {
            log.error("Error occurred importing system notifications", e)
            flash.error = "${message(code: 'app.label.localizationHelp.noFiles')}"
            redirect(action: 'interactiveHelp')
        }

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def importJsonReleaseNotes() {
        StringBuilder sb = new StringBuilder()
        int imported = 0
        try {
            MultipartFile file = request.getFile('file')
            def json = JSON.parse(file.getInputStream().text)
            json.each {
                ReleaseNotes releaseNotes = ReleaseNotes.findByReleaseNumberAndIsDeleted(it.releaseNumber, false)
                if (!releaseNotes) {
                    if (it.releaseNumber ==~ /^[0-9|.]+$/) {
                        ReleaseNotes rn = new ReleaseNotes(releaseNumber: it.releaseNumber, title: it.title, description: it.description)
                        CRUDService.save(rn);
                        it.notes?.each { item ->
                            ReleaseNotesItem rni = new ReleaseNotesItem(releaseNotes: rn, title: item.title, description: item.description, hasDescription: !!item.description,
                                    summary: item.summary, shortDescription: item.shortDescription, invisible: item.invisible, sortNumber: item.sortNumber
                            )
                            CRUDService.save(rni);
                        }
                        imported++
                    } else {
                        sb.append("Wrong release number ${it.releaseNumber}. Ignored...\n\n")
                    }
                } else {
                    sb.append("Release Note for ${it.releaseNumber} already exists.\n\n")
                }
            }
            String importedMessage = "${message(code: 'app.load.import.success', args: [imported])}"
            redirect(action: 'releaseNotes')
            if (sb.size() > 0) {
                flash.warn = importedMessage + "\n\n" + sb.toString()
            } else {
                flash.message = importedMessage
            }
        } catch (Exception e) {
            log.error("Error occurred importing release notes", e)
            flash.error = "${message(code: 'app.label.localizationHelp.noFiles')}"
            redirect(action: 'releaseNotes')
        }
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def update() {
        LocalizationHelpMessage instance = LocalizationHelpMessage.get(params.long("id"))
        if (!instance) {
            notFound()
        }
        Localization localization = Localization.get(params.long("localizationId"))
        if (LocalizationHelpMessage.findAllByLocalizationAndIdNotEqual(localization, instance.id)?.size() > 0) {
            instance.message = params.message
            render view: "edit", model: [localization: localization, helpMessage: instance]
            flash.error = message(code: 'app.label.localizationHelp.unique')
            return
        }
        try {

            instance.message = params.message
            instance.localization = localization
            instance.save(flush: true, failOnError: true)
            Localization.resetThis(localization.code)
        } catch (ValidationException ve) {
            render view: "edit", model: [localization: localization, helpMessage: instance]
            return
        }
        redirect(action: 'index')
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.localizationHelp.appName', default: 'Help Message'), instance.localization.text])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def updateReleaseNotes() {
        ReleaseNotes instance = ReleaseNotes.get(params.long("id"))
        if (!instance) {
            notFound()
        }
        try {

            bindData(instance, params)
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "editReleaseNotes", model: [instance: instance]
            return
        }
        redirect(action: 'viewReleaseNotes', params: [id: instance.id])
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.localizationHelp.releaseNote'), instance.releaseNumber])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def updateSystemNotification() {
        SystemNotification instance = SystemNotification.get(params.long("id"))
        if (!instance) {
            notFound()
        }
        try {
            bindData(instance, params, ["userGroups"])
            bindUserGroups(instance, params)
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "editSystemNotification", model: [instance: instance]
            return
        }
        redirect(action: 'viewSystemNotification', params: [id: instance.id])
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.systemNotification.systemNotification'), instance.title])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def updateInteractiveHelp() {
        InteractiveHelp instance = InteractiveHelp.get(params.long("id"))
        if (!instance) {
            notFound()
        }
        try {
            bindData(instance, params)
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "editInteractiveHelp", model: [instance: instance]
            return
        }
        redirect(action: 'viewInteractiveHelp', params: [id: instance.id])
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.interactiveHelp.interactiveHelp'), instance.title])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def publishSystemNotification() {
        SystemNotification instance = SystemNotification.get(params.long("id"))
        if (!instance) {
            notFound()
        }
        try {
            instance.published = true
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "editSystemNotification", model: [instance: instance]
            return
        }
        redirect(action: 'systemNotification')
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.systemNotification.systemNotification'), instance.title])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def updateReleaseNotesItem() {
        ReleaseNotesItem instance = ReleaseNotesItem.get(params.long("id"))
        if (!instance) {
            notFound()
        }
        try {

            bindData(instance, params, ["releaseNotesId"])
            instance.hasDescription = !!instance.description
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            render view: "editReleaseNotesItem", model: [instance: instance]
            return
        }
        redirect(action: 'viewReleaseNotes', params: [id: instance.releaseNotes.id])
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'app.label.localizationHelp.releaseNoteItem'), instance.title])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def delete(Long id) {
        Localization localization = Localization.get(params.long("id"))
        LocalizationHelpMessage instance = localization.helpMessage
        if (!instance) {
            notFound()
            return
        }

        try {
            localization.helpMessage = null
            instance.delete(flush: true, failOnError: true)
            Localization.resetThis(localization.code)
        } catch (ValidationException ve) {
            redirect(action: "index")
            flash.error = message(code: "default.unable.deleted.message", args: [message(code: 'app.label.localizationHelp.appName', default: 'Help Message')])
            return
        }
        redirect(action: "index")
        flash.message = "${message(code: 'app.label.localizationHelp.delete', args: ["'" + localization.text + "'"])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def deleteReleaseNotes(Long id) {
        ReleaseNotes instance = ReleaseNotes.get(params.long("id"))
        if (!instance) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(instance, params.deleteJustification)
        } catch (ValidationException ve) {
            redirect(action: "releaseNotes")
            flash.error = message(code: "default.unable.deleted.message", args: [message(code: 'app.label.localizationHelp.releaseNote')])
            return
        }
        redirect(action: "releaseNotes")
        flash.message = "${message(code: 'default.delete.message', args: [message(code: 'app.label.localizationHelp.releaseNote.delete'), "'" + instance.releaseNumber + "'"])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def deleteSystemNotification(Long id) {
        SystemNotification instance = SystemNotification.get(params.long("id"))
        if (!instance) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(instance, params.deleteJustification)
        } catch (ValidationException ve) {
            redirect(action: "systemNotification")
            flash.error = message(code: "default.unable.deleted.message", args: [message(code: 'app.label.systemNotification.systemNotification')])
            return
        }
        redirect(action: "systemNotification")
        flash.message = "${message(code: 'default.delete.message', args: [message(code: 'app.label.systemNotification.systemNotification'), "'" + instance.title + "'"])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def deleteInteractiveHelp(Long id) {
        InteractiveHelp instance = InteractiveHelp.get(params.long("id"))
        if (!instance) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(instance, params.deleteJustification)
        } catch (ValidationException ve) {
            redirect(action: "interactiveHelp")
            flash.error = message(code: "default.unable.deleted.message", args: [message(code: 'app.label.interactiveHelp.interactiveHelp')])
            return
        }
        redirect(action: "interactiveHelp")
        flash.message = "${message(code: 'default.delete.message', args: [message(code: 'app.label.interactiveHelp.interactiveHelp'), "'" + instance.title + "'"])}"

    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def deleteReleaseNotesItem(Long id) {
        ReleaseNotesItem instance = ReleaseNotesItem.get(params.long("id"))
        if (!instance) {
            notFound()
            return
        }
        try {
            CRUDService.softDelete(instance, params.deleteJustification)
        } catch (ValidationException ve) {
            redirect(action: 'viewReleaseNotes', params: [id: instance.releaseNotes.id])
            flash.error = message(code: "default.unable.deleted.message", args: [message(code: 'app.label.localizationHelp.releaseNote')])
            return
        }
        redirect(action: 'viewReleaseNotes', params: [id: instance.releaseNotes.id])
        flash.message = "${message(code: 'default.delete.message', args: [message(code: 'app.label.localizationHelp.releaseNoteItem.delete'), "'" + instance.title + "'"])}"

    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.localizationHelp.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    def updateFileReferences() {
        int totalUpdated = 0
        if (params.oneDriveSiteId && params.oneDriveFolderId) {
            Map sharepointResponse = oneDriveRestService.listFiles(params.oneDriveSiteId, params.oneDriveFolderId)
            if (sharepointResponse.status == 200) {
                List files = sharepointResponse.items
                if (files) {
                    LocalizationHelpMessage.findAll().each {
                        def (html, updated) = updateHtml(it.message, files)
                        if (updated > 0) {
                            it.message = html
                            it.save(flush: true, failOnError: true)
                            log.info("Help Content for '${it.localization.text}' was updated for ${updated} files!\n\n")
                            totalUpdated += updated
                        }
                    }
                    flash.message = "${message(code: 'app.label.localizationHelp.filesUodateSuccessfully', args: [totalUpdated])}"
                } else {
                    flash.error = "${message(code: 'app.label.localizationHelp.noFilesSharepoint')}"
                }
            } else {
                flash.error = "${message(code: 'app.label.localizationHelp.errorSharepoint')} code: ${sharepointResponse.code} message:${sharepointResponse.text}"
            }
        } else {
            flash.error = "${message(code: 'app.label.localizationHelp.selectFolder')}"
        }
        redirect(action: 'index')
    }

    private static List updateHtml(String html, List files) {
        int updated = 0
        Document page = Jsoup.parse("<html><head><title>Test</title></head><body>${html}</body></html>");
        page.select("iframe").each {
            String fileName = it.attr("title")
            String newUrl = files.find { it.name == fileName }?.url?.replace("download", "embed")
            if (newUrl) {
                it.attr("src", newUrl)
                updated++;
            }
        }
        return [page.html(), updated]
    }

    def updateFileReferencesForReleaseNotes() {
        int totalUpdated = 0
        if (params.oneDriveSiteId && params.oneDriveFolderId) {
            Map sharepointResponse = oneDriveRestService.listFiles(params.oneDriveSiteId, params.oneDriveFolderId)
            if (sharepointResponse.status == 200) {
                List files = sharepointResponse.items
                if (files) {
                    ([] + ReleaseNotes.findAll() + ReleaseNotesItem.findAll()).each {
                        int localUpdated = 0
                        def (html, updated) = updateHtml(it.description, files)
                        localUpdated += updated
                        it.description = html
                        if (it instanceof ReleaseNotesItem) {
                            (html, updated) = updateHtml(it.summary, files)
                            localUpdated += updated
                            it.summary = html
                            (html, updated) = updateHtml(it.shortDescription, files)
                            localUpdated += updated
                            it.shortDescription = html
                        }
                        if (localUpdated > 0) {
                            it.save(flush: true, failOnError: true)
                            log.info("Help Content for '${it.title}' was updated for ${localUpdated} files!\n\n")
                            totalUpdated += localUpdated
                        }
                    }
                    flash.message = "${message(code: 'app.label.localizationHelp.filesUodateSuccessfully', args: [totalUpdated])}"
                } else {
                    flash.error = "${message(code: 'app.label.localizationHelp.noFilesSharepoint')}"
                }
            } else {
                flash.error = "${message(code: 'app.label.localizationHelp.errorSharepoint')} code: ${sharepointResponse.code} message:${sharepointResponse.text}"
            }
        } else {
            flash.error = "${message(code: 'app.label.localizationHelp.selectFolder')}"
        }
        redirect(action: 'releaseNotes')
    }

    def dontShow() {
        User currentUser = userService.currentUser
        //todo: control versions
        // String v = grails.util.Metadata.current.getApplicationVersion()
        ReleaseNotesNotifier r = ReleaseNotesNotifier.findByUser(currentUser)
        if (!r) {
            r = new ReleaseNotesNotifier(user: currentUser)
            r.save(flush: true, failOnError: true)
        }
        if (params.notifications) {
            params.notifications.split(",").each {
                new SystemNotificationNotifier(user: currentUser, systemNotification: SystemNotification.get(it as Long)).save(flush: true)
            }
        }
        render "ok"
    }

    def remindLater() {
        session.remindLater = true
        render "ok"
    }

    def sendNotifications() {
        ReleaseNotesNotifier.executeUpdate("delete from ReleaseNotesNotifier")
        flash.message = "${message(code: 'app.label.whatsNewReminder.sent')}"
        redirect(action: 'releaseNotes')
    }

    def readReleaseNotes() {
        User currentUser = userService.currentUser
        ReleaseNotesNotifier r = ReleaseNotesNotifier.findByUser(currentUser)
        if (!r) {
            r = new ReleaseNotesNotifier()
            r.user = currentUser
            r.save(flush: true, failOnError: true)
        }
        List releaseNotes = ReleaseNotes.getAllSorted()
        ReleaseNotes instance
        if (params.id) instance = ReleaseNotes.get(params.long("id"))
        if (!instance && releaseNotes?.size() > 0) instance = releaseNotes[0]
        [releaseNotes: releaseNotes, instance: instance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def toggleVisability(Long id) {
        ReleaseNotesItem instance = ReleaseNotesItem.get(params.long("id"))
        if (!instance) {
            notFound()
            return
        }
        try {
            instance.invisible = !instance.invisible
            CRUDService.update(instance)
        } catch (ValidationException ve) {
            redirect(action: 'viewReleaseNotes', params: [id: instance.releaseNotes.id])
            flash.error = message(code: "default.updated.message", args: [message(code: 'app.label.localizationHelp.releaseNote')])
            return
        }
        redirect(action: 'viewReleaseNotes', params: [id: instance.releaseNotes.id])
    }

}