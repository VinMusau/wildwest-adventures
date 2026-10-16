package com.wildwest.api.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Spawns ngrok as a child process on application startup, if enabled.
 *
 * Behaviour:
 *  - If ngrok is already running with a tunnel to the configured URL,
 *    do nothing. This makes DevTools restarts safe: the second start
 *    finds the existing tunnel and skips.
 *  - If no tunnel exists, launch ngrok and keep a reference so we can
 *    terminate it on shutdown.
 *  - If we did not launch it, we do not terminate it — the user might
 *    have started it manually and want to keep it.
 */
@Component
@ConditionalOnProperty(name = "wildwest.dev.ngrok.enabled", havingValue = "true")
public class NgrokLauncher implements ApplicationRunner, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(NgrokLauncher.class);
    private static final String ADMIN_API = "http://127.0.0.1:4040/api/tunnels";

    private final NgrokProperties properties;
    private Process process;
    private boolean launchedByUs = false;

    public NgrokLauncher(NgrokProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (tunnelAlreadyRunning()) {
            log.info("ngrok tunnel to {} is already running — reusing it", properties.url());
            return;
        }

        log.info("Starting ngrok: {} http {} --url {}",
                properties.binary(), properties.port(), properties.url());

        ProcessBuilder pb = new ProcessBuilder(
                properties.binary(),
                "http",
                String.valueOf(properties.port()),
                "--url",
                properties.url()
        );
        pb.inheritIO();   // pipe ngrok's output to the backend console

        try {
            process = pb.start();
            launchedByUs = true;
            log.info("ngrok launched (pid {})", process.pid());
        } catch (IOException e) {
            log.error("Failed to launch ngrok. Is it installed and on PATH? Error: {}", e.getMessage());
            log.error("Disable with NGROK_ENABLED=false in .env if you'd rather run it manually.");
        }
    }

    @Override
    public void destroy() {
        if (!launchedByUs || process == null) return;
        if (process.isAlive()) {
            log.info("Shutting down ngrok (pid {})", process.pid());
            process.destroy();
            try {
                if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Query ngrok's local admin API. If it responds and mentions our
     * reserved URL, the tunnel is already live and we can skip launching.
     */
    private boolean tunnelAlreadyRunning() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ADMIN_API))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.statusCode() == 200 && res.body().contains(properties.url());
        } catch (Exception e) {
            // Admin API unreachable — ngrok is not running
            return false;
        }
    }
}