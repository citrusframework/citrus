/*
 * File: TestLinkEnum.java
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
 * last modified: Saturday, January 14, 2012 (08:51) by: Matthias Beil
 */
package com.consol.citrus.testlink.citrus;

/**
 * Enumeration for CITRUS / TestLink constants.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public enum CitrusTestLinkEnum {

    /** URL for accessing TestLink. */
    Url("testlink.url"),

    /** Developer key needed for accessing TestLink. */
    Key("testlink.key"),

    /** TestPlan ID. */
    PlanId("testlink.testplan.id"),

    /** Build ID. */
    BuildId("testlink.build.id"),

    /** Name of Build. */
    BuildName("testlink.build.name"),

    /** TestCase ID. */
    CaseId("testlink.testcase.id"),

    /** TestCase internal ID. */
    CaseInternalId("testlink.testcase.internal.id"),

    /** TestCase notes. */
    CaseNotes("testlink.testcase.notes"),

    /** TestCase platform. */
    CasePlatform("testlink.testcase.platform"),

    /** TestCase execution status. */
    CaseExecutionStatus("testlink.testcase.execution.status");

    /** key. */
    private final String key;

    /**
     * Constructor for {@code TestLinkEnum} class.
     *
     * @param keyIn
     *            Key value to use for CITRUS variables.
     */
    private CitrusTestLinkEnum(final String keyIn) {

        this.key = keyIn;
    }

    /**
     * Returns the value of the {@code key} field.
     *
     * @return {@code key} field.
     */
    public String getKey() {

        return this.key;
    }

}
