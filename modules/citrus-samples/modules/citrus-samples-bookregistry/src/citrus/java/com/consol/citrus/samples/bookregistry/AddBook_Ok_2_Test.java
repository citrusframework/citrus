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

package com.consol.citrus.samples.bookregistry;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.MessageHeaders;
import org.springframework.oxm.Marshaller;
import org.springframework.util.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.samples.CitrusSamplesDemo;
import com.consol.citrus.samples.bookregistry.model.*;
import com.consol.citrus.samples.common.DemoAwareTestNGCitrusTestBuilder;
import com.consol.citrus.validation.MarshallingValidationCallback;

/**
 * @author Christoph Deppisch
 */
public class AddBook_Ok_2_Test extends DemoAwareTestNGCitrusTestBuilder {

    private BookRegistryDemo demo = new BookRegistryDemo();
    
    @Autowired
    @Qualifier("bookRegistryRequestMessageSender")
    private MessageSender bookRequestMessageSender;
    
    @Autowired
    @Qualifier("bookRegistryResponseMessageHandler")
    private MessageReceiver bookResponseMessageHandler;
    
    @Autowired
    private Marshaller marshaller;
    
    @Test
    public void runTest(ITestContext testContext) {
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
        
        executeTest(testContext);
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
    
    /**
     * Gets the demo application.
     * @return
     */
    public CitrusSamplesDemo getDemo() {
        return demo;
    }

}
