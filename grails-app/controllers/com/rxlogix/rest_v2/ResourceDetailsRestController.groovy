package com.rxlogix.rest_v2

import com.jaspersoft.jasperserver.dto.resources.ClientFile
import com.jaspersoft.jasperserver.dto.resources.ClientResource
import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType
import com.rxlogix.jasperserver.ClientTypeHelper
import com.rxlogix.jasperserver.ContentResource
import com.rxlogix.jasperserver.FileResource
import com.rxlogix.jasperserver.FileResourceData
import com.rxlogix.jasperserver.Folder
import com.rxlogix.jasperserver.Resource
import com.rxlogix.jasperserver.SelfCleaningFileResourceDataWrapper
import com.rxlogix.jasperserver.converters.ToClientConversionOptions
import com.rxlogix.jasperserver.converters.ToClientConverter
import com.rxlogix.jasperserver.exception.IllegalParameterValueException
import com.rxlogix.util.MiscUtil
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Secured(["isAuthenticated()"])
class ResourceDetailsRestController {
    public static final String PATH_PARAM_URI = "uri"

    def repositoryService
    def resourceConverterProvider
    private Map<String, String> contentTypeMapping = [
            "pdf": "application/pdf",
            "html": "text/html",
            "xls": "application/xls",
            "rtf": "application/rtf",
            "csv": "text/csv",
            "odt": "application/vnd.oasis.opendocument.text",
            "txt": "text/plain",
            "docx": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "ods": "application/vnd.oasis.opendocument.spreadsheet",
            "xlsx": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "font": "font/*",
            "jrxml": "application/xml",
            "jar": "application/zip",
            "prop": "text/plain",
            "jrtx": "application/xml",
            "xml": "application/xml",
            "json": "application/json",
            "css": "text/css",
            "accessGrantSchema": "application/xml",
            "olapMondrianSchema": "application/xml",
            "pptx": "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "json": "application/json",
            "dashboardComponent": "application/dashboardComponentsSchema+json"
    ]

    def getResourceDetails() {
        def accept = request.getHeader(HttpHeaders.ACCEPT)
        Boolean expanded = params.boolean("expanded", false)
        List<String> includes = params.list("include")

        final String firstMatch = accept != null ? accept.split("")[0].split(",")[0] : null
        Resource resource = repositoryService.getResource(params.uri)
        if(resource != null) {

            final String clientType = ClientTypeHelper.extractClientType(accept)
            if (clientType == null
                    && (resource instanceof FileResource || resource instanceof ContentResource)
                    && !ResourceMediaType.FILE_XML.equals(firstMatch)
                    && !ResourceMediaType.FILE_JSON.equals(firstMatch)) {
                FileResourceData data = repositoryService.getFileResourceData(resource)
                FileResourceData wrapper = new SelfCleaningFileResourceDataWrapper(data)
                String type = resource instanceof FileResource ? ((FileResource) resource).getFileType() : ((ContentResource) resource).getFileType()
                renderFileResourceData(wrapper, resource.getName(), type)
            } else {
                ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> toClientConverter = null
                if (clientType != null) {
                    // try to find converter for specific combination of client type and server type
                    toClientConverter = resourceConverterProvider.getToClientConverter(resource.getResourceType(), clientType)
                }
                if (toClientConverter == null) {
                    // no client type or no converter for client/server type combination. Let's take server type converter then
                    toClientConverter = resourceConverterProvider.getToClientConverter(resource)
                }
                final ClientResource clientResource = toClientConverter.toClient(resource,
                        ToClientConversionOptions.getDefault().setExpanded(expanded).setIncludes(includes))
                String contentTypeTemplate = firstMatch != null && firstMatch.endsWith("json") ?
                        ResourceMediaType.RESOURCE_JSON_TEMPLATE : ResourceMediaType.RESOURCE_XML_TEMPLATE
                String contentType = contentTypeTemplate.replace(ResourceMediaType.RESOURCE_TYPE_PLACEHOLDER,
                        toClientConverter.getClientResourceType())
                render(text: MiscUtil.marshal(clientResource), contentType: contentType, encoding: "UTF-8")
            }
        } else {
            response.status = 404
        }
    }

    def defaultPutHandler() {
        String uri = params.uri.replaceAll("/\$", "")
        String sourceUri = request.getHeader(HttpHeaders.CONTENT_LOCATION)
        String disposition = request.getHeader("Content-Disposition")
        String description = request.getHeader("Content-Description")
        String mediaType = request.getHeader(HttpHeaders.CONTENT_TYPE)

        if (sourceUri != null) {

        } else {
            // uri - uri of resource
            if (uri == null || uri.endsWith(Folder.SEPARATOR)) {
                throw new IllegalParameterValueException(PATH_PARAM_URI, uri)
            }

            int lastSeparator = uri.lastIndexOf(Folder.SEPARATOR)
            String name = uri.substring(lastSeparator + Folder.SEPARATOR_LENGTH)
            String parentFolderUri = uri.substring(0, lastSeparator)
            parentFolderUri = parentFolderUri.equals("") ? Folder.SEPARATOR : parentFolderUri
            String type = extractType(mediaType, name)

            Resource file
            Response.Status status = Response.Status.OK
            String label = name
            if (disposition != null) {
                if (!disposition.contains("filename=") || disposition.endsWith("filename=")) {
                    throw new IllegalParameterValueException("Content-Disposition", disposition)
                } else {
                    label = disposition.split("filename=")[1]
                }
            }

            if (repositoryService.getResource(uri) == null) {
                file = repositoryService.createFileResource(request.inputStream, parentFolderUri, name, label, description, type, true)
                status = Response.Status.CREATED
            } else {
                file = repositoryService.updateFileResource(request.inputStream, parentFolderUri, name, label, description, type)
            }
            ClientResource clientFile = resourceConverterProvider.getToClientConverter(file).toClient(file, null)
            //response = Response.status(status).entity(clientFile).build()
        }
    }

    protected Object renderFileResourceData(FileResourceData data, String name, String fileType) {
        if(!data.hasData()){
            response.status = HttpServletResponse.SC_NO_CONTENT
        } else {
            String contentType = contentTypeMapping.get(fileType)
            if (contentType == null) {
                if (name.contains(".") && !name.endsWith(".")) {
                    contentType = contentTypeMapping.get(name.substring(name.lastIndexOf(".") + 1))
                }
            }

            if (contentType == null && ContentResource.TYPE_IMAGE.equals(fileType)) {
                try {
                    Iterator<ImageReader> readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(data.getDataStream()))
                    String format = null
                    while (readers.hasNext()) {
                        format = readers.next().getFormatName()
                    }
                    contentType = "image/" + (format == null ? "*" : format.toLowerCase())
                } catch (Throwable e) {
                    // Some unknown file, which pretend to be an image. Ignore it.
                }
            }
            render( file: data.data, contentType: contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType)
        }
    }

    protected String extractType(String mimeType, String name) {
        String type = null
        if (mimeType.contains("")) {
            mimeType = mimeType.split("")[0].trim()
        }

        if (mimeType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
            type = ContentResource.TYPE_UNSPECIFIED
        } else {
            String wildcardMimeType = mimeType.replaceFirst("/.*", "/*")
            for (ClientFile.FileType eType : ClientFile.FileType.values()) {
                if (eType.getMimeType().equalsIgnoreCase(mimeType) || eType.getMimeType()
                        .equalsIgnoreCase(wildcardMimeType)) {
                    type = eType.toString()
                }
            }

            // let's try to analyse extension
            if (type == null && name.contains(".") && !name.endsWith(".")) {
                String extension = name.substring(name.lastIndexOf(".") + 1)
                if (contentTypeMapping.containsKey(extension)) {
                    type = extension
                }
            }
        }

        return type == null ? ContentResource.TYPE_UNSPECIFIED : type
    }
}
