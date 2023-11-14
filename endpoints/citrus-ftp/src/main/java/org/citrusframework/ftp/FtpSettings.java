/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.ftp;

import org.citrusframework.message.MessageType;

/**
 * @author Christoph Deppisch
 */
public class FtpSettings {

    private static final String FTP_PROPERTY_PREFIX = "citrus.ftp.";
    private static final String FTP_ENV_PREFIX = "CITRUS_FTP";

    private static final String MARSHALLER_TYPE_PROPERTY = FTP_PROPERTY_PREFIX + "marshaller.type";
    private static final String MARSHALLER_TYPE_ENV = FTP_ENV_PREFIX + "MARSHALLER_TYPE";
    private static final String MARSHALLER_TYPE_DEFAULT = MessageType.XML.name();

    private FtpSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Gets the default marshaller type setting that represents the data format
     * that the marshaller will use (usually one of XML or JSON).
     * @return
     */
    public static String getMarshallerType() {
        return System.getProperty(MARSHALLER_TYPE_PROPERTY,
                System.getenv(MARSHALLER_TYPE_ENV) != null ? System.getenv(MARSHALLER_TYPE_ENV) : MARSHALLER_TYPE_DEFAULT);
    }

}
