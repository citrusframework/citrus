/*
 * File: CitrusTestLinkEnum.java
 *
 * Copyright (c) 2006-2012 the original author or authors.
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
 *
 * last modified: Saturday, January 21, 2012 (20:03) by: Matthias Beil
 */
package com.consol.citrus.testlink;

/**
 * Enumeration for CITRUS / TestLink constants.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public enum CitrusTestLinkEnum {

    /** URL for accessing TestLink. URL without XML-RPC part. */
    Url("testlink.url", true),

    /** Developer key needed for accessing TestLink. */
    Key("testlink.key", true),

    /** Flag indicating if test should be written to TestLink. Defaults to {@code true}. */
    WriteToTestLink("testlink.write", true),

    /** TestPlan ID. */
    TestPlanId("testlink.testplan.id", true),

    /** Build ID. */
    BuildId("testlink.build.id", true),

    /** TestCase ID. */
    TestCaseId("testlink.testcase.id", true),

    /** TestCase internal ID. */
    TestCaseInternalId("testlink.testcase.internal.id", false),

    /** Notes in case of success. */
    NotesSuccess("testlink.notes.success", false),

    /** Notes in case of an failure, will be used as a prefix. */
    NotesFailure("testlink.notes.failure", false),

    /** TestCase platform. */
    TestCasePlatform("testlink.testcase.platform", false);

    /** mandatory. */
    private final boolean mandatory;

    /** key. */
    private final String key;

    /**
     * Constructor for {@code TestLinkEnum} class.
     * 
     * @param keyIn
     *            Key value to use for CITRUS variables.
     * @param mandatoryIn
     *            Defines if this value is mandatory ({@code true}) or not ({@code false}).
     */
    private CitrusTestLinkEnum(final String keyIn, final boolean mandatoryIn) {

        this.key = keyIn;
        this.mandatory = mandatoryIn;
    }

    /**
     * Returns the value of the {@code key} field.
     * 
     * @return {@code key} field.
     */
    public String getKey() {

        return this.key;
    }

    /**
     * Returns the value of the {@code mandatory} field.
     * 
     * @return {@code mandatory} field.
     */
    public boolean isMandatory() {

        return this.mandatory;
    }
}
