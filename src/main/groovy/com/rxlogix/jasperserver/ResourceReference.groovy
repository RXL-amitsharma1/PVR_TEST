package com.rxlogix.jasperserver

import com.fasterxml.jackson.annotation.JsonIgnore

public class ResourceReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean local;
    private String referenceURI;
    private ResourceLookup referenceLookup;
    private Resource localResource;

    /**
     * Creates a new ResourceReference which references the specified URI
     *
     * @param referenceURI reference URI
     * @return resource reference
     */
    public ResourceReference(String referenceURI) {
        setReference(referenceURI);
    }

    /**
     * Creates a new ResourceReference which references the specified ResourceLookup
     *
     * @param referenceLookup reference to ResourceLookup
     * @return resource reference
     */
    public ResourceReference(ResourceLookup referenceLookup) {
        setReference(referenceLookup);
    }

    /**
     * Creates a new ResourceReference which contains specified resource
     *
     * @param localResource resource
     * @return resource reference
     */
    public ResourceReference(Resource localResource) {
        setLocalResource(localResource);
    }

    public ResourceReference() {

    }

    /**
     * Shows if ResourceReference contains its own local resource
     *
     * @return <code>true</code> if the resource is local
     */
    public boolean isLocal() {
        return local;
    }

    /**
     * Returns the local resource
     *
     * @return resource
     */
    public Resource getLocalResource() {
        return localResource;
    }

    /**
     * Returns resource reference URI string
     *
     * @return URI
     */
    public String getReferenceURI() {
        return referenceURI;
    }

    /**
     * Returns the ResourceLookup reference
     *
     * @return ResourceLookup
     */
    public ResourceLookup getReferenceLookup() {
        return referenceLookup;
    }

    /**
     * Returns the actual URI address irrespective of reference type
     *
     * @return URI
     */
    public String getTargetURI() {
        String uri;
        if (isLocal()) {
            uri = localResource == null ? null : localResource.getURIString();
        } else {
            uri = referenceURI;
        }
        return uri;
    }

    /**
     * Sets the resource to local container;
     *
     * @param localResource
     */
    public void setLocalResource(Resource localResource) {
        this.local = true;
        this.referenceURI = null;
        this.referenceLookup = null;
        this.localResource = localResource;
    }

    /**
     * Sets the reference URI to resource
     *
     * @param referenceURI
     */
    public void setReference(String referenceURI) {
        this.local = false;
        this.referenceURI = referenceURI;
        this.referenceLookup = null;
        this.localResource = null;
    }

    /**
     * Sets the reference lookup to resource
     *
     * @param referenceLookup
     */
    @JsonIgnore
    public void setReference(ResourceLookup referenceLookup) {
        this.local = false;
        this.referenceURI = referenceLookup.getURIString();
        this.referenceLookup = referenceLookup;
        this.localResource = null;
    }

}
