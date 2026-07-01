package com.example.demo.security;

import com.example.demo.exception.InvalidCredentialsException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier {

    private final String clientId;

    public GoogleTokenVerifier(@Value("${google.client-id}") String clientId) {
        this.clientId = clientId;
    }

    public GoogleIdToken.Payload verify(String token) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
            .setAudience(Collections.singletonList(clientId))
            .build();

            GoogleIdToken idToken = verifier.verify(token);

            if (idToken == null) {
                throw new InvalidCredentialsException("Invalid Google token");
            }

            return idToken.getPayload();

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCredentialsException("Google token verification failed: " + e.getMessage());
        }
    }
}