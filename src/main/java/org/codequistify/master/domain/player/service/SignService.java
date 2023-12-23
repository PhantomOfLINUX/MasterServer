package org.codequistify.master.domain.player.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.codequistify.master.domain.player.dto.GoogleUserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SignService {
    private final Logger LOGGER = LoggerFactory.getLogger(SignService.class);
    private final OkHttpClient okHttpClient;
    private final Gson gson;


    @Value("${oauth2.google.client.id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${oauth2.google.client.pw}")
    private String GOOGLE_CLIENT_PW;
    @Value("${oauth2.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URI;
    @Value("${oauth2.google.token-uri}")
    private String GOOGLE_TOKEN_URI;
    @Value("${oauth2.google.resource-uri}")
    private String GOOGLE_RESOURCE_URI;

    public String getSocialSignInURL(){
        LOGGER.info("sign url");

        return "https://accounts.google.com/o/oauth2/auth?" +
                "client_id="+GOOGLE_CLIENT_ID +
                "&redirect_uri="+GOOGLE_REDIRECT_URI +
                "&response_type=code" +
                "&scope=email profile";
    }

    public GoogleUserResource socialLogin(String code) {
        String accessToken = getAccessToken(code);
        JsonObject userResource = getUserResource(accessToken).getAsJsonObject();

        LOGGER.info("{} {} {}", userResource.get("id"), userResource.get("email"), userResource.get("name"));

        return new GoogleUserResource(
                userResource.get("id").getAsString(),
                userResource.get("email").getAsString(),
                userResource.get("name").getAsString()
        );
    }
    private String getAccessToken(String code){

        RequestBody body = new FormBody.Builder()
                .add("code", code)
                .add("client_id", GOOGLE_CLIENT_ID)
                .add("client_secret", GOOGLE_CLIENT_PW)
                .add("redirect_uri", GOOGLE_REDIRECT_URI)
                .add("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url(GOOGLE_TOKEN_URI)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            JsonElement responseElement = gson.fromJson(response.body().charStream(), JsonElement.class);
            return responseElement.getAsJsonObject()
                    .get("access_token").getAsString();
        }
        catch (IOException exception){
            exception.printStackTrace();
        }
        return null;
    }

    private JsonElement getUserResource(String accessToken) {
        Request request = new Request.Builder()
                .url(GOOGLE_RESOURCE_URI)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            return gson.fromJson(response.body().charStream(), JsonElement.class);
        }
        catch (IOException exception){
            exception.printStackTrace();
        }
        return null;
    }
}
