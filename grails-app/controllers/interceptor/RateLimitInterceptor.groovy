package interceptor

import com.rxlogix.CustomMessageService
import grails.config.Config
import grails.converters.JSON
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.apache.http.HttpStatus
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@CompileStatic
class RateLimitInterceptor implements GrailsConfigurationAware {
    CustomMessageService customMessageService

    int order = -1                                                   // This interceptor should take precedence over all other interceptors
    Map<String, Bucket> clientBuckets = new ConcurrentHashMap<>()    // Store buckets per client IP
    final Duration DURATION = Duration.ofSeconds(60)        // Reset duration
    Map<String, Long> lastUsedTimestamps = new ConcurrentHashMap<>() // Track last usage for cleanup
    int LIMIT                                                        // Number of requests allowed per client
    int GLOBAL_LIMIT                                                 // Number of requests allowed irrespective of the client
    long expiryThresholdMinutes                                      // Threshold to periodically clean up inactive IP entries
    Bucket globalBucket                                              // Global bucket
    Boolean rateLimitEnabled

    RateLimitInterceptor() {
        match(uri: '/public/api/**')
        match(uri: '/scim/v2/**')
    }

    void setConfiguration(Config cfg) {
        LIMIT = cfg.getProperty('rate.limit.client.requests.per.minute', Integer, 1000)
        GLOBAL_LIMIT = cfg.getProperty('rate.limit.global.requests.per.minute', Integer, 10000)
        expiryThresholdMinutes = cfg.getProperty('rate.limit.bucket.expiry.minutes', Long, 60L)
        rateLimitEnabled = cfg.getProperty('rate.limit.enabled', Boolean, false)
        globalBucket = createBucket(GLOBAL_LIMIT, DURATION)
    }

    boolean before() {
        if (!rateLimitEnabled) {
            return true
        }
        try {
            // Identify client and check their bucket
            String clientKey = getClientKey()
            if (!clientKey) {
                renderResponse(customMessageService.getMessage('rate.limit.unknown.client'), HttpStatus.SC_BAD_REQUEST)     // Bad Request
                return false
            }

            // Fetch or create a bucket for the client
            Bucket bucket = clientBuckets.computeIfAbsent(clientKey) {
                createBucket(LIMIT, DURATION)
            }

            lastUsedTimestamps[clientKey] = System.currentTimeMillis() // Update last used timestamp

            if (!bucket.tryConsume(1)) {
                log.error("Rate limit threshold reached for client ${clientKey}")
                renderResponse(customMessageService.getMessage('rate.limit.threshold.reached') as String, HttpStatus.SC_TOO_MANY_REQUESTS)      // Too Many Requests
                return false
            }

            // Check the global bucket
            if (!globalBucket.tryConsume(1)) {
                log.error("Rate limit global threshold reached")
                renderResponse(customMessageService.getMessage('rate.limit.threshold.reached') as String, HttpStatus.SC_TOO_MANY_REQUESTS)      // Too Many Requests
                return false
            }
            return true
        } catch (Exception e) {
            log.error("Serious error in rate limit interceptor ", e)
            return true
        }
    }

    private String getClientKey() {
        String userIpAddress = ""
        if (request.getHeader("X-Forwarded-For") != null) {
            //for Environmens with load balancers
            userIpAddress = request.getHeader("X-Forwarded-For")
        } else {
            userIpAddress = request.getRemoteAddr()
        }
        return userIpAddress
    }

    private void renderResponse(String text, int statusCode) {
        String acceptHeader = request.getHeader("Accept")
        String contentType = request.getHeader("Content-Type")

        response.status = statusCode
        if (acceptHeader?.contains("application/json") || acceptHeader?.contains("*/*") || contentType?.contains("application/json")) {
            render([error: "${text}"] as JSON)
        } else {
            render(text: "${text}", contentType: "text/plain")
        }
    }

    private Bucket createBucket(int limit, Duration duration) {
        Bandwidth bandwidth = Bandwidth.classic(limit, Refill.greedy(limit, duration))
        return Bucket.builder().addLimit(bandwidth).build()
    }

    // Periodically clean up inactive IP entries
    void removeInactiveBuckets() {
        long now = System.currentTimeMillis()
        long expiryThreshold = Duration.ofMinutes(expiryThresholdMinutes).toMillis()
        clientBuckets.keySet().removeIf { ip ->
            def lastUsed = lastUsedTimestamps[ip] ?: 0
            if ((now - lastUsed) > expiryThreshold) {
                lastUsedTimestamps.remove(ip)
                return true
            }
            return false
        }
    }
}
