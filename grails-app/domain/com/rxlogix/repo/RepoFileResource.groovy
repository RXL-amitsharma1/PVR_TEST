package com.rxlogix.repo

import com.rxlogix.config.ReportTemplate
import com.rxlogix.jasperserver.FileResource
import com.rxlogix.jasperserver.Resource

class RepoFileResource extends RepoResource {
    String fileType
    byte[] data

    static belongsTo = ReportTemplate

    static mapping = {
        tablePerHierarchy false
        table name: "REPO_FILE_RESOURCE"

        fileType column: "FILE_TYPE"
        data column: "DATA", lazy: true
    }

    static constraints = {
        fileType(maxSize: 20)
        data(nullable: true, maxSize: 20971520)
    }

    protected void copyDataFrom(FileResource dataRes) {
        if (dataRes.isReference()) {
            setData(null)
        } else {
            //only update when the client has set some data
            if (dataRes.hasData()) {
                byte[] clientData = dataRes.copyData().data
                setData(clientData)
            }
        }
    }

    @Override
    protected void copyFrom(Resource clientRes) {
        super.copyFrom(clientRes)
        FileResource dataRes = (FileResource) clientRes
        setFileType(dataRes.fileType)
        copyDataFrom(dataRes)
    }

    @Override
    protected void copyTo(Resource clientRes) {
        super.copyTo(clientRes)
        FileResource fileRes = (FileResource) clientRes
        fileRes.setFileType(getFileType())
        if (hasClientOption(CLIENT_OPTION_FULL_DATA)) {
            copyDataTo(fileRes)
        } else {
            fileRes.setData(null)
        }
        fileRes.setReferenceURI(null)
    }

    protected void copyDataTo(FileResource fileRes) {
        fileRes.data = data
    }

    @Override
    transient protected  Class<FileResource> getClientItf() {
        return FileResource.class
    }

    String toString() {
        return "RepoFileResource" +id
    }

    // Added to load calculated field after object load in place of constructor.
    def afterLoad(){
        this.resourceType = getClientType().getName()
    }

    def beforeValidate(){
        this.resourceType = getClientType().getName()
    }
}
