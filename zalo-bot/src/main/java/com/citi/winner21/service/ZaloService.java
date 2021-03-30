package com.citi.winner21.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ExecutionException;

@Service()
public class ZaloService {
    private  static  final  String COLLECTION_NANME = "zalo_bot";
    private  static  final  String FOLLOW_BET = "follow_bet";
    public void listenerCommand( EventListener<DocumentSnapshot> eventListener ) {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection(COLLECTION_NANME).document("command").addSnapshotListener(eventListener);
//        ApiFuture<DocumentSnapshot> future = documentReference.get();
//        DocumentSnapshot documentSnapshot = future.get();
//        WefinexResult product =  null ;command2
//        if(documentSnapshot.exists()) {
//            product = documentSnapshot.toObject(WefinexResult.class);
//            return  product;
//        } else  {
//            return  null;
//        }
    }
    public void listenerCommandTrade(EventListener<QuerySnapshot> listener ) {
        Firestore firestore = FirestoreClient.getFirestore();
        firestore.collection("wefinex").orderBy("lastUpdate", Query.Direction.DESCENDING).limit(20).addSnapshotListener(listener);
    }
    public  String saveQR(String qr) throws ExecutionException, InterruptedException {
        ZaloQR p = new ZaloQR();
        p.setQR(qr);
        Firestore firestore = FirestoreClient.getFirestore();
        p.setLastUpdate(new Date());
        ApiFuture<WriteResult> collectionApi =  firestore.collection(COLLECTION_NANME).document("QRCODE").set(p);
        return  collectionApi.get().getUpdateTime().toString();
    }
}
@Data
class ZaloCommand{
    private String action;
    private String id ;
    private Date lastUpdate;
}
@Data
class ZaloQR{
    private String QR;
    private Date lastUpdate;
}

@Data
 class WefinexCommand {
    private String type;
    private String price ;
    private String time ;
    private String action;
    private String id ;
    private Date lastUpdate;
}

