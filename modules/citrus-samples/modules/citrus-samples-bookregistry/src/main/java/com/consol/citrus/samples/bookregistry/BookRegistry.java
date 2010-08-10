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
