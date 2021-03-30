package com.wefinet.auto.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.Data;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

@Service
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
           Firestore firestore = FirestoreClient.getFirestore();
           EventListener<DocumentSnapshot> eventListener = (documentSnapshot, e) -> {
               ZaloCommand temp = documentSnapshot.toObject(ZaloCommand.class);
               System.out.println(temp.toString());
           };
           firestore.collection("zalo_bot").document("command").addSnapshotListener(eventListener);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

}
@Data
class ZaloCommand{
    private String action;
    private String id ;
    private Date lastUpdate;
}
