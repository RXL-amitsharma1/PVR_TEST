package com.rxlogix.jasperserver

public class ResourceLookup extends Resource {

    private String resourceType;

    @Override
    protected Class getClientItf() {
        return null
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ResourceLookup)) {
            return false;
        }
        if (o == this) {
            return true;
        }

        String uri = this.getURIString();
        ResourceLookup resourceLookup = (ResourceLookup) o;
        return (uri != null && uri.equals(resourceLookup.getURIString()) && resourceType != null &&
                resourceType.equals(resourceLookup.getResourceType()));
    }

    public Serializable getIdentifier() {
        return getURIString();
    }

    public String getType() {
        return getResourceType();
    }

    @Override
    public int hashCode() {
        return getURIString().hashCode();
    }
}
