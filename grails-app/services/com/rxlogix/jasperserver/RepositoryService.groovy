package com.rxlogix.jasperserver

import com.jaspersoft.jasperserver.dto.resources.ClientResource
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup
import com.rxlogix.jasperserver.converters.ToClientConversionOptions
import com.rxlogix.jasperserver.converters.ToClientConverter
import com.rxlogix.jasperserver.converters.ToServerConversionOptions
import com.rxlogix.jasperserver.converters.ToServerConverter
import com.rxlogix.jasperserver.exception.*
import com.rxlogix.repo.RepoFileResource
import com.rxlogix.repo.RepoResource
import grails.gorm.transactions.Transactional

import javax.annotation.PostConstruct
import javax.mail.FolderNotFoundException
import javax.ws.rs.NotAcceptableException
import java.util.regex.Pattern

@Transactional
class RepositoryService {
    def root
    def resourceConverterProvider
    def fileResourceTypes = [
            "font",
            "jrxml",
            "jar",
            "prop",
            "jrtx",
            "xml",
            "json",
            "css",
            "accessGrantSchema",
            "olapMondrianSchema",
            "dashboardComponent",
            "img"
    ]
    def resourceIdNotSupportedSymbols= "[~!#\\\$%^|\\s`@&*()\\-+={}\\[\\]:\"'\\<\\>,?/\\|\\\\]"
    def nameWithNumber = Pattern.compile("^.*_\\d+\$", Pattern.CASE_INSENSITIVE)

    @PostConstruct
    def init() {
        root = new Folder(name: "/")
        def templates = new TemplatesFolder()
        root.addSubFolder(templates)
    }

    /**
     * Retrieves the full details of a resource from the repository.
     *
     * <p>
     * For resources that include binary data (such as {@link FileResource} and
     * {@link ContentResource}), the data will not be returned by this method
     * and a separate call needs to be made if the data is required.
     * </p>
     *
     * @param uri the path of the resource in the repository
     * @return the resource if found or <code>null</code> otherwise
     */
    public Resource getResource(String uri) {
        return findByURI(uri, false)
    }

    public Resource findByURI(String uri, boolean required) {
        if (uri == null) {
            //throw new JSException("jsexception.null.uri")
        }

        // Deal with URIs that come with "repo:" on the front

        final String repoURIPrefix = Resource.URI_PROTOCOL + ":"
        String workUri = uri.startsWith(repoURIPrefix) ? uri.substring(repoURIPrefix.length()) : uri

        int sep = workUri.lastIndexOf(Folder.SEPARATOR)
        Resource res = null
        if (sep >= 0) {
            String name = workUri.substring(sep + Folder.SEPARATOR_LENGTH)
            String folderName = workUri.substring(0, sep)
            if(log.isDebugEnabled()){
                log.debug("Looking for name: " + name + " in folder: " + folderName)
            }
            Folder folder = getFolder(folderName)
            if (folder != null) {
                if (name) {
                    res = findByName(folder, name, required)
                } else {
                    res = folder
                }
            } else {
                if(log.isDebugEnabled()){
                    log.debug("No folder: " + folderName)
                }
            }
        }
        return res
    }
    protected Resource findByName(Folder folder, String name, boolean required) {
        Resource resource
        resource = folder?.findSubFolder(name)
        if (!resource && folder && folder.children) {
            resource = folder.children.find {
                it.name == name
            }
        }
        return resource
    }

        /**
     * Removes resources with uris in one transaction
     *
     * @param uris of resources to remove
     * @throws AccessDeniedException if current user cannot delete at least one resource
     */
    void deleteResources(List<String> uris) throws ResourceNotFoundException, AccessDeniedException {

    }

    /**
     * Searches resources
     *
     * @param folderUri folder, in which search should be
     * @throws IllegalParameterValueException if some parameters values are invalid
     * @throws ResourceNotFoundException if specified folder uri not exists
     *
     */
    List<ClientResourceLookup> getResources(String folderUri)
            throws IllegalParameterValueException, ResourceNotFoundException {

        def folder = getFolder(folderUri)
        def result = []
        if (folder) {
            def subFolders = folder.subFolders
            if (subFolders) {
                result += subFolders.collect {
                    def lookup = it.toClientLookup()
                    def converter = resourceConverterProvider.getToClientConverter(lookup)
                    return converter.toClient(lookup, ToClientConversionOptions.getDefault())
                }
            }
        }
        if (folder && folder.children) {
            result += folder.children.collect {
                def lookup = it.toClientLookup()
                def converter = resourceConverterProvider.getToClientConverter(lookup)
                return converter.toClient(lookup, ToClientConversionOptions.getDefault())
            }
        }
        return result
    }

    protected Folder getFolder(String uri) {
        if (uri == null || uri.length() == 0 || uri.equals(Folder.SEPARATOR)) {
            return getRootFolder()
        }
        final String repoURIPrefix = Resource.URI_PROTOCOL + ":"
        String workUri = uri.startsWith(repoURIPrefix) ? uri.substring(repoURIPrefix.length()) : uri

        Folder folder = getRootFolder()
        for (StringTokenizer pathTokenizer = new StringTokenizer(workUri, Folder.SEPARATOR);
             pathTokenizer.hasMoreTokens() && folder != null;) {
            String pathToken = pathTokenizer.nextToken()
            if (pathToken && pathToken.length() > 0) {
                folder = folder.findSubFolder(pathToken)
            }
        }
        return folder
    }

    protected Folder getRootFolder() {
        return root
    }

    protected boolean folderExists(String uri) {
        return getFolder(uri) != null
    }

    public FileResourceData getFileResourceData(Resource resource) {
        if (resource instanceof FileResource) {
            return getResourceData(resource.getURIString())
        }
        if (resource instanceof ContentResource) {
            return getContentResourceData(resource.getURIString())
        }
        throw new IllegalStateException(resource.getURIString() + " is not a file")
    }

    private FileResourceData getResourceData(final String uri) {
        FileResource res = (FileResource) findByURI(uri, true)
        while (res.isFileReference()) {
            res = res.getReference()
        }
        return res.copyData()
    }

    private FileResourceData getContentResourceData(final String uri) {
        ContentResource res = (ContentResource) findByURI(uri, true)
        return res.copyData()
    }

    public Resource createResource(Resource serverResource, String parentUri, boolean createFolders) throws RemoteException {
        if (createFolders) {
            ensureFolderUri(parentUri)
        } else if (!folderExists(null, parentUri)) {
            throw new FolderNotFoundException(parentUri)
        }
        Folder parent = getFolder(parentUri)

        serverResource.setParent(parent)
        if (serverResource.getName() == null || "".equals(serverResource.getName())) {
            serverResource.setName(generateName(parentUri, serverResource.getLabel()))
        }
        if (serverResource instanceof Folder) {
            saveFolder((Folder) serverResource)
        } else {
            saveResource(serverResource)
        }
        return getResource(serverResource.getURIString())
    }

    public ClientResource saveOrUpdate(ClientResource clientResource, boolean overwrite, boolean createFolders, String clientType) throws RemoteException {
        final String uri = clientResource.getUri()
        Resource resource = getResource(uri)

        // asking not toServerConverter, but toClientConverter for types compatibility to avoid NullPointerException,
        // because BinaryDataResourceConverter returns null as serverResourceType
        /*
        if (resource != null) {
            // is it different type of resource?
            if (resourceConverterProvider.getToClientConverter(resource.getResourceType(), ClientTypeHelper.extractClientType(clientResource.getClass())) == null) {
                if (overwrite) {
                    deleteResource(uri)
                    resource = null
                } else {
                    throw new ResourceAlreadyExistsException(uri)
                }
            } else {
                if (!new Integer(resource.getVersion()).equals(clientResource.getVersion())){
                    if (overwrite) {
                        deleteResource(uri)
                        resource = null
                    } else {
                        throw new VersionNotMatchException()
                    }
                }
            }
        }*/
        resource = ((ToServerConverter<ClientResource, Resource, ToServerConversionOptions>)resourceConverterProvider.getToServerConverter(clientResource))
                .toServer(clientResource, resource, ToServerConversionOptions.getDefault().setOwnersUri(uri))
        if(resource.isNew()){
            //TODO: Creating of new resources is not supported yet
            //resource = createResource(resource, resource.getParentPath(), createFolders)
        } else {
            resource = updateResource(resource)
        }
        ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> toClientConverter = resourceConverterProvider.getToClientConverter(resource)
        if(clientType != null && !clientType.isEmpty()) {
            toClientConverter = resourceConverterProvider.getToClientConverter(resource.getResourceType(),
                    clientType)
            if (toClientConverter == null) {
                throw new NotAcceptableException(clientType)
            }
        }
        return toClientConverter.toClient(resource, ToClientConversionOptions.getDefault())
    }

    public Resource updateResource(Resource resource) throws ResourceNotFoundException, VersionNotMatchException {
        if (resource instanceof Folder) {
            saveFolder((Folder) resource)
        } else {
            saveResource(resource)
        }
        return getResource(resource.getURIString())
    }

    public Resource createFileResource(InputStream stream, String parentFolderUri, String name, String label,
                                       String description, String type, boolean createFolders) {
        Resource file = fileResourceTypes.contains(type) ? new FileResource() : new ContentResource()
        file.setLabel(label)
        file.setName(name)
        file.setDescription(description)
        file.setCreationDate(new Date())

        if (fileResourceTypes.contains(type)) {
            ((FileResource) file).readData(stream)
            ((FileResource) file).setFileType(type)
        } else {
            ((ContentResource) file).readData(stream)
            ((ContentResource) file).setFileType(type)
        }

        return createResource(file, parentFolderUri, createFolders)
    }

    public Resource updateFileResource(InputStream stream, String parentUri, String name, String label, String description, String type) {
        String uri = parentUri.endsWith(Folder.SEPARATOR) ? parentUri + name : parentUri + Folder.SEPARATOR + name
        Resource file = getResource(uri)

        if (file instanceof FileResource) {
            ((FileResource) file).readData(stream)
            ((FileResource) file).setFileType(type)
            ((FileResource) file).setReferenceURI(null)
        } else if (file instanceof ContentResource) {
            ((ContentResource) file).readData(stream)
            ((ContentResource) file).setFileType(type)
        } else {
            throw new NotAFileException(parentUri + Folder.SEPARATOR + name)
        }

        file.setLabel(label == null ? name : label)
        file.setDescription(description)

        return updateResource(file)
    }

    def deleteResource(String uri) {
        //throw NotImplementedException("RepositoryService.deleteResource ${uri}")
    }

    private void saveFolder(Folder folder) {
        //throw new NotImplementedException("RepositoryService.saveFolder ${folder.uri}")
    }

    private void saveResource(Resource resource) {
        // Supports only JRXML files
        if (resource instanceof FileResource && resource.fileType == FileResource.TYPE_JRXML) {
            RepoFileResource repoFile
            //if (resource.isNew()) {
            //    repoFile = new RepoFileResource()
            //} else {
            repoFile = RepoFileResource.findByName(resource.name)
            //}
            repoFile.copyFromClient(resource)
            repoFile.save(flush: true, failOnError: true)
        } else {
            //throw new NotImplementedException("RepositoryService.saveResource ${resource.uri}")
        }
    }

    private Folder ensureFolderUri(String uri) throws AccessDeniedException, ResourceAlreadyExistsException, IllegalParameterValueException {
        try {
            uri = "".equals(uri) ? Folder.SEPARATOR : uri
            Folder folder = getFolder(uri)
            if (folder == null) {
                if (getResource(null, uri) != null) {
                    throw new ResourceAlreadyExistsException(uri)
                }

                int lastSeparator = uri.lastIndexOf(Folder.SEPARATOR)
                String label = uri.substring(lastSeparator + 1, uri.length())
                if (!label.equals(transformLabelToName(label))){
                    throw new IllegalParameterValueException("folder.name", label)
                }

                folder = new Folder()
                Folder parent = getFolder(ensureFolderUri(uri.substring(0, lastSeparator)))
                folder.setParent(parent)
                folder.setName(label)
                folder.setLabel(label)

                saveFolder(folder)
            } else {
                // /Public and /public should be different folders
                if (!folder.getURIString().equals(uri)){
                    //throw new FolderAlreadyExistsException(uri, folder.getURIString())
                }
            }
            return folder
        } catch (org.springframework.security.access.AccessDeniedException spe) {
            throw new AccessDeniedException("Access denied", uri)
        }
    }

    private String generateName(String parentUri, String label){
        String name = transformLabelToName(label)
        String uri = parentUri + Folder.SEPARATOR + name
        Resource resource = getResource(uri)
        if (resource == null) {
            resource = getFolder(uri)
        }
        if (resource != null){
            if (nameWithNumber.matcher(name).matches()){
                int divider = name.lastIndexOf("_")
                Integer number = Integer.parseInt(name.substring(divider + 1)) + 1
                name = name.substring(0, divider + 1) + number.toString()
            }
            else {
                name = name.concat("_1")
            }
            name = generateName(parentUri, name)
        }
        return name
    }

    private String transformLabelToName(String label){
        return label.replaceAll(resourceIdNotSupportedSymbols, "_")
    }
}
