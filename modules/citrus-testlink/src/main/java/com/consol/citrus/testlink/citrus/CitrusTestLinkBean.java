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
 * last modified: Saturday, January 14, 2012 (13:28) by: Matthias Beil
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

    /** success. */
    private Boolean success;

    /** packageName. */
    private String packageName;

    /** citrusCase. */
    private TestCase citrusCase;

    /** tlkBean. */
    private TestLinkBean tlkBean;

    /** failureCause. */
    private Throwable failureCause;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusTestLinkBean} class.
     */
    public CitrusTestLinkBean() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

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
    public String getPackageName() {

        return this.packageName;
    }

    /**
     * Sets the value of the {@code package name} field.
     *
     * @param packageNameIn
     *            field to set.
     */
    public void setPackageName(final String packageNameIn) {

        this.packageName = packageNameIn;
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
     * Returns the value of the {@code failure cause} field.
     *
     * @return {@code failure cause} field.
     */
    public Throwable getFailureCause() {

        return this.failureCause;
    }

    /**
     * Sets the value of the {@code failure cause} field.
     *
     * @param failureCauseIn
     *            field to set.
     */
    public void setFailureCause(final Throwable failureCauseIn) {

        this.failureCause = failureCauseIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.packageName == null) ? 0 : this.packageName.hashCode());

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

        if (this.packageName == null) {

            if (other.packageName != null) {
                return false;
            }
        } else if (!this.packageName.equals(other.packageName)) {
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
        builder.append(", packageName=");
        builder.append(this.packageName);
        builder.append(", citrusCase=");
        builder.append(this.citrusCase);
        builder.append(", tlkBean=");
        builder.append(this.tlkBean);
        builder.append(", failureCause=");
        builder.append(this.failureCause);
        builder.append("]");

        return builder.toString();
    }

}
