package com.rxlogix.jasperserver

public class Folder extends Resource {
    static final String SEPARATOR = "/"
    static final int SEPARATOR_LENGTH = SEPARATOR.length()

    Set children
    Set<Folder> subFolders

    public String getURIString() {
        if (parent == null && Folder.SEPARATOR.equals(name)) {
            return Folder.SEPARATOR
        }
        return super.getURIString()
    }

    public Set<Resource> getChildren() {
        return children
    }

    public void addChild(Resource resource) {
        if (!children) {
            children = new HashSet()
        }
        resource.setParent(this)
        children.add(resource)
    }

    public boolean removeChild(Resource resource) {
        return children.remove(resource)
    }

    public void addSubFolder(Folder subFolder) {
        if (!this.subFolders) {
            this.subFolders = new LinkedHashSet<>()
        }
        subFolder.setParent(this)
        this.subFolders.add(subFolder)
    }

    public Folder findSubFolder(String name) {
        return subFolders?.find {it.name == name}
    }

    public boolean isRoot() {
        return getURIString().equals(Folder.SEPARATOR)
    }

    protected Class getClientItf() {
        return Folder.class
    }
}
