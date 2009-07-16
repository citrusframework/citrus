package com.consol.citrus;

import java.util.Date;

public class TestCaseMetaInfo {
    /** Author of testcase */
    private String author = "";

    /** Creation date of testcase */
    private Date creationDate;

    /** Status of testcase */
    private String status = "";

    /** Last updated by */
    private String lastUpdatedBy = "";

    /** Last updated on */
    private Date lastUpdatedOn;

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the lastUpdatedBy
     */
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    /**
     * @return the lastUpdatedOn
     */
    public Date getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    /**
     * @param lastUpdatedOn the lastUpdatedOn to set
     */
    public void setLastUpdatedOn(Date lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }


}
