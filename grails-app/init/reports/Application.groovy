package reports

import com.hazelcast.core.Hazelcast
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.util.Holders
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration

@EnableAutoConfiguration(exclude = [SecurityFilterAutoConfiguration, SessionAutoConfiguration])
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void onShutdown(Map<String, Object> event) {
        //Stop hazel services
        if (Holders.config.getProperty('hazelcast.enabled', Boolean))
            Hazelcast.shutdownAll()
    }
}