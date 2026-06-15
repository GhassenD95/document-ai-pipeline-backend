package tn.finix.documentaipipelinebackend.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method) && "/api/documents/upload".equals(path)) {
            String ip = request.getRemoteAddr();
            Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests. Please wait before uploading again.\",\"timestamp\":\"" + java.time.Instant.now() + "\"}");
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
