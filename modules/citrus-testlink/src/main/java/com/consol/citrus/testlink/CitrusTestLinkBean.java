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
 * last modified: Saturday, January 21, 2012 (20:54) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.TestCase;

/**
 * Bean holding all values for CITRUS to TestLink handling.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public final class CitrusTestLinkBean extends AbstractTestLinkBean {

    // ~ Instance fields -------------------------------------------------------------------------

    /** startTime. */
    private long startTime;

    /** endTime. */
    private long endTime;

    /** valid. */
    private boolean valid;

    /** success. */
    private Boolean success;

    /** responseState. */
    private Boolean responseState;

    /** id. */
    private String id;

    /** response. */
    private final List<String> responseList;

    /** citrusTestCase. */
    private TestCase citrusTestCase;

    /** cause. */
    private Throwable cause;

    /** responseCause. */
    private Throwable responseCause;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusTestLinkBean} class.
     */
    public CitrusTestLinkBean() {

        super();

        this.valid = true;
        this.responseList = new ArrayList<String>();
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code valid} field.
     *
     * @return {@code valid} field.
     */
    public boolean isValid() {

        return this.valid;
    }

    /**
     * Only allows to set this field to be {@code false}. As long as there is no reason to set this
     * field to {@code false}, this bean only holds valid entries and is assumed to be valid.
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
     * Returns the value of the {@code citrus test case} field.
     *
     * @return {@code citrus test case} field.
     */
    public TestCase getCitrusTestCase() {

        return this.citrusTestCase;
    }

    /**
     * Sets the value of the {@code citrus test case} field.
     *
     * @param citrusTestCaseIn
     *            field to set.
     */
    public void setCitrusTestCase(final TestCase citrusTestCaseIn) {

        this.citrusTestCase = citrusTestCaseIn;
    }

    /**
     * Returns the value of the {@code response} field.
     *
     * @return {@code response} field.
     */
    public List<String> getResponseList() {

        return this.responseList;
    }

    /**
     * Sets the value of the {@code response} field.
     *
     * @param responseIn
     *            field to set.
     */
    public void addResponse(final String responseIn) {

        if ((null != responseIn) && (!responseIn.isEmpty())) {

            this.responseList.add(responseIn);
        }
    }

    /**
     * Returns the value of the {@code response state} field.
     *
     * @return {@code response state} field.
     */
    public Boolean getResponseState() {

        return this.responseState;
    }

    /**
     * Sets the value of the {@code response state} field.
     *
     * @param responseStateIn
     *            field to set.
     */
    public void setResponseState(final Boolean responseStateIn) {

        this.responseState = responseStateIn;
    }

    /**
     * Returns the value of the {@code response cause} field.
     *
     * @return {@code response cause} field.
     */
    public Throwable getResponseCause() {

        return this.responseCause;
    }

    /**
     * Sets the value of the {@code response cause} field.
     *
     * @param responseCauseIn
     *            field to set.
     */
    public void setResponseCause(final Throwable responseCauseIn) {

        this.responseCause = responseCauseIn;
    }

    /**
     * Returns the value of the {@code cause} field.
     *
     * @return {@code cause} field.
     */
    public Throwable getCause() {

        return this.cause;
    }

    /**
     * Sets the value of the {@code cause} field.
     *
     * @param causeIn
     *            field to set.
     */
    public void setCause(final Throwable causeIn) {

        this.cause = causeIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
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

        if (!super.equals(obj)) {
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
        builder.append(", valid=");
        builder.append(this.valid);
        builder.append(", success=");
        builder.append(this.success);
        builder.append(", responseState=");
        builder.append(this.responseState);
        builder.append(", id=");
        builder.append(this.id);
        builder.append(", responseList=");
        builder.append(this.responseList);
        builder.append(", citrusTestCase=");
        builder.append(this.citrusTestCase);
        builder.append(", cause=");
        builder.append(this.cause);
        builder.append(", responseCause=");
        builder.append(this.responseCause);
        builder.append(", isValid()=");
        builder.append(this.isValid());
        builder.append(", getSuccess()=");
        builder.append(this.getSuccess());
        builder.append(", getStartTime()=");
        builder.append(this.getStartTime());
        builder.append(", getEndTime()=");
        builder.append(this.getEndTime());
        builder.append(", getId()=");
        builder.append(this.getId());
        builder.append(", getCitrusTestCase()=");
        builder.append(this.getCitrusTestCase());
        builder.append(", getResponseList()=");
        builder.append(this.getResponseList());
        builder.append(", getResponseState()=");
        builder.append(this.getResponseState());
        builder.append(", getResponseCause()=");
        builder.append(this.getResponseCause());
        builder.append(", getCause()=");
        builder.append(this.getCause());
        builder.append("]");

        return builder.toString();
    }

}
