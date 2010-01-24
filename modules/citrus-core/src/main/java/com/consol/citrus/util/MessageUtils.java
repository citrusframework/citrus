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

package com.consol.citrus.util;

import org.springframework.integration.core.MessageHeaders;

/**
 * Message utility class.
 * 
 * @author Christoph Deppisch
 */
public class MessageUtils {
    /**
     * Check if given header is Spring integration internal header.
     * @param key
     * @return
     */
    public static boolean isSpringIntegrationHeaderEntry(String key) {
        return key.startsWith(MessageHeaders.PREFIX);
    }
}
