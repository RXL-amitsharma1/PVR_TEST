package com.rxlogix

import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.grails.datastore.mapping.multitenancy.web.SessionTenantResolver

@CompileStatic
@Log4j
class RxTenantResolver extends SessionTenantResolver {

    static final Integer DUMMY_TENANT = -1

    Boolean isMultiTenancyEnabled = (Holders.config.getProperty('pvreports.multiTenancy.enabled', Boolean))
    Integer defaultTenantId = (Holders.config.getProperty('pvreports.multiTenancy.defaultTenant', Integer))

    @Override
    Serializable resolveTenantIdentifier() throws TenantNotFoundException {
        if (!isMultiTenancyEnabled) {
            return defaultTenantId
        }
        try {
            return super.resolveTenantIdentifier()
        } catch (TenantNotFoundException e) {
            log.trace("Tenant not found for current thread ${Thread.currentThread().name}")
            //TODO need to add handling tenancy in localization plugin and threads.
            return DUMMY_TENANT
        }
    }
}
