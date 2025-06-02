package com.rxlogix.jasperserver

public abstract class Resource {

    public static String URI_PROTOCOL = "repo"
    public static int VERSION_NEW = -1

    Long id
    int version
    Date creationDate
    Date updateDate
    String name
    String label
    String description
    String folderUri
    String uri

    Folder parent
    private String resourceType


    private static String getParentFolderFromUri(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        int lastSeparator = s.lastIndexOf(Folder.SEPARATOR);

        if (lastSeparator < 0) {
            return null;
        }

        if (lastSeparator == 0) {
            return Folder.SEPARATOR;
        }

        return s.substring(0, lastSeparator);
    }

    private static String getNameFromUri(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        int lastSeparator = s.lastIndexOf(Folder.SEPARATOR);

        if (lastSeparator < 0 || lastSeparator == s.length() - 1) {
            return null;
        }

        return s.substring(lastSeparator + 1, s.length());
    }

    protected Resource() {
        version = VERSION_NEW
    }

    public void setName(String name) {
        this.uri = null
        this.name = name
    }

    public void setParent(Folder parent) {
        this.uri = null
        this.parent = parent
    }

    public String getParentPath() {
        return parent?.getURIString()
    }

    protected abstract Class getClientItf()

    public final Class getClientType() {
        return getClientItf()
    }

    protected void copyTo(Resource clientRes) {
        clientRes.setVersion(getVersion())
        clientRes.setCreationDate(getCreationDate())
        clientRes.setUpdateDate(getUpdateDate())
        clientRes.setName(getName())
        clientRes.setLabel(getLabel())
        clientRes.setDescription(getDescription())

        Folder parentFolder = getParent()
        if (parentFolder != null) {
            clientRes.setParent(parentFolder)
        }
    }

    public String getURIString() {
        if (uri == null) {
            Folder parentFolder = getParent()
            if (parentFolder == null || parentFolder.isRoot()) {
                uri = Folder.SEPARATOR + getName()
            } else {
                uri = parentFolder.getURIString() + Folder.SEPARATOR + getName()
            }
        }
        return uri
    }

    public void setURIString(String uri)
    {
        this.uri = uri;
        this.name = getNameFromUri(uri);
        this.folderUri = getParentFolderFromUri(uri);
    }

    public ResourceLookup toClientLookup() {
        Class clientItf = getClientItf()
        ResourceLookup clientRes = new ResourceLookup()
        clientRes.setResourceType(clientItf.getName())
        copyTo(clientRes)
        return clientRes
    }

    public boolean isNew() {
        return version == Resource.VERSION_NEW
    }


    public String getResourceType() {
        return resourceType != null ? resourceType : (resourceType = getClientType().getName())
    }

    public void setResourceType(String resourceType) {
        // No-op.
    }
}
