package grails.plugin.springsession

class SpringSessionConfigProperties {

    int maxInactiveInterval
    String mapName
    Boolean allowPersistMutable

    SpringSessionConfigProperties(Map springSessionConfig) {
        maxInactiveInterval = springSessionConfig.get('timeout.interval') ?: 1800
        mapName = springSessionConfig.get('map.name') ?: 'spring:session'
        allowPersistMutable = springSessionConfig.get('allow.persist.mutable') ?: false
    }

}
