package com.syncme.syncme.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
@Slf4j
public class GoogleTokenVerifier {
    
    @Value("${google.client.id}")
    private String clientId;
    
    private GoogleIdTokenVerifier verifier;
    
    private GoogleIdTokenVerifier getVerifier() {
        if (verifier == null) {
            verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
        }
        return verifier;
    }
    
    public GoogleIdToken.Payload verify(String idTokenString) 
            throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = getVerifier().verify(idTokenString);
        
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            log.error("Invalid ID token");
            throw new IllegalArgumentException("Invalid ID token");
        }
    }
}
