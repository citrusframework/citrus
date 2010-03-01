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

package com.consol.citrus.samples.bookregistry.model;

import java.util.Date;

/**
 * @author Christoph Deppisch
 */
public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private int year;
    private Date registrationDate;
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
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
     * @return the isbn
     */
    public String getIsbn() {
        return isbn;
    }
    
    /**
     * @param isbn the isbn to set
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }
    
    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }
    
    /**
     * @return the registrationDate
     */
    public Date getRegistrationDate() {
        return registrationDate;
    }
    
    /**
     * @param registrationDate the registrationDate to set
     */
    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
}
