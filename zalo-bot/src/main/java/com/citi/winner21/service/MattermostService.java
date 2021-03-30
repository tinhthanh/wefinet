package com.citi.winner21.service;

import com.citi.winner21.ultils.HttpUtil;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class MattermostService {

    @Value("${mattermost-url:}")
    private String mattermostChannelURL;
    private Gson gson = new Gson();
    private long previousMsgTime = 0L;

    @Autowired
    private HttpUtil httpUtil;

    public void postMessage(String message) {
        if (System.currentTimeMillis() - previousMsgTime >= 60 * 60 * 1000) {
            previousMsgTime = System.currentTimeMillis();
            httpUtil.postRequestBody(this.mattermostChannelURL, gson.toJson(Collections.singletonMap("text", message)), String.class, null);
        }
    }
}



