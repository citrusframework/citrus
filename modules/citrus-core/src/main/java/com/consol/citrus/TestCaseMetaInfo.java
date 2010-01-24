/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus;

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
    public static enum Status {DRAFT, READY_FOR_REVIEW, FINAL, DISABLED};
    
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
        return creationDate;
    }

    /**
     * Set the creation date.
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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
        return lastUpdatedOn;
    }

    /**
     * Set last updating date.
     * @param lastUpdatedOn the lastUpdatedOn to set
     */
    public void setLastUpdatedOn(Date lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
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
