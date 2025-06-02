package com.reports

import com.rxlogix.Constants
import com.rxlogix.SqlGenerationService
import com.rxlogix.config.*
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.json.JsonOutput
import com.rxlogix.mapping.AllowedAttachment
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.mapping.SafetyCalendar
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.json.JsonSlurper
import org.grails.datastore.mapping.multitenancy.web.SessionTenantResolver
import com.rxlogix.enums.IcsrRuleEvaluationEnum

import java.text.SimpleDateFormat

class PvReportsTagLib {
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]


    def queryService
    def dynamicReportService
    def configurationService
    def templateService
    def userService
    def applicationSettingsService
    def detailedCaseSeriesService
    SqlGenerationService sqlGenerationService

    def renderAnnotateIcon = { attrs ->
        Set<Comment> comments = attrs."comments"

        if (comments?.size()) {
            attrs."data-content" = comments.sort { it.dateCreated }.last().textData
            attrs."class" = "fa fa-commenting-o commentPopoverMessage " + (attrs."class"?:"")
        }else{
            attrs."class" = "fa fa-comment-o "+(attrs."class"?:"")
        }
        out << """<span class="annotationPopover"><i class='${ attrs."class"}'  
                    data-content='${applyCodec( encodeAs:'HTML',attrs."data-content")}' 
                    ${attrs.style?"style='"+attrs.style+"'":""}
                    data-placement=${attrs."data-placement"?:"right"}
                    ${attrs.title?"title='"+applyCodec( encodeAs:'HTML',attrs.title)+"'":""}
                     ></i></span>
                """
    }


    def showIfLoggedInUserSame = { attrs, body ->
        String userName = attrs.remove('userName')
        if (sec.username().decodeHTML() == userName || userService.currentUser.username.decodeHTML() == userName) {
            out << body()
        }
    }

    def renderDynamicReportName = { attrs ->
        ExecutedReportConfiguration executedReportConfiguration = attrs.remove('executedConfiguration')
        ExecutedTemplateQuery executedTemplateQuery = attrs.remove('executedTemplateQuery')
        String icon = "";
        String name = ""
        if (executedReportConfiguration.instanceOf(ExecutedPeriodicReportConfiguration.class) && !attrs.hideSubmittable)
            icon = (executedTemplateQuery.draftOnly ? "<span class='submittable_icon' title='" + message(code: "app.label.draftOnly") + "'> [NS] </span> " :
                    " <span class='submittable_icon' title='" + message(code: "app.label.submittable") + "'> [S] </span> ")
        String rowId = attrs?.rowId
        String columnName = attrs.columnName
        //This if condition work when we execute the dynamic drilldown report
        if(rowId && columnName) {
            List<ExecutedTemplateQuery> sections = []
            executedReportConfiguration?.fetchExecutedTemplateQueriesByCompletedStatus()?.each {
                if(it.manuallyAdded){
                    Map dataMap = MiscUtil.parseJsonText(it?.onDemandSectionParams)
                    if(dataMap != null && dataMap.rowId == Integer.parseInt(rowId.toString()) && dataMap.columnName.equals(columnName)) {
                        sections.add(it)
                    }
                }
            }
            if (sections?.size() > 0) {
                if (executedTemplateQuery.title == executedReportConfiguration.reportName) {
                    name = (true ? executedReportConfiguration.reportName + ": " : "") + executedTemplateQuery.executedTemplate.name
                } else {
                    name = executedTemplateQuery.title
                }
            }
            name = MiscUtil.matchCSVPattern(name)
        }else {
            name = dynamicReportService.getReportNameAsTitle(executedReportConfiguration, executedTemplateQuery, true)
        }
        if (attrs.max && (name.size() > (attrs.max as Integer))) {
            out << ("<span title='${applyCodec(encodeAs: 'HTML', name)}'>" + applyCodec(encodeAs: 'HTML', name.substring(0, (attrs.max as Integer))) + "...</span>" + icon)
        } else {
            out << (applyCodec(encodeAs: 'HTML', name) + icon)
        }
    }

    def renderDateRangeInformation = { attrs->
        BaseDateRangeInformation executedDateRangeInformation = attrs.remove('executedDateRangeInformation')
        out << configurationService.getDateRangeValue(executedDateRangeInformation, userService.user?.preference?.locale)
    }

    def renderShortFormattedDate = { attrs ->
        if (!attrs.date) {
            out << ""
            return
        }
        attrs.formatName = "default.date.format.short"
        out << formatDate(attrs)
    }

    def renderLongFormattedDate = { attrs ->
        if (!attrs.date) {
            out << ""
            return
        }
        attrs.formatName = attrs.showTimeZone ? "default.date.format.long.tz" : "default.date.format.long"
        out << formatDate(attrs)
    }

    def getCurrentUserTimezone = { attrs ->
        // Setting ZoneId as Grails 6.x format date uses the same.
        out << getTimezoneObj(userService.currentUser?.preference?.timeZone)?.toZoneId()?.id
    }

    def selectActionCategory = {attrs ->
        attrs.from = ActionItemCategory.list().sort { it.name }
        attrs.optionKey = "key"
        attrs.optionValue= "name"
        out << select(attrs)
    }

    def dateRangeValueForCriteria = {attrs ->
        if (!attrs.executedTemplateQuery) {
            out << ""
            return
        }
        out << configurationService.getDateRangeValueForCriteria(attrs.executedTemplateQuery, userService.currentUser?.preference?.locale)
    }

    def footerSelect = { attrs ->
        attrs['data-select'] = JsonOutput.toJson(ReportFooter.findAllByIsDeleted(false).collect { it.footer })
        attrs.maxlength = grailsApplication.config.getProperty('report.footer.max.length', Integer)
        attrs.placeholder = message(code: "placeholder.templateQuery.footer")
        out << textArea(attrs)
    }

    def templateFooterSelect = { attrs ->
        attrs['data-select'] = JsonOutput.toJson(ReportFooter.findAllByIsDeleted(false).collect { it.footer })
        attrs.maxlength = grailsApplication.config.getProperty('template.footer.max.length', Integer)
        attrs.placeholder = message(code: "placeholder.templateQuery.footer")
        out << textArea(attrs)
    }

    def generateSpotFireFileName = { attrs ->
        String str = attrs.fileName
        if (str) {
            int endIndex = str.lastIndexOf("/");
            if (endIndex != -1) {
                str = str.substring((endIndex+1), str.length())
            }
        }
        out << str
    }

    def renderInstanceName = { attrs->
        if(grailsApplication.config.pvr.instance.ipaddress.display) {
        String instanceName  = grailsApplication.config.getProperty('pvr.cluster.instance.name', String, '')
        def body = """<li><span style="margin-left: 20px;">${instanceName}</span></li>
                                        <li class="divider"></li>"""

            out << sec.ifAnyGranted(attrs, body)
        }
    }

    def showIfDmsServiceActive = { attrs, body ->
        if (applicationSettingsService.hasDmsIntegration() && SpringSecurityUtils.ifAnyGranted("ROLE_DMS")) {
            out << body()
        }
    }

    def templateListAsJSONStringHidden = { attrs ->
        if (attrs.list) {
            attrs.value = templateService.getJSONStringRF(attrs.list, attrs.get('selectedLocale'))
        } else {
            attrs.value = null
        }
        out << hiddenField(attrs)
    }

    def chartDefaultOptionsHidden = { attrs ->
        attrs.value = templateService.getChartDefaultOptions()
        out << hiddenField(attrs)
    }

    def templateAsJSON = { attrs ->
        out << templateService.getTemplateAsJSON(attrs.get('reportTemplate'))
    }

    def queryAsJSON = { attrs ->
        out << queryService.getQueryAsJSON(attrs.get('query'))
    }

    def renderUserLastLoginDate = { attrs ->
        def user = userService.currentUser
        if (!user || !user.lastToLastLogin) {
            out << message(code: 'user.neverLoggedIn.before.label')
            return
        }
        attrs.date = user.lastToLastLogin
        attrs.timeZone = getTimezoneObj(user.preference?.timeZone)
        attrs.formatName = 'user.lastLogin.date.format'
        out << (formatDate(attrs) + " (${message(code: 'app.timezone.TZ.GMT')} ${TimeZoneEnum.values().find { it.timezoneId == user.preference?.timeZone }?.gmtOffset})")
    }

    def renderLastLoginDateForUser = { attrs ->
        User user = attrs.user
        attrs.date = user?.lastLogin
        if (!attrs.date) {
            out << message(code: 'user.neverLoggedIn.label')
            return
        }
        attrs.timeZone = getTimezoneObj(user.preference?.timeZone)
        out << renderLongFormattedDate(attrs)
    }


    def actionItemUpdate = { attrs ->
        def message= attrs.message
        out << getHtmlTemplate(message)
    }

    void getHtmlTemplate(message) {
        def content = '<div class="alert alert-success alert-dismissible forceLineWrap" role="alert">' +
                '<button type="button" class="close" data-dismiss="alert">' +
                '<span aria-hidden="true">' +
                'x' +
                '<span class="sr-only">' + '<g:message code="default.button.close.label"/></span>' + '</button>' +
                '<i class="fa fa-check">' + '</i>' +
                '<g:applyCodec encodeAs="HTML">' +
                message.decodeHTML() + '</g:applyCodec><br/>' + '</div>'
    }

    def selectCurrentTenant = {attrs ->
        attrs.value = session[SessionTenantResolver.ATTRIBUTE] as Long
        out << selectTenant(attrs)
    }

    def selectTenant = {attrs ->
        attrs.from = userService.currentUser.tenants.findAll{it.active}
        out << select(attrs)
    }

    def withOutTenant = { attrs, body ->
        Tenants.withoutId {
            out << body()
        }
    }

    def renderTemplateName = { attrs ->
        ExecutedTemplateQuery executedTemplateQuery = attrs.remove("executedTemplateQuery") as ExecutedTemplateQuery
        String templateName = executedTemplateQuery.executedTemplate.name
        if (executedTemplateQuery.executedTemplate?.ciomsI) {
            if (executedTemplateQuery.blindProtected && executedTemplateQuery.privacyProtected) {
                templateName = templateName + " (" + message(code: "templateQuery.blindedPrivacy.label") + ")"
            } else if (executedTemplateQuery.blindProtected) {
                templateName = templateName + " (" + message(code: "templateQuery.blinded.label") + ")"
            } else if (executedTemplateQuery.privacyProtected) {
                templateName = templateName + " (" + message(code: "templateQuery.privacyProtected.label") + ")"
            }
        }
        out << (applyCodec(encodeAs: 'HTML', templateName))
    }

    def renderCriteriaDate = {attrs ->
        ExecutedTemplateQuery templateQuery = attrs.templateQuery
        String value = attrs.value?:''
        String body = ""
        Locale locale = userService.getCurrentUser()?.preference?.locale ?: templateQuery?.executedConfiguration?.owner?.preference?.locale
        if (value.trim() == Constants.REPORTING_PERIOD_START_DATE) {
            body = configurationService.getCriteriaDate(templateQuery, locale).get(templateQuery.id + Constants.REPORTING_PERIOD_START_DATE)
        } else if (value.trim() == Constants.REPORTING_PERIOD_END_DATE) {
            body = configurationService.getCriteriaDate(templateQuery, locale).get(templateQuery.id + Constants.REPORTING_PERIOD_END_DATE)
        } else {
            body = value
        }
        out << (applyCodec(encodeAs: 'HTML', body))
    }

    def selectXsltsName = {attrs ->
        attrs.from = grailsApplication.config.getProperty('pv.app.e2b.xslts.options', Map)
        attrs.optionKey = "key"
        attrs.optionValue= "key"
        out << select(attrs)
    }

    def createMenuLink = { attrs ->
        if (attrs.link && attrs.link.toString().startsWith('/')) {
            out << createLink(uri: attrs.link)
        } else {
            out << attrs.link
        }
    }

    def selectController = { attrs, body ->
        def controller
        def fromTemplate = attrs.fromTemplate
        def isHide = attrs.templateQueryInstance

        if(attrs.isForPeriodicReport)
            controller = "periodicReport"
        else if (attrs.isForIcsrReport)
            controller = "icsrReport"
        else if (attrs.isForIcsrProfile)
            controller = "icsrProfileConfiguration"
        else if(attrs.isForAutoReasonOfDelay)
            controller = "autoReasonOfDelay"
        else
            controller = "configuration"

        out << """
                <a data-url='${createLink(controller: "${controller}" , action: 'createQuery')}'
                                data-message='${controller == 'autoReasonOfDelay' ? message(code: "app.query.create.warning") : message(code: "app.template.query.create.warning")}'
                               title="${message(code: 'default.CreateQuery.title')}"
                               class="pv-ic newQuery createTemplateQueryButton createQueryRCAButton glyphicon glyphicon-plus-sign ${fromTemplate} ${isHide}"></a>                                 
        """

    }

    def selectE2BOutgoingFolder = { attrs ->
        Set<String> options = grailsApplication.config.getProperty('pv.app.e2b.outgoing.folders.path', Set)
        if (attrs.value) {
            options.add(attrs.value)
        }
        attrs.from = options
        out << select(attrs)
    }

    def selectE2BIncomingFolder = { attrs ->
        Set<String> options = grailsApplication.config.getProperty('pv.app.e2b.incoming.folders.path', Set)
        if (attrs.value) {
            options.add(attrs.value)
        }
        attrs.from = options
        out << select(attrs)
    }

    def scheduleDateJSON = {attrs, body ->
        if(attrs.value){
            def jsonSlurper = new JsonSlurper()
            def object = jsonSlurper.parseText(attrs.value as String)
            if(object.startDateTime.indexOf("NaN") != -1){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
                Date now = new Date()
                object.startDateTime = formatter.format(now)
            }
            def jsonValue = new JsonOutput().toJson(object)
            attrs.value = jsonValue
            out << hiddenField(attrs)
        }
        else{
            out << hiddenField(attrs)
        }
    }

    /**
     * Renders closable inline alert. Hidden by default - please use jquery show/hide to toggle the state
     *
     * @attributes id REQUIRED Used to find alert in DOM
     * @attributes type REQUIRED Type of alert - bootstrap alert types [primary,secondary,success,danger,warning,info,light,dark]
     * @attributes message REQUIRED Message text
     * @attributes icon OPTIONAL Alert icon - bootstrap icons [check,info,question,warning,plus... - and more bootstrap fa-_ icons]
     * @attributes forceLineWrap OPTIONAL Used for long length words wrapping; false by default
     */
    def renderClosableInlineAlert = { attributes ->
        String forceLineWrapClass = attributes.forceLineWrap ? "forceLineWrap" : ""
        String iconTag = attributes.icon ? '<i class="fa fa-' + attributes.icon + '">&nbsp;</i>' : ''

        out << """
            <div id="${attributes.id}" class="alert alert-${attributes.type} closable-inner-alert ${forceLineWrapClass}" style="display: none" >
                <button type="button" class="close inline-alert-close" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                    <span class="sr-only">${message(code: 'default.button.close.label')}</span>
                </button>
                ${iconTag}<span class="alert-text">${attributes.message}</span>
            </div>                                
        """
    }

    def renderPrimaryCaseSeriesColumns = {attrs, body ->
        out << (detailedCaseSeriesService.primaryVisibleFields as JSON).toString().replace("'","\\'")
    }

    def renderSecondaryCaseSeriesColumns = { attrs, body ->
        Map m = detailedCaseSeriesService.secondaryFields
        m.each {
            it.value.put('label', message(code: 'app.caseList.' + it.key, default: it.key).toString())
        }
        m = m.sort {
            if(!it.value.order) {
                it.value.order = 999
            }
            it.value.order
        }
        out << (m as JSON).toString()
    }

    def renderPrimaryCaseSeriesFields = { attrs, body ->
        String data = ''
        detailedCaseSeriesService.primaryVisibleFields.each {
            data = data + ("<th data-id='${it.key}'>${message(code:'app.caseList.'+it.key,default: it.key)}</th>\n")
        }
        out << data
    }

    /**
     * Renders inline spinner. Hidden by default - please use jquery show/hide to toggle the state
     *
     * @attributes id REQUIRED Used to find element in DOM
     * @attributes message OPTIONAL Message text - text displayed near the spinner icon (e.g. 'Please, wait...')
     * @attributes forceLineWrap OPTIONAL Used for message long length words wrapping; true/false - false by default
     */
    def renderInlineSpinner = { attributes ->
        String forceLineWrapClass = attributes.forceLineWrap ? "forceLineWrap" : ""
        String messageTag = attributes.message != null ? """<span class="inline-spinner-text">${attributes.message}</span>""" : ""

        out << """
            <span id="${attributes.id}" class="inline-spinner ${forceLineWrapClass}" style="display: none; text-align: center;" >
                <i class="fa fa-refresh fa-spin"></i>&nbsp;${messageTag}
            </span>
        """
    }

    def maskData = {attrs, body ->
        if(attrs.tenantId == Tenants.currentId()) {
            out << body()
        } else {
            out << "**********"
        }
    }
    
    
    def renderAllowedAttachments = {attrs,body ->
        List attachmentCharacteristics = []
        List newList = []
        if(attrs.attachmentIds){
            Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
            if(attrs.attachmentIds instanceof String){
                attrs.attachmentIds.split(",").each{
                    newList.add(Long.parseLong(it))
                }
                attrs.attachmentIds = newList
            }
            AllowedAttachment.withNewSession {
                
                attrs.attachmentIds.each{
                    Long id = it
                    AllowedAttachment attachment
                        attachment = AllowedAttachment.findByIdAndLangId(id, langId)
                    if(attachment){
                        attachmentCharacteristics.add(attachment.name)
                        
                    }else {
                        attachmentCharacteristics.add("${it}(Attachment characteristic not found)")
                    }
                }
            }
        }
        out << attachmentCharacteristics.join(",")
    }

    def renderIcsrMsgTypeName = {attrs,body ->
        String icsrMsgTypeName = null
        if(attrs.icsrMsgType){
            Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
            MessageType.withNewSession {
                MessageType messageTypeObject = MessageType.findByIdAndLangId(attrs.icsrMsgType, langId)
                if (messageTypeObject) {
                    icsrMsgTypeName = messageTypeObject.description
                }
            }
        }
        out << icsrMsgTypeName
    }

    def renderCalendarName = {attrs,body ->
        List calendarName = []
        if(attrs.calendarIds){
            SafetyCalendar.withNewSession {
                attrs.calendarIds.each {
                    SafetyCalendar calendarObject = SafetyCalendar.read(it)
                    if (calendarObject) {
                        calendarName.add(calendarObject.name)
            
                    } else {
                        calendarName.add("${it}(Calendar not found)")
                    }
        
                }
            }
        }
        
        out << calendarName.join(",")
    }

    def renderRuleEvaluation = { attrs, body ->
        String ruleName = null
        if (attrs.ruleEvaluation) {
            IcsrRuleEvaluationEnum icsrRuleEvaluationEnum = IcsrRuleEvaluationEnum.valueOf(attrs.ruleEvaluation)
            ruleName = message(code: icsrRuleEvaluationEnum.getI18nKey())
        }
        out << ruleName
    }

    def renderNameLinkInList = {attrs, body ->
        String dashboardLabel = attrs.label
        String style = attrs.tagStyle ?: "text-transform:none;text-overflow:ellipsis;overflow:hidden;white-space: nowrap"
        style += ";max-width:${attrs.maxWidth ?: 420}px"
        style += ";min-width:${attrs.minWidth ?: 210}px"
        out << g.link(controller: attrs.controller, action: attrs.action, params: attrs.params, style: style, title: attrs.label) {
            out << dashboardLabel
        }
    }

    /*
    this method is used to get auth names corresponding to auth id saved in PVR-DB
     */
    def renderIcsrAuthTypeName = {attrs,body ->
        List<String> icsrAuthTypeName = []
        if(attrs.icsrAuthType){
            Integer langId = sqlGenerationService.getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
            AuthorizationType.withNewSession {
                attrs.icsrAuthType.each {
                    AuthorizationType authorizationType = AuthorizationType.findByIdAndLangId(it, langId)
                    if (authorizationType) {
                        icsrAuthTypeName.add(authorizationType?.name)
                    }
                }
            }
        }
        out << icsrAuthTypeName.join(", ")
    }

    private TimeZone getTimezoneObj(String id){
        if(!id){
            return null
        }
        return TimeZone.getTimeZone(id)
    }

}
