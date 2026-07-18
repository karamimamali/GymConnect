package com.gymconnect.cucumber;

import io.cucumber.spring.ScenarioScope;
import jakarta.jms.Message;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Scenario-scoped state shared between step definition classes. A fresh
 * instance is created for every scenario, which keeps scenarios independent
 * (the I in FIRST) even though they run against one shared Spring context.
 */
@Component
@ScenarioScope
public class ScenarioState {

    /** Credentials returned by the registration endpoints. */
    public record Credentials(String username, String password) {
    }

    private final Map<String, Credentials> registeredUsers = new HashMap<>();
    private String token;
    private int lastStatus;
    private String lastBody;
    private Message lastWorkloadMessage;

    /** Captures the status and body of the last REST call for the Then steps. */
    public void record(MvcResult result) {
        try {
            this.lastStatus = result.getResponse().getStatus();
            this.lastBody = result.getResponse().getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to read response body", e);
        }
    }

    /** Adds the bearer token of the logged-in user, when one is present. */
    public MockHttpServletRequestBuilder authorize(MockHttpServletRequestBuilder builder) {
        return token == null ? builder : builder.header("Authorization", "Bearer " + token);
    }

    public void rememberUser(String firstName, String lastName, Credentials credentials) {
        registeredUsers.put(key(firstName, lastName), credentials);
    }

    public Credentials credentialsOf(String firstName, String lastName) {
        Credentials credentials = registeredUsers.get(key(firstName, lastName));
        if (credentials == null) {
            throw new IllegalStateException(
                    "No user registered in this scenario for " + firstName + " " + lastName);
        }
        return credentials;
    }

    public String usernameOf(String firstName, String lastName) {
        return credentialsOf(firstName, lastName).username();
    }

    private String key(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLastStatus() {
        return lastStatus;
    }

    public String getLastBody() {
        return lastBody;
    }

    public Message getLastWorkloadMessage() {
        return lastWorkloadMessage;
    }

    public void setLastWorkloadMessage(Message lastWorkloadMessage) {
        this.lastWorkloadMessage = lastWorkloadMessage;
    }
}
