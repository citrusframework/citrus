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

package org.citrusframework.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Special exception thrown in case several actions in a parallel container have failed.
 * The exception receives a list of exceptions and provides detailed to string method for 
 * overview of failed exceptions.
 * 
 * @author Christoph Deppisch
 */
public class ParallelContainerException extends CitrusRuntimeException {

    private static final long serialVersionUID = 1L;

    private List<CitrusRuntimeException> exceptions = new ArrayList<CitrusRuntimeException>();
    
    public ParallelContainerException(List<CitrusRuntimeException> nestedExceptions) {
        super();
        
        this.exceptions = nestedExceptions;
    }
    
    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Several actions failed in parallel container");
        for (CitrusRuntimeException exception : exceptions) {
            builder.append("\n\t+ " + exception.getClass().getName() + ": " + exception.getLocalizedMessage());
        }
        
        return builder.toString();
    }
}
