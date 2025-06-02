package com.rxlogix.repo

import com.rxlogix.jasperserver.Folder

class RepoFolder extends RepoResource {
    String uri

    static hasMany = [children: RepoResource, subFolders: RepoFolder]

    static mapping = {
        tablePerHierarchy false
        table name: "REPO_FOLDER"

        uri column: "URI"
    }

    static constraints = {
        uri(maxSize: 250)
        children(nullable: true)
        subFolders(nullable: true)
    }

    @Override
    transient protected  Class<Folder> getClientItf() {
        return Folder.class
    }

    // Added to load calculated field after object load in place of constructor.
    def afterLoad(){
        this.resourceType = getClientType().getName()
    }

    def beforeValidate(){
        this.resourceType = getClientType().getName()
    }
}
