package com.rxlogix.repo

import com.rxlogix.jasperserver.Resource
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class RepoResourceSpec extends Specification implements DomainUnitTest<RepoResource> {
    def resourceName
    def resourceLabel
    def resourceDescription

    def setup() {
        resourceName = "resourceName"
        resourceLabel = "resourceLabel"
        resourceDescription = "resourceDescription"
    }

    void "test copyFromClient() method"() {
        given: "Resource instance"
        def Resource resource = new ResourceStub()
        resource.name = resourceName
        resource.label = resourceLabel
        resource.description = resourceDescription
        def repoResource = new RepoResourceStub()

        when: "Call copyFromClient() on RepoResource instance"
        repoResource.copyFromClient(resource)

        then: "It copies fields to the RepoResource instance"
        repoResource.name == resourceName
        repoResource.label == resourceLabel
        repoResource.description == resourceDescription
    }

    void "test copyToClient() method"() {
        given: "RepoResource instance"
        def repoResource = new RepoResourceStub()
        repoResource.name = resourceName
        repoResource.label = resourceLabel
        repoResource.description = resourceDescription

        when: "Call copyToClient() on RepoResource instance"
        Resource resource = repoResource.copyToClient()

        then: "It copies fields to the Resource instance"
        resource.name == resourceName
        resource.label == resourceLabel
        resource.description == resourceDescription
    }

    static public class ResourceStub extends Resource {
        public ResourceStub() {
        }

        @Override
        protected Class getClientItf() {
            return ResourceStub.class
        }
    }

    public class RepoResourceStub extends RepoResource {
        public RepoResourceStub() {

        }

        @Override
        protected Class getClientItf() {
            return ResourceStub.class
        }
    }
}
