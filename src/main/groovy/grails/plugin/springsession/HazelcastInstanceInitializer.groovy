package grails.plugin.springsession

import com.hazelcast.config.AttributeConfig
import com.hazelcast.config.Config
import com.hazelcast.config.IndexConfig
import com.hazelcast.config.IndexType
import com.hazelcast.config.JoinConfig
import com.hazelcast.config.NetworkConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import grails.core.GrailsApplication
import org.springframework.beans.factory.config.AbstractFactoryBean
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository
import org.springframework.session.hazelcast.Hazelcast4PrincipalNameExtractor

class HazelcastInstanceInitializer extends AbstractFactoryBean<HazelcastInstance> {

    GrailsApplication grailsApplication

    ConfigObject hazelcastConfig

    SpringSessionConfigProperties springSessionConfigProperties

    HazelcastInstance startHazelcastServer() {
        System.setProperty("hazelcast.ignoreXxeProtectionFailures", "true");

        String groupName = hazelcastConfig.group.name
        String groupPassword = hazelcastConfig.group.password
        int serverPort = hazelcastConfig.server.port
        boolean serverPortAutoIncrement = hazelcastConfig.server.auto.increment.port
        int serverPortCount = hazelcastConfig.server.portCount
        String serverOutboundPortDefinition = hazelcastConfig.server.outbound.port.definition
        String instanceName = hazelcastConfig.server.instance.name

        boolean enableManagementCenter = hazelcastConfig.management.center.enabled

        AttributeConfig attributeConfig = new AttributeConfig()
                .setName(Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
                .setExtractorClassName(Hazelcast4PrincipalNameExtractor.class.getName())

        Config config = new Config()

        config.getMapConfig(springSessionConfigProperties.getMapName())
                .addAttributeConfig(attributeConfig)
                .addIndexConfig(new IndexConfig(IndexType.HASH, Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE))

        config.setInstanceName(instanceName)
        config.setClusterName(groupName)
        //config.getGroupConfig().setName(groupName).setPassword(groupPassword)

        if(enableManagementCenter) {
            config.getManagementCenterConfig().setConsoleEnabled(true)
            config.getManagementCenterConfig().setDataAccessEnabled(true)
        }

        NetworkConfig networkConfig = config.getNetworkConfig()
        networkConfig.setPort(serverPort).setPortAutoIncrement(serverPortAutoIncrement)
        networkConfig.getInterfaces().setEnabled(false)
        networkConfig.setPortCount(serverPortCount)
        networkConfig.addOutboundPortDefinition(serverOutboundPortDefinition)

        JoinConfig joinConfig = networkConfig.getJoin()
        joinConfig.getMulticastConfig().setEnabled(false)
        joinConfig.getAwsConfig().setEnabled(false)
        joinConfig.getTcpIpConfig().setEnabled(true)

        String[] nodes = hazelcastConfig.network.nodes
        nodes.each { String node ->
            joinConfig.getTcpIpConfig().addMember(node)
        }

        HazelcastInstance hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(config)

        logger.info("Hazelcast server : " + hazelcastInstance.name + " joined to cluster having " + hazelcastInstance.getCluster().getMembers().size() + " members.")
        return hazelcastInstance
    }

    @Override
    Class<?> getObjectType() {
        return HazelcastInstance
    }

    @Override
    protected HazelcastInstance createInstance() throws Exception {
        return startHazelcastServer()
    }

    @Override
    void destroy() throws Exception {
        logger.info("Shutting down all hazelcast instances on this JVM...")
        Hazelcast.shutdownAll()
    }
}
