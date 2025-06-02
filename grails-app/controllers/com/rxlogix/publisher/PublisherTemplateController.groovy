package com.rxlogix.publisher

import com.rxlogix.config.BasicPublisherSource
import com.rxlogix.config.ExecutedPublisherSource
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherTemplate
import com.rxlogix.config.publisher.PublisherTemplateParameter
import com.rxlogix.user.Preference
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile

@Secured(["isAuthenticated()"])
class PublisherTemplateController {

    def messageSource
    def publisherService
    def publisherSourceService
    def CRUDService
    def userService
    def oneDriveRestService

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def index() {
        if (params.message)
            flash.message= params.message
        render view: "index"
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def list() {
        params.sort == "dateCreated" ? params.sort = "dateCreated" : params.sort
        List<PublisherTemplate> PublisherTemplateList = PublisherTemplate.publisherTemplateBySearchString(params.searchString).list([max: params.length, offset: params.start, sort: params.sort, order: params.direction])
        render([aaData: PublisherTemplateList*.toMap(), recordsTotal: PublisherTemplate.publisherTemplateBySearchString(null).count(), recordsFiltered: PublisherTemplate.publisherTemplateBySearchString(params.searchString).count()] as JSON)
    }

    def getPublisherTemplateList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        render([items: PublisherTemplate.qualityCheckedWithSearch(term).list([max: max, offset: Math.max(page - 1, 0) * max]).collect {
            [id: it[0], text: it[1], description: it[2], qced: it[3]]}, total_count: PublisherTemplate.qualityCheckedWithSearch(term).count()] as JSON)
    }

    def getTemplateNameDescription(Long id) {
        PublisherTemplate publisherTemplate = PublisherTemplate.read(id)
        Map result = [
                text                : publisherTemplate?.name,
                qced                : publisherTemplate?.qualityChecked
        ]
        render(result as JSON)
    }

    def getTemplateParameters(Long id) {
        PublisherTemplate instance = PublisherTemplate.read(id)
        List parameters = instance?.parameters?.findAll { it.type != PublisherTemplateParameter.Type.CODE }?.sort { it.name } ?: []
        response.status = 200
        render parameters as JSON
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def create(PublisherTemplate instance) {
        render view: "create", model: [instance: instance]
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def save() {
        PublisherTemplate instance = new PublisherTemplate()
        try {
            MultipartFile file = request.getFile('file')
            List<String> uniqueParamTitles = new ArrayList<>()
            populateProperties(instance, params, request, uniqueParamTitles)
            if(instance.parameters && uniqueParamTitles.size() != instance.parameters.size()){
                response.status = 500
                render([error: [message(code: 'com.rxlogix.config.publisher.PublisherTemplate.duplicate.tile')]] as JSON)
                return
            }
            CRUDService.save(instance)
        } catch (ValidationException ve) {
            Preference preference = userService.currentUser?.preference
            Locale locale = preference?.locale
            List allErrorMessages = instance.errors.allErrors.collect {
                messageSource.getMessage(it, locale)
            }
            response.status = 500
            render([error: allErrorMessages] as JSON)
            return
        }
        String message = message(code: 'default.created.message', args: [message(code: 'app.label.PublisherTemplate.appName', default: 'Publisher Template'), instance.name])
        render([message: message] as JSON)
    }

    private populateProperties(PublisherTemplate instance, params, request, List<String> paramTitles) {
        bindData(instance, params, ['template', 'parameters', 'qualityChecked'])
        instance.qualityChecked = params.qualityCheck ? params.boolean('qualityCheck'):false
        if (params.lockCode) {
            instance.template = oneDriveRestService.getCheckInFile(params.lockCode, userService.currentUser)
        } else {
            request.getFiles('file').each { MultipartFile file ->
                if (file.size > 0) {
                    instance.template = file.bytes
                    instance.fileName = file.originalFilename
                }
            }
        }
        instance.parameters?.collect { it }?.each {
            it.delete()
        }
        instance.parameters?.clear()
        def hiddenArray = getHiddenValues(params)
        params."parameters.name"?.eachWithIndex { r, i ->
            if (params."parameters.name"[i] && i > 0) {
                PublisherTemplateParameter ptp = new PublisherTemplateParameter(
                        name: params."parameters.name"[i],
                        title: params."parameters.title"[i],
                        description: params."parameters.description"[i],
                        value: params."parameters.value"[i],
                        hidden: (hiddenArray && hiddenArray[i-1] == '1')? true:false,
                        type: params."parameters.type"[i] as PublisherTemplateParameter.Type
                )

                if(ptp.title && !paramTitles.contains(ptp.title.trim().toUpperCase())){
                    paramTitles.add(ptp.title.trim().toUpperCase())
                }
                instance.addToParameters(ptp)
            }
        }
    }

    def getHiddenValues(params){
        return params."parameters.hidden" && params."parameters.hidden" != "" ? params."parameters.hidden".split("_"):null
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def edit(Long id) {
        PublisherTemplate instance = PublisherTemplate.read(id)
        if (!instance) {
            notFound()
            return
        }
        instance.parameters = instance.parameters?.sort{it.name}
        Set reportNamesList = getReportNamesList(instance)
        if(reportNamesList && reportNamesList.size()>0) {
            flash.warn = message(code: "app.template.usage.reports", args: [reportNamesList.size()]) + " - " + reportNamesList.join(', ')
        }
        render view: "edit", model: [instance: instance]
    }

    def getReportNamesList(PublisherTemplate instance) {
        List <PublisherConfigurationSection> publisherConfigurationSectionList = PublisherConfigurationSection.findAllByPublisherTemplate(instance)
        Set reportNamesList = []
        publisherConfigurationSectionList.each{
            reportNamesList.add(it?.configuration?.reportName)
            reportNamesList.add(it?.executedConfiguration?.reportName)
        }
        reportNamesList.remove(null)
        if(reportNamesList && reportNamesList.size()>0) {
            reportNamesList = reportNamesList.unique()
        }
        return reportNamesList
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def update() {
        PublisherTemplate instance = PublisherTemplate.get(params.id)
        if (!instance) {
            notFound()
            return
        }
        try {
            MultipartFile file = request.getFile('file')
            List<String> uniqueParamTitles = new ArrayList<>()
            populateProperties(instance, params, request, uniqueParamTitles)
            if (!userService.isAnyGranted("ROLE_QUALITY_CHECK")) {
                instance.qualityChecked = false
            }            
            if(instance.parameters && uniqueParamTitles.size() != instance.parameters.size()){
                response.status = 500
                render([error: [message(code: 'com.rxlogix.config.publisher.PublisherTemplate.duplicate.tile')]] as JSON)
                return
            }
            CRUDService.update(instance)
        } catch (ValidationException ve) {
            Preference preference = userService.currentUser?.preference
            Locale locale = preference?.locale
            List allErrorMessages = instance.errors.allErrors.collect {
                messageSource.getMessage(it, locale)
            }
            response.status = 500
            render([error: allErrorMessages] as JSON)
            return
        }
        String message = message(code: 'default.updated.message', args: [message(code: 'app.label.PublisherTemplate.appName', default: 'Publisher Template'), instance.name])
        render([message: message] as JSON)
    }

    def show(Long id) {
        PublisherTemplate instance = PublisherTemplate.read(id)
        if (!instance) {
            notFound()
            return
        }
        render view: "show", model: [instance: instance]
    }

    def publisherTemplateParameterList() {
        PublisherTemplate template = PublisherTemplate.read(params.id)
        List<PublisherTemplateParameter> PublisherTemplateParameterList = formListOfParameters(PublisherTemplateParameter.publisherTemplateParameterByTemplateAndSearchString(template, params.searchString).list([max: params.length, offset: params.start, sort: params.sort, order: params.direction]))
        render([aaData: PublisherTemplateParameterList, recordsTotal: PublisherTemplateParameter.publisherTemplateParameterByTemplateAndSearchString(template, null).count(), recordsFiltered: PublisherTemplateParameter.publisherTemplateParameterByTemplateAndSearchString(template, params.searchString).count()] as JSON)
    }

    private formListOfParameters(List <PublisherTemplateParameter> parameters){
        List parametersList = []
        if (parameters) {
            parameters.each {
                Map <String,String> row = [:]
                row << ['id': it.id.toString()]
                row << ['name': it.name]
                row << ['hidden': it.hidden]
                row << ['title': it.title]
                row << ['description': it.description]
                row << ['value': it.value]
                row << ['type': it.type==PublisherTemplateParameter.Type.QUESTIONNAIRE ? message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.QUESTIONNAIRE.name()):message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.TEXT.name())+"/"+message(code:"app.PublisherTemplateParameter.Type."+PublisherTemplateParameter.Type.CODE.name())]
                parametersList.add(row)
            }
        }
        return parametersList
    }

    @Secured(['ROLE_SYSTEM_CONFIGURATION'])
    def delete(Long id) {
        PublisherTemplate instance = PublisherTemplate.get(id)
        if (!instance) {
            notFound()
            return
        }
        Set reportNamesList = getReportNamesList(instance)
        if(reportNamesList && reportNamesList.size()>0) {
            flash.error = message(code: "app.template.usage.reports", args: [reportNamesList.size()]) + " - " + reportNamesList.join(', ')
            redirect(action: "index")
            return
        }
        try {
            CRUDService.softDelete(instance, instance.name, params.deleteJustification)
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'app.label.PublisherTemplate.appName', default: 'Publisher Template'), instance.name])}"
        } catch (ValidationException ve) {
            flash.error = message(code: "app.label.PublisherTemplate.delete.error.message")
        }
        redirect(action: "index")
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'app.label.PublisherTemplate.appName'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: HttpStatus.NOT_FOUND }
        }
    }

    def downloadAttachment() {
        PublisherTemplate t = PublisherTemplate.get(params.long("id"))
        if (!t) {
            notFound()
            return
        }
        render(file: t.template, fileName: t.fileName, contentType: "application/octet-stream")
    }

    def fetchParameters() {
        MultipartFile file = request.getFile('file')
        Map<String, List<PublisherTemplateParameter>> parametersMap = publisherService.fetchParameters(file.inputStream)
        List<PublisherTemplateParameter> newParameters = parametersMap.validParam
        List<PublisherTemplateParameter> existingParameters =  getExistingTemplateParameters()
          if (!validateFileType(file)) {
            response.status = 500
            render([error: getInvalidFileTypeErrorMsg()] as JSON)
            return
        }
        String dataValidationMsg = validateParameters(newParameters, existingParameters)
        if(dataValidationMsg){
            response.status = 500
            render([error: dataValidationMsg] as JSON)
            return
        }
        List dataList = []
        if(existingParameters && !existingParameters.isEmpty()){
            compareAndMergeParameters(dataList, newParameters, existingParameters)
            //Return invalid parameters names if any
            dataList[2] = parametersMap.invalidParam
            render dataList as JSON
            return
        }
        dataList[0] = newParameters
        dataList[1] = []
        dataList[2] = parametersMap.invalidParam
        render dataList as JSON
    }

    void compareAndMergeParameters(List dataList, List<PublisherTemplateParameter> newParameters, List<PublisherTemplateParameter> existingParameters){
        List<String> existingParametersNames = existingParameters.name
        List<String> newParametersNames = newParameters.name
        List<Integer> parametersStateList = []
        existingParametersNames.each {
            if(newParametersNames*.toLowerCase().contains(it.toLowerCase())){
                parametersStateList.add(0)
            }else{
                parametersStateList.add(-1)
            }
        }
        List<PublisherTemplateParameter> filteredNewList = []
        newParameters.each {it-> if(!existingParametersNames*.toLowerCase().contains(it.name.toLowerCase())){
            filteredNewList.add(it)
            parametersStateList.add(1)
        }}
        existingParameters.addAll(filteredNewList)
        dataList[0] = existingParameters
        dataList[1] = parametersStateList
    }

    List<PublisherTemplateParameter> getExistingTemplateParameters(){
        if(params.'id'){
            PublisherTemplate instance = PublisherTemplate.read(params.long('id'))
            return instance?.parameters?.sort{it.id}
        }
        def hiddenArray = getHiddenValues(params)
        List<PublisherTemplateParameter> parameters = []
        params."parameters.name"?.eachWithIndex { r, i ->
            if (params."parameters.name"[i] && i > 0) {
                PublisherTemplateParameter ptp = new PublisherTemplateParameter(
                        name: params."parameters.name"[i],
                        title: params."parameters.title"[i],
                        description: params."parameters.description"[i],
                        value: params."parameters.value"[i],
                        hidden: (hiddenArray && hiddenArray[i-1] == '1')? true:false,
                        type: params."parameters.type"[i] as PublisherTemplateParameter.Type
                )
                parameters.add(ptp)
            }
        }
        return parameters
    }

    static boolean validateFileType(MultipartFile file){
        String fileName = file?.originalFilename?.toLowerCase()
        if(fileName && PublisherTemplate.allowedFileTypes.contains(fileName.substring(fileName.lastIndexOf(".") + 1))){
            return true
        }
        return false
    }

    String getInvalidFileTypeErrorMsg(){
        return message(code: 'com.rxlogix.config.publisher.PublisherTemplate.fileName.invalid')
    }

    void clearTemplateParameters(PublisherTemplate instance){
        instance.parameters?.collect { it }?.each {
            it.delete()
        }
        instance.parameters?.clear()
        instance.template = null
        instance.fileName = null
    }

    protected String validateParameters(List<PublisherTemplateParameter> parameters, List<PublisherTemplateParameter> existingParameters){
        if(!existingParameters || existingParameters.isEmpty()){
            if(!parameters || parameters.isEmpty()){
                return message(code: 'com.rxlogix.config.publisher.PublisherTemplate.parameters.nullable')
            }
        }
        return null
    }

    def fetchParametersFromOneDrive() {
        Map<String, List<PublisherTemplateParameter>> parametersMap = publisherService.fetchParameters(new ByteArrayInputStream(oneDriveRestService.getCheckInFile(params.lockCode, userService.currentUser)))
        List<PublisherTemplateParameter> parameters = parametersMap.validParam
        render parameters as JSON
    }

    def testScript() {
        ExecutedPublisherSource attachment = new ExecutedPublisherSource()
        attachment.name = params.name
        attachment.fileType = params.fileType as BasicPublisherSource.FileType
        attachment.script = params.script
        attachment.discard()
        Map result = publisherSourceService.runScript(attachment)
        render result.log
    }
}