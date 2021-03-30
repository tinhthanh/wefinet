package com.citi.winner21.service;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
@Service("FirebaseInitialization")
public class FirebaseInitialization {
    @PostConstruct
    public void initialization() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("./wefinex-73e57.json");
            FirebaseOptions options = null;
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
