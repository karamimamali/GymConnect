package com.gymconnect.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME;

/**
 * Entry point for the BDD suite of the main service. Component scenarios are
 * tagged {@code @component}; scenarios covering the integration contract with
 * the workload microservice are tagged {@code @integration}.
 *
 * <p>Select scenarios from the CLI with, for example:</p>
 * <pre>
 *   ./gradlew :main-service:test --tests '*CucumberTest' -Dcucumber.filter.tags=@auth
 * </pre>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.gymconnect.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:build/reports/cucumber/main-service.html")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class CucumberTest {
}
