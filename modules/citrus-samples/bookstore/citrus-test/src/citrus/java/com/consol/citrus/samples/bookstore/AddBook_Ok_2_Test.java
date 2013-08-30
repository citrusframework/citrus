/*
 * Copyright 2006-2012 the original author or authors.
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

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.samples.bookstore.model.*;
import com.consol.citrus.validation.MarshallingValidationCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.MessageHeaders;
import org.springframework.oxm.Marshaller;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;

/**
 * @author Christoph Deppisch
 */
@Test
public class AddBook_Ok_2_Test extends TestNGCitrusTestBuilder {

    @Autowired
    @Qualifier("bookStoreRequestMessageSender")
    private MessageSender bookRequestMessageSender;
    
    @Autowired
    @Qualifier("bookStoreResponseMessageHandler")
    private MessageReceiver bookResponseMessageHandler;
    
    @Autowired
    private Marshaller marshaller;

    @CitrusTest
    public void AddBook_Ok_2_Test() {
        String isbn = "978-citrus:randomNumber(10)";
        
        send(bookRequestMessageSender)
            .payload(createAddBookRequestMessage(isbn), marshaller)
            .header("citrus_soap_action", "addBook");
        
        receive(bookResponseMessageHandler)
            .validationCallback(new MarshallingValidationCallback<AddBookResponseMessage>() {
                @Override
                public void validate(AddBookResponseMessage response, MessageHeaders headers) {
                    Assert.isTrue(response.isSuccess());
                }
            });
    }
    
    /**
     * @param isbn
     * @return
     */
    private AddBookRequestMessage createAddBookRequestMessage(String isbn) {
        AddBookRequestMessage requestMessage = new AddBookRequestMessage();
        Book book = new Book();
        book.setAuthor("Mike Loukides, Sonatype");
        book.setTitle("Maven: The Definitive Guide");
        book.setIsbn(isbn);
        book.setYear(2008);
        book.setRegistrationDate(Calendar.getInstance());
        requestMessage.setBook(book);
        return requestMessage;
    }

}
