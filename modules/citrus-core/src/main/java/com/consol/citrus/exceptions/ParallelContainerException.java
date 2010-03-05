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

package com.consol.citrus.exceptions;

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
