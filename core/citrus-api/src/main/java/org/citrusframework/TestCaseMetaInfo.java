/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework;

import java.util.Date;

/**
 * Test case meta information.
 *
 * @author Christoph Deppisch
 */
public class TestCaseMetaInfo {
    /** Author of testcase */
    private String author = "";

    /** Creation date of testcase */
    private Date creationDate;

    /** Status of testcase */
    public enum Status {DRAFT, READY_FOR_REVIEW, FINAL, DISABLED}

    private Status status = Status.DRAFT;

    /** Last updated by */
    private String lastUpdatedBy = "";

    /** Last updated on */
    private Date lastUpdatedOn;

    /**
     * Get the test author.
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the test author.
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Get the creation date.
     * @return the creationDate
     */
    public Date getCreationDate() {
        if (creationDate != null) {
            return new Date(creationDate.getTime());
        } else {
            return null;
        }
    }

    /**
     * Set the creation date.
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        if (creationDate == null) {
            return;
        }

        this.creationDate = new Date(creationDate.getTime());
    }

    /**
     * Get the author that recently updated this test case.
     * @return the lastUpdatedBy
     */
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * Set the author that recently updated this test case.
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    /**
     * Get last updating date.
     * @return the lastUpdatedOn
     */
    public Date getLastUpdatedOn() {
        if (lastUpdatedOn != null) {
            return new Date(lastUpdatedOn.getTime());
        } else {
            return null;
        }
    }

    /**
     * Set last updating date.
     * @param lastUpdatedOn the lastUpdatedOn to set
     */
    public void setLastUpdatedOn(Date lastUpdatedOn) {
        this.lastUpdatedOn = new Date(lastUpdatedOn.getTime());
    }

    /**
     * Set the status the this test case.
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Get the status of this test case.
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

}
