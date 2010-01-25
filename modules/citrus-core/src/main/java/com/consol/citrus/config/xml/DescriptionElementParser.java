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

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for description elements of test actions.
 * 
 * @author Christoph Deppisch
 */
public class DescriptionElementParser {
    /**
     * Static parse method taking care of test action description.
     * @param element
     * @param builder
     */
    public static void doParse(Element element, BeanDefinitionBuilder builder) {
        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            builder.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }
    }
}
