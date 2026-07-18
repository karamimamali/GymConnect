package com.gymconnect.workload.cucumber;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

/**
 * Scenario-scoped state shared between step definition classes; recreated for
 * every scenario so scenarios stay independent.
 */
@Component
@ScenarioScope
public class ScenarioState {

    private String token;
    private int lastStatus;
    private String lastBody;

    /** Captures the status and body of the last REST call for the Then steps. */
    public void record(MvcResult result) {
        try {
            this.lastStatus = result.getResponse().getStatus();
            this.lastBody = result.getResponse().getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to read response body", e);
        }
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
}
