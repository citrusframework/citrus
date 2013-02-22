/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.samples.flightbooking.entity.converter;

import java.text.ParseException;

import com.consol.citrus.samples.flightbooking.entity.CustomerEntity;
import com.consol.citrus.samples.flightbooking.model.Customer;

/**
 * Converter takes care on model to entity conversion and vice versa.
 * @author Christoph Deppisch
 */
public class CustomerConverter {

    /**
     * Prevent instantiation.
     */
    private CustomerConverter() {
    }
    
    /**
     * Get model form entity.
     * @param entity
     * @return
     * @throws ParseException 
     */
    public static Customer from(CustomerEntity entity) {
        if (entity == null) { 
            return null;
        }
        
        Customer model = new Customer();

        model.setId(entity.getId());
        model.setFirstname(entity.getFirstname());
        model.setLastname(entity.getLastname());
        
        return model;
    }
    
    /**
     * Get entity form model.
     * @param entity
     * @return
     */
    public static CustomerEntity from(Customer model) {
        CustomerEntity entity = new CustomerEntity();

        entity.setId(model.getId());
        entity.setFirstname(model.getFirstname());
        entity.setLastname(model.getLastname());
        
        return entity;
    }
}
