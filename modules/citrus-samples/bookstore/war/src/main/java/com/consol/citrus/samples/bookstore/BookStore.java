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

package com.consol.citrus.samples.bookstore;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

import com.consol.citrus.samples.bookstore.model.*;
import com.consol.citrus.samples.bookstore.model.ListBooksResponseMessage.Books;
import com.consol.citrus.samples.bookstore.exceptions.DuplicateIsbnException;
import com.consol.citrus.samples.bookstore.exceptions.UnknownBookException;

/**
 * @author Christoph Deppisch
 */
public class BookStore {

    /** In memory book registry */
    private static Map<String, Book> bookStore = new HashMap<String, Book>();
    
    /** Atomic identifyer generator */
    private static AtomicLong ids = new AtomicLong();
    
    /**
     * Adds a book to the registry.
     * @param request
     * @return
     */
    public Message<AddBookResponseMessage> addBook(Message<AddBookRequestMessage> request) {
        AddBookResponseMessage response = new AddBookResponseMessage();
        
        Book book = request.getPayload().getBook();
        
        if (!bookStore.containsKey(book.getIsbn())) {
            book.setId(ids.incrementAndGet());
            book.setRegistrationDate(Calendar.getInstance());
            bookStore.put(book.getIsbn(), book);
            
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
        
        Book book = bookStore.get(request.getPayload().getIsbn());
        
       if (book == null) {
            throw new UnknownBookException(request, request.getPayload().getIsbn());
        } else {
            response.setBook(book);
        }
        
        return MessageBuilder.withPayload(response).build();
    }
    
    /**
     * Get the book cover for a book with given isbn.
     * @param request
     * @return
     */
    public Message<GetBookAbstractResponseMessage> getBookAbstract(Message<GetBookAbstractRequestMessage> request) {
        GetBookAbstractResponseMessage response = new GetBookAbstractResponseMessage();
        
        Book book = bookStore.get(request.getPayload().getIsbn());
        
      if (book == null) {
            throw new UnknownBookException(request, request.getPayload().getIsbn());
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
        Books books = new Books();
        books.getBooks().addAll(bookStore.values());
        response.setBooks(books);
        
        return MessageBuilder.withPayload(response).build();
    }
}
