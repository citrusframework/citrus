/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.cucumber.integration.glueexclusions;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_EXCLUDED_CLASS_NAME_PATTERN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Scans the glue package but drops the glue classes matching the excluded class name pattern.
 * {@link DroppedSteps} defines the same step as {@link KeptSteps}, so the suite fails with a
 * duplicate step definition when the filter is not applied.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("org.citrusframework.cucumber.integration.glueexclusions")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.citrusframework.cucumber.integration.glueexclusions")
@ConfigurationParameter(key = GLUE_EXCLUDED_CLASS_NAME_PATTERN_PROPERTY_NAME, value = "^org\\.citrusframework\\.cucumber\\.integration\\.glueexclusions\\.DroppedSteps$")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,org.citrusframework.cucumber.CitrusReporter")
public class GlueExclusionsFeatureIT {
}
