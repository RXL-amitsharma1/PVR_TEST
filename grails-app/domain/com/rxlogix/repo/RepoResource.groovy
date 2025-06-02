package com.rxlogix.repo

import com.rxlogix.jasperserver.Resource

abstract class RepoResource {
    public final static String CLIENT_OPTION_FULL_DATA = "fullData"
    public final static String CLIENT_OPTION_AS_NEW = "asNew"

    private static final ThreadLocal clientOptions = new ThreadLocal()

    String name
    String label
    String description
    String resourceType
    RepoFolder parent

    Integer version = Resource.VERSION_NEW
    Date dateCreated
    Date lastUpdated
    //static belongsTo = [parent: RepoFolder]

    static mapping = {
        tablePerHierarchy false
        table name: "REPO_RESOURCE"

        name column: "NAME"
        label column: "LABEL"
        description column: "DESCRIPTION"
        parent column: "PARENT_ID"
        resourceType column: "RESOURCE_TYPE"
    }

    static constraints = {
        name(maxSize: 1006) // name is combination of template name and '.jrxml' string.
        label(maxSize: 1000)
        description(nullable: true, maxSize: 250)
        resourceType(maxSize: 255)
        parent(nullable: true)
    }

    protected Map getClientOptions() {
        return (Map) clientOptions.get()
    }

    protected boolean hasClientOption(String option) {
        Map options = getClientOptions()
        return options != null && options.containsKey(option)
    }

    public boolean isNew() {
        return version == Resource.VERSION_NEW
    }

    public Object copyToClient(Map options = null) {
        if (options != null) {
            clientOptions.set(options)
        }
        try {
            Resource clientRes = getClientItf().getDeclaredConstructor().newInstance()
            copyTo(clientRes)
            return clientRes
        } finally {
            if (options != null) {
                clientOptions.remove()
            }
        }
    }

    public void copyFromClient(Object objIdent) {
        copyFrom((Resource) objIdent)
    }

    protected void copyFrom(Resource clientRes) {
        if (!isNew() && getVersion() != clientRes.getVersion()) {
            //throw new RuntimeException("jsexception.resource.no.match.versions")
            //throw new JSResourceVersionNotMatchException("jsexception.resource.no.match.versions", new Object[] {getResourceURI(), new Integer(clientRes.getVersion()), new Integer(getVersion())})
        }

        setName(clientRes.getName())
        setLabel(clientRes.getLabel())
        setDescription(clientRes.getDescription())
        /*
        if (UpdateDatesIndicator.shouldUpdate()) {
            setCreationDate(UpdateDatesIndicator.getOperationalDate())

            if (UpdateDatesIndicator.useOperationalForUpdateDate() || clientRes.getUpdateDate() == null) {
                setUpdateDate(UpdateDatesIndicator.getOperationalDate())
            } else {
                setUpdateDate(clientRes.getUpdateDate())
            }
        }
        */
    }

    protected void copyTo(Resource clientRes) {
        if (id) {
            clientRes.id = id
        }
        clientRes.version = version
        clientRes.creationDate = dateCreated
        clientRes.updateDate = lastUpdated
        clientRes.name = name
        clientRes.label = label
        clientRes.description = description

        if (parent != null) {
            //clientRes.setParentFolder(parentFolder.getURI())
        }
    }

    transient protected abstract Class<? extends Resource> getClientItf()

    transient public final  Class<? extends Resource> getClientType() {
        return getClientItf()
    }
}
