/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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

package com.consol.citrus.http.util;

/**
 * Constant values used for Http communication
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class HttpConstants {
    public static final String HTTP_VERSION = "HTTP/1.1";

    public static final String LINE_BREAK = "\r\n";

    public static final String HTTP_STATUS_OK = "OK";

    public static final String HTTP_CODE_200 = "200";
    public static final String HTTP_CODE_500 = "500";
    public static final String HTTP_CODE_404 = "404";

    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";

    public static final String LOCAL_HOST = "localhost";

    public static final int LOCAL_PORT = 8080;
}
