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

package com.consol.citrus.samples.bookregistry;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.samples.bookregistry.exceptions.DuplicateIsbnException;
import com.consol.citrus.samples.bookregistry.exceptions.UnknownBookException;
import com.consol.citrus.samples.bookregistry.model.*;

/**
 * @author Christoph Deppisch
 */
public class BookRegistry {

    /** In memory book registry */
    private static Map<String, Book> bookRegistry = new HashMap<String, Book>();
    
    /** Atomic identifyer generator */
    private static AtomicInteger ids = new AtomicInteger();
    
    /**
     * Adds a book to the registry.
     * @param request
     * @return
     */
    public Message<AddBookResponseMessage> addBook(Message<AddBookRequestMessage> request) {
        AddBookResponseMessage response = new AddBookResponseMessage();
        
        Book book = request.getPayload().getBook();
        
        if(!bookRegistry.containsKey(book.getIsbn())) {
            book.setId(ids.incrementAndGet());
            book.setRegistrationDate(new Date());
            bookRegistry.put(book.getIsbn(), book);
            
            response.setSuccess(true);
        } else {
            throw new DuplicateIsbnException(request);
        }
        
        return MessageBuilder.withPayload(response).build();
    }
    
    /**
     * Get the book details for a book with given isbn.
     * @param request
     * @return
     */
    public Message<GetBookDetailsResponseMessage> getBookDetails(Message<GetBookDetailsRequestMessage> request) {
        GetBookDetailsResponseMessage response = new GetBookDetailsResponseMessage();
        
        Book book = bookRegistry.get(request.getPayload().getIsbn());
        
        if(book == null) {
            throw new UnknownBookException(request);
        } else {
            response.setBook(book);
        }
        
        return MessageBuilder.withPayload(response).build();
    }
    
    /**
     * List all books in this registry.
     * @return
     */
    public Message<ListBooksResponseMessage> listBooks() {
        ListBooksResponseMessage response = new ListBooksResponseMessage();
        response.setBooks(new ArrayList<Book>(bookRegistry.values()));
        
        return MessageBuilder.withPayload(response).build();
    }
}
