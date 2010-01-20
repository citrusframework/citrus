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

package com.consol.citrus;

/**
 * Class defining some constants used in the test suite
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class CitrusConstants {
    /** Prefix/sufix used to identify variable names */
    public static final String VARIABLE_PREFIX = "${";
    public static final char VARIABLE_SUFFIX = '}';

    /** Search wildcard */
    public static final String SEARCH_WILDCARD = "*";

    public static final String DEFAULT_APPLICATIONCONTEXT = "citrus-context.xml";

    public static final String DEFAULT_JAVA_DIRECTORY = "src/citrus/java/";
    public static final String DEFAULT_TEST_DIRECTORY = "src/citrus/tests/";
    
    public static final String DEFAULT_SUITE_NAME = "citrus-default-testsuite";
    public static final String DEFAULT_TEST_NAME = "citrus-test";
}
