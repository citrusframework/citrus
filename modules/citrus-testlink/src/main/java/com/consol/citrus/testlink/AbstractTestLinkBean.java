/*
 * File: AbstractTestLinkBean.java
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
 * last modified: Saturday, January 21, 2012 (20:14) by: Matthias Beil
 */
package com.consol.citrus.testlink;

/**
 * Common values which are needed for CITRUS to TestLink and so also for TestLink to CITRUS handling.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class AbstractTestLinkBean {

    // ~ Instance fields -------------------------------------------------------------------------

    /** testPlanId. */
    private Integer testPlanId;

    /** buildId. */
    private Integer buildId;

    /** testCaseId. */
    private Integer testCaseId;

    /** testCaseInternalId. */
    private Integer testCaseInternalId;

    /** url. */
    private String url;

    /** key. */
    private String key;

    /** buildName. */
    private String buildName;

    /** platform. */
    private String platform;

    /** notes. */
    private String notesSuccess;

    /** notesFailure. */
    private String notesFailure;

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code url} field.
     *
     * @return {@code url} field.
     */
    public final String getUrl() {

        return this.url;
    }

    /**
     * Sets the value of the {@code url} field.
     *
     * @param urlIn
     *            field to set.
     */
    public final void setUrl(final String urlIn) {

        this.url = urlIn;
    }

    /**
     * Returns the value of the {@code key} field.
     *
     * @return {@code key} field.
     */
    public final String getKey() {

        return this.key;
    }

    /**
     * Sets the value of the {@code key} field.
     *
     * @param keyIn
     *            field to set.
     */
    public final void setKey(final String keyIn) {

        this.key = keyIn;
    }

    /**
     * Returns the value of the {@code platform} field.
     *
     * @return {@code platform} field.
     */
    public final String getPlatform() {

        return this.platform;
    }

    /**
     * Sets the value of the {@code platform} field.
     *
     * @param platformIn
     *            field to set.
     */
    public final void setPlatform(final String platformIn) {

        this.platform = platformIn;
    }

    /**
     * Returns the value of the {@code test plan id} field.
     *
     * @return {@code test plan id} field.
     */
    public final Integer getTestPlanId() {

        return this.testPlanId;
    }

    /**
     * Sets the value of the {@code test plan id} field.
     *
     * @param testPlanIdIn
     *            field to set.
     */
    public final void setTestPlanId(final Integer testPlanIdIn) {

        this.testPlanId = testPlanIdIn;
    }

    /**
     * Returns the value of the {@code build id} field.
     *
     * @return {@code build id} field.
     */
    public final Integer getBuildId() {

        return this.buildId;
    }

    /**
     * Sets the value of the {@code build id} field.
     *
     * @param buildIdIn
     *            field to set.
     */
    public final void setBuildId(final Integer buildIdIn) {

        this.buildId = buildIdIn;
    }

    /**
     * Returns the value of the {@code build name} field.
     *
     * @return {@code build name} field.
     */
    public final String getBuildName() {

        return this.buildName;
    }

    /**
     * Sets the value of the {@code build name} field.
     *
     * @param buildNameIn
     *            field to set.
     */
    public final void setBuildName(final String buildNameIn) {

        this.buildName = buildNameIn;
    }

    /**
     * Returns the value of the {@code test case id} field.
     *
     * @return {@code test case id} field.
     */
    public final Integer getTestCaseId() {

        return this.testCaseId;
    }

    /**
     * Sets the value of the {@code test case id} field.
     *
     * @param testCaseIdIn
     *            field to set.
     */
    public final void setTestCaseId(final Integer testCaseIdIn) {

        this.testCaseId = testCaseIdIn;
    }

    /**
     * Returns the value of the {@code test case internal id} field.
     *
     * @return {@code test case internal id} field.
     */
    public final Integer getTestCaseInternalId() {

        return this.testCaseInternalId;
    }

    /**
     * Sets the value of the {@code test case internal id} field.
     *
     * @param testCaseInternalIdIn
     *            field to set.
     */
    public final void setTestCaseInternalId(final Integer testCaseInternalIdIn) {

        this.testCaseInternalId = testCaseInternalIdIn;
    }

    /**
     * Returns the value of the {@code notes success} field.
     *
     * @return {@code notes success} field.
     */
    public final String getNotesSuccess() {

        return this.notesSuccess;
    }

    /**
     * Sets the value of the {@code notes success} field.
     *
     * @param notesSuccessIn
     *            field to set.
     */
    public final void setNotesSuccess(final String notesSuccessIn) {

        this.notesSuccess = notesSuccessIn;
    }

    /**
     * Returns the value of the {@code notes failure} field.
     *
     * @return {@code notes failure} field.
     */
    public final String getNotesFailure() {

        return this.notesFailure;
    }

    /**
     * Sets the value of the {@code notes failure} field.
     *
     * @param notesFailureIn
     *            field to set.
     */
    public final void setNotesFailure(final String notesFailureIn) {

        this.notesFailure = notesFailureIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.key == null) ? 0 : this.key.hashCode());
        result = (prime * result) + ((this.testCaseId == null) ? 0 : this.testCaseId.hashCode());
        result = (prime * result) + ((this.url == null) ? 0 : this.url.hashCode());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final AbstractTestLinkBean other = (AbstractTestLinkBean) obj;

        if (this.key == null) {

            if (other.key != null) {
                return false;
            }
        } else if (!this.key.equals(other.key)) {
            return false;
        }

        if (this.testCaseId == null) {

            if (other.testCaseId != null) {
                return false;
            }
        } else if (!this.testCaseId.equals(other.testCaseId)) {
            return false;
        }

        if (this.url == null) {

            if (other.url != null) {
                return false;
            }
        } else if (!this.url.equals(other.url)) {
            return false;
        }

        return true;
    }

}
