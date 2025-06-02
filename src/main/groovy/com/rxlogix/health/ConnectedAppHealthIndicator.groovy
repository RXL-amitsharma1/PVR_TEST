package com.rxlogix.health


import groovy.util.logging.Slf4j
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health

@Slf4j
class ConnectedAppHealthIndicator extends AbstractHealthIndicator {

    String appName
    String healthUrl

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (!pingURL(healthUrl, 5 * 1000)) {
            throw new Exception("Application is down or unhealthy ${appName} : ${healthUrl}")
        }
        builder.up()
    }

    public boolean pingURL(String url, int timeout) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            log.error("Error occurred while testing ${url} : ${exception.message}")
            return false;
        }
    }
}