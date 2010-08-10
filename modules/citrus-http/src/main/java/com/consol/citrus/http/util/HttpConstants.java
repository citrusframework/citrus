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

package com.consol.citrus.http.util;

/**
 * Constant values used for Http communication.
 * 
 * @author Christoph Deppisch 2007
 */
public class HttpConstants {
    /** Http version */
    public static final String HTTP_VERSION = "HTTP/1.1";

    /** Basic line break characters */
    public static final String LINE_BREAK = "\r\n";

    /** Http status */
    public static final String HTTP_STATUS_OK = "OK";

    /** Http status codes */
    public static final String HTTP_CODE_200 = "200";
    public static final String HTTP_CODE_500 = "500";
    public static final String HTTP_CODE_404 = "404";

    /** Http request methods */
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";

    /** Local host */
    public static final String LOCAL_HOST = "localhost";

    /** Default Http port */
    public static final int LOCAL_PORT = 8080;
}
