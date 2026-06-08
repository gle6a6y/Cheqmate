package project.cheqmate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${cheqmate.fcm.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try {
            GoogleCredentials credentials = loadCredentials();
            if (credentials == null) {
                log.warn("Firebase NOT initialized: no service-account credentials found. " +
                        "FCM push is disabled (notifications are still persisted and sent over SSE). " +
                        "Set cheqmate.fcm.credentials-path or GOOGLE_APPLICATION_CREDENTIALS to enable it.");
                return;
            }
            FirebaseApp.initializeApp(FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build());
            log.info("Firebase initialized — FCM push enabled.");
        } catch (Exception e) {
            log.warn("Firebase NOT initialized ({}). FCM push disabled; " +
                    "notifications still persisted and sent over SSE.", e.getMessage());
        }
    }

    private GoogleCredentials loadCredentials() throws Exception {
        if (credentialsPath != null && !credentialsPath.isBlank() && Files.exists(Path.of(credentialsPath))) {
            try (InputStream in = new FileInputStream(credentialsPath)) {
                return GoogleCredentials.fromStream(in);
            }
        }
        try {
            return GoogleCredentials.getApplicationDefault();
        } catch (Exception e) {
            return null;
        }
    }
}
