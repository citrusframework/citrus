/*
 * File: CitrusTestLinkBean.java
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
 * last modified: Saturday, January 14, 2012 (20:58) by: Matthias Beil
 */
package com.consol.citrus.testlink.citrus;

import com.consol.citrus.TestCase;
import com.consol.citrus.testlink.TestLinkBean;

/**
 * Bean for handling CITRUS and TestLink exchange.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class CitrusTestLinkBean {

    // ~ Instance fields -----------------------------------------------------------------------------------------------

    /** startTime. */
    private long startTime;

    /** endTime. */
    private long endTime;

    /** valid. */
    private boolean valid;

    /** testLinkVariables. */
    private boolean testLinkVariables;

    /** success. */
    private Boolean success;

    /** id. */
    private String id;

    /** url. */
    private String url;

    /** key. */
    private String key;

    /** platform. */
    private String platform;

    /** notes. */
    private String notes;

    /** citrusCase. */
    private TestCase citrusCase;

    /** tlkBean. */
    private TestLinkBean tlkBean;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusTestLinkBean} class.
     */
    public CitrusTestLinkBean() {

        super();

        // assume bean is valid
        this.valid = true;
        this.testLinkVariables = false;
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code valid} field.
     *
     * @return {@code valid} field.
     */
    public boolean isValid() {

        return this.valid;
    }

    /**
     * Only allows to set this field to be {@code false}. As long as there is no reason to set this field to
     * {@code false}, this bean only holds valid entries and is assumed to be valid.
     *
     * @param validIn
     *            Set valid to {@code false}.
     */
    public void setValid(final boolean validIn) {

        if (!validIn) {

            this.valid = validIn;
        }
    }

    /**
     * Returns the value of the {@code test link variables} field.
     *
     * @return {@code test link variables} field.
     */
    public boolean hasTestLinkVariables() {

        return this.testLinkVariables;
    }

    /**
     * Sets the value of the {@code test link variables} field.
     *
     * @param testLinkVariablesIn
     *            field to set.
     */
    public void setTestLinkVariables(final boolean testLinkVariablesIn) {

        this.testLinkVariables = testLinkVariablesIn;
    }

    /**
     * Returns the value of the {@code success} field.
     *
     * @return {@code success} field.
     */
    public Boolean getSuccess() {

        return this.success;
    }

    /**
     * Sets the value of the {@code success} field.
     *
     * @param successIn
     *            field to set.
     */
    public void setSuccess(final Boolean successIn) {

        this.success = successIn;
    }

    /**
     * Returns the value of the {@code start time} field.
     *
     * @return {@code start time} field.
     */
    public long getStartTime() {

        return this.startTime;
    }

    /**
     * Sets the value of the {@code start time} field.
     *
     * @param startTimeIn
     *            field to set.
     */
    public void setStartTime(final long startTimeIn) {

        this.startTime = startTimeIn;
    }

    /**
     * Returns the value of the {@code end time} field.
     *
     * @return {@code end time} field.
     */
    public long getEndTime() {

        return this.endTime;
    }

    /**
     * Sets the value of the {@code end time} field.
     *
     * @param endTimeIn
     *            field to set.
     */
    public void setEndTime(final long endTimeIn) {

        this.endTime = endTimeIn;
    }

    /**
     * Returns the value of the {@code package name} field.
     *
     * @return {@code package name} field.
     */
    public String getId() {

        return this.id;
    }

    /**
     * Sets the value of the {@code id} field.
     *
     * @param idIn
     *            field to set.
     */
    public void setId(final String idIn) {

        this.id = idIn;
    }

    /**
     * Returns the value of the {@code url} field.
     *
     * @return {@code url} field.
     */
    public String getUrl() {

        return this.url;
    }

    /**
     * Sets the value of the {@code url} field.
     *
     * @param urlIn
     *            field to set.
     */
    public void setUrl(final String urlIn) {

        this.url = urlIn;
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
     * Sets the value of the {@code key} field.
     *
     * @param keyIn
     *            field to set.
     */
    public void setKey(final String keyIn) {

        this.key = keyIn;
    }

    /**
     * Returns the value of the {@code platform} field.
     *
     * @return {@code platform} field.
     */
    public String getPlatform() {

        return this.platform;
    }

    /**
     * Sets the value of the {@code platform} field.
     *
     * @param platformIn
     *            field to set.
     */
    public void setPlatform(final String platformIn) {

        this.platform = platformIn;
    }

    /**
     * Returns the value of the {@code citrus case} field.
     *
     * @return {@code citrus case} field.
     */
    public TestCase getCitrusCase() {

        return this.citrusCase;
    }

    /**
     * Sets the value of the {@code citrus case} field.
     *
     * @param citrusCaseIn
     *            field to set.
     */
    public void setCitrusCase(final TestCase citrusCaseIn) {

        this.citrusCase = citrusCaseIn;
    }

    /**
     * Returns the value of the {@code tlk bean} field.
     *
     * @return {@code tlk bean} field.
     */
    public TestLinkBean getTlkBean() {

        return this.tlkBean;
    }

    /**
     * Sets the value of the {@code tlk bean} field.
     *
     * @param tlkBeanIn
     *            field to set.
     */
    public void setTlkBean(final TestLinkBean tlkBeanIn) {

        this.tlkBean = tlkBeanIn;
    }

    /**
     * Returns the value of the {@code notes} field.
     *
     * @return {@code notes} field.
     */
    public String getNotes() {

        return this.notes;
    }

    /**
     * Sets the value of the {@code notes} field.
     *
     * @param notesIn
     *            field to set.
     */
    public void setNotes(final String notesIn) {

        this.notes = notesIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());

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

        final CitrusTestLinkBean other = (CitrusTestLinkBean) obj;

        if (this.id == null) {

            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("CitrusTestLinkBean [startTime=");
        builder.append(this.startTime);
        builder.append(", endTime=");
        builder.append(this.endTime);
        builder.append(", success=");
        builder.append(this.success);
        builder.append(", id=");
        builder.append(this.id);
        builder.append(", url=");
        builder.append(this.url);
        builder.append(", key=");
        builder.append(this.key);
        builder.append(", platform=");
        builder.append(this.platform);
        builder.append(", citrusCase=");
        builder.append(this.citrusCase);
        builder.append(", tlkBean=");
        builder.append(this.tlkBean);
        builder.append(", notes=");
        builder.append(this.notes);
        builder.append("]");

        return builder.toString();
    }

}
