/*
 * File: CitrusTestLinkFileHandler.java
 *
 * Copyright (c) 2006-2012 the original author or authors.
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
 *
 * last modified: Friday, May 18, 2012 (17:56) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.List;

/**
 * Interface defining functionality to write a CITRUS test result to a file.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public interface CitrusTestLinkFileHandler {

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Read from the given directory all files and convert them to a CITRUS test bean.
     *
     * <p>
     * Return a list holding the CITRUS test bean and the corresponding file. This is to allow to
     * delete the file, if the test case could be written without any error to TestLink. This avoids
     * to write the same test case twice.
     * </p>
     *
     * @param directory
     *            Directory from where to read all CITRUS test files.
     *
     * @return List of CITRUS test beans. Only for those files which have valid JSON information
     *         stored and conform to the correct file name schema. List will never be {@code null}.
     */
    List<CitrusTestLinkFileBean> readFromDirectory(final String directory);

    /**
     * Write CITRUS test case result into a file of the given directory.
     *
     * @param bean
     *            Holding CITRUS test case result.
     * @param directory
     *            Directory where to write the result too.
     */
    void writeToFile(final CitrusTestLinkBean bean, final String directory);

}
