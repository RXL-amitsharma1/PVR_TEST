package com.rxlogix.rest_v2

import com.jaspersoft.jasperserver.dto.resources.ClientResource
import com.jaspersoft.jasperserver.dto.resources.ClientResourceListWrapper
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup
import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType
import com.rxlogix.jasperserver.ClientTypeHelper
import com.rxlogix.jasperserver.Resource
import com.rxlogix.jasperserver.exception.IllegalParameterValueException
import com.rxlogix.jasperserver.exception.MandatoryParameterNotFoundException
import com.rxlogix.util.MiscUtil
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

import javax.servlet.http.HttpServletResponse
import javax.ws.rs.NotAcceptableException
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Secured(["isAuthenticated()"])
class RepositoryRestController {
    public static final String HEADER_X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override"

    def userService
    def repositoryService
    def resourceConverterProvider

    def getResources() {
        String folderUri = params.folderUri
        List<String> type = params.list("type")
        Boolean recursive = params.boolean("recursive", true)
        String sortBy = params.sortBy
        Boolean expanded = params.boolean("expanded", true)
        String accept = request.getHeader(HttpHeaders.ACCEPT)
        log.debug("[RepositoryRestController.getResources] params.folderUri= ${params.folderUri}")

        if (ResourceMediaType.FOLDER_JSON.equals(accept) || ResourceMediaType.FOLDER_XML.equals(accept)) {
            params.uri = params.folderUri?: '/'
            forward(action: "getResourceDetails")
        } else {
            List<ClientResourceLookup> result = repositoryService.getResources(folderUri)
            def wrapped = new ClientResourceListWrapper(result)
            return render(text: MiscUtil.marshal(wrapped), contentType: "text/xml", encoding: "UTF-8")
        }
    }

    def getResourceDetails() {
        log.debug("[RepositoryRestController.getResourceDetails] params.uri= ${params.uri}")
        forward(controller: "resourceDetailsRest", action: "getResourceDetails")
    }

    def deleteResource() {
        repositoryService.deleteResource(params.uri)
        response.status = HttpServletResponse.SC_NO_CONTENT
    }

    def defaultPostHandler() {
        String httpMethod = request.getHeader(HEADER_X_HTTP_METHOD_OVERRIDE)
        if (httpMethod == "PUT") {
            forward(action: "defaultPutHandler")
        }
    }

    protected ClientResource parseEntity(InputStream entityStream, MediaType mediaType) throws IllegalParameterValueException {
        final String clientType = ClientTypeHelper.extractClientType(mediaType)
        if(clientType == null){
            throw new IllegalParameterValueException("resource Media-Type", mediaType != null ? mediaType.toString() : "null")
        }
        final Class<? extends ClientResource> clientTypeClass = resourceConverterProvider.getClientTypeClass(clientType)
        return MiscUtil.unmarshal(entityStream, clientTypeClass)
    }

    def defaultPutHandler() {
        String uri = params.uri.replaceAll("/\$", "")
        InputStream stream = request.inputStream
        MediaType mediaType = MediaType.valueOf(request.getHeader(HttpHeaders.CONTENT_TYPE))
        String accept = request.getHeader(HttpHeaders.ACCEPT)
        Boolean overwrite = params.getBoolean("overwrite", false)
        Boolean expanded = params.boolean("expanded", false)
        Boolean createFolders = params.getBoolean("overwrite", true)

        try {
            ClientResource resourceLookup = parseEntity(stream, mediaType)
            if(resourceLookup == null){
                throw new MandatoryParameterNotFoundException("resource body")
            }
            resourceLookup.setUri(uri)
            final ClientResource updatedResource
            try {
                updatedResource = repositoryService.saveOrUpdate(resourceLookup, overwrite,
                        createFolders, ClientTypeHelper.extractClientType(accept as String))
            } catch (NotAcceptableException e){
                // original exception comes with client type, not Mime-Type. Throw exception with proper Mime-Type here
                throw new NotAcceptableException(accept)
            }
            int createdVersion = Resource.VERSION_NEW + 1
            // if current version is '0' (new version for the resource to be created is '-1') and previous version isn't '0',
            // then send 201 (Created), otherwise - 200 (OK)
            Response.Status status = (updatedResource.getVersion() == createdVersion
                    && (resourceLookup.getVersion() == null || resourceLookup.getVersion() != createdVersion)) ? Response.Status.CREATED : Response.Status.OK

            if (expanded != null && expanded) {
                forward(controller: "resourceDetailsRest", action: "getResourceDetails", params: [uri: updatedResource.getUri(), expanded: true])
            } else {
                render(status: status.statusCode, contentType: "text/xml", text: MiscUtil.marshal(updatedResource))
            }
        } catch (IllegalParameterValueException e) {
            return forward(controller: "resourceDetailsRest", action: "defaultPutHandler")
        }
    }
}