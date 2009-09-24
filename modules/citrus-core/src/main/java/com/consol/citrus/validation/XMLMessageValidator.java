/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.validation;

import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;

public interface XMLMessageValidator extends MessageValidator {
    public boolean validateXMLSchema(Message<?> receivedMessage);

    public boolean validateDTD(Resource dtdResource, Message<?> receivedMessage);

    public boolean validateNamespaces(Map<String, String> expectedNamespaces, Message<?> receivedMessage);
    
    public boolean validateMessageElements(Map<String, String> validateElements, Message<?> receivedMessage, NamespaceContext nsContext, TestContext context);
}
