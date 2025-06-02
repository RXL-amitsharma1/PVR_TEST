package com.rxlogix.cmis

import com.rxlogix.util.MiscUtil
import grails.util.Holders
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

class ApiAdapter implements AdapterInterface {

    def settings
    private static Logger logger = LoggerFactory.getLogger(getClass())

    ApplicationContext getApplicationContext() {
        return Holders.getGrailsApplication().mainContext
    }

    @Override
    void init(Object _settings) {
        settings = _settings
    }

    @Override
    void load(File reportFile, String subfolder, String name, String description, String tag, String sensitivity, String author, Object object) {
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>()
        HttpHeaders headers = getRequestHttpHeaders()
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers)
        populateJsonAttributesMap(bodyMap, reportFile, subfolder, name, description, tag, sensitivity, author)
        Map<String, String> objectProperties = getObjectPropertiesMap(object);
        if (objectProperties) {
            bodyMap.add("objectProperties", JsonOutput.toJson(objectProperties))
        }
        ResponseEntity<String> response = generateResponse(requestEntity)
        if (response.statusCode != HttpStatus.OK) {
            throw new Exception("Load failed due to ${response.toString()} with ${response.statusCode}")
        }
    }

    @Override
    List<String> getFolderList(String folder, Object object) {
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>()
        bodyMap.add("folder", folder)
        Map<String, String> objectProperties = getObjectPropertiesMap(object);
        if (objectProperties) {
            bodyMap.add("objectProperties", JsonOutput.toJson(objectProperties))
        }
        List<String> folderList = getFolderListForApiAdapter(bodyMap, settings.dmsFolderListApiUrl)
        return folderList
    }

    Map<String,String> getObjectPropertiesMap(def object) {
        Map<String,String> propertiesValues = [:]
        if (object && settings.objectProperties) {
            settings.objectProperties.each {
                try {
                    propertiesValues.put(it, MiscUtil.evaluate(object, it)?.toString())
                } catch (Exception ex) {
                    logger.error(ex.message)
                }
            }
        }
        return propertiesValues
    }

    HttpHeaders getRequestHttpHeaders() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.MULTIPART_FORM_DATA)
        settings.apiHeaders?.each {
            headers.set(it.getKey(), it.getValue())
        }
        return headers
    }

    def generateResponse(HttpEntity<MultiValueMap<String, Object>> requestEntity) {
        RestTemplate restTemplate = new RestTemplate()
        ResponseEntity<String> response = restTemplate.postForEntity(settings.dmsUploadApiUrl, requestEntity, String.class)
        return response
    }

    List<String> getFolderListForApiAdapter(MultiValueMap<String, Object> bodyMap, String getFolderListUrl) {
        HttpHeaders headers = getRequestHttpHeaders()
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers)
        RestTemplate restTemplate = new RestTemplate()
        ResponseEntity<List<String>> response = restTemplate.exchange(getFolderListUrl, HttpMethod.POST, requestEntity, List.class)
        List<String> folders = response.getBody()
        return folders
    }

    private void populateJsonAttributesMap(
            MultiValueMap<String, Object> bodyMap, File reportFile, String subfolder, String name, String description, String tag, String sensitivity, String author) {
        String folderPath = (settings.rootFolder ?: "") + (subfolder ?: "")
        Map attrMap = [folderPath : folderPath,
                       name       : name,
                       description: description,
                       tag        : tag,
                       sensitivity: sensitivity,
                       author     : author
        ]
        settings.additionalUploadProperties?.each {
            attrMap.put(it.getKey(), it.getValue())
        }
        def json = JsonOutput.toJson(attrMap)
        bodyMap.add("file", new FileSystemResource(reportFile))
        bodyMap.add("jsonAttributes", json)
    }
}
