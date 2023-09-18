/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractOutputFileReporter extends AbstractTestReporter {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractOutputFileReporter.class);

    @Override
    public final void generate(TestResults testResults) {
        if (!isEnabled()) {
            return;
        }

        createReportFile(getReportFileName(), getReportContent(testResults));
    }

    protected abstract boolean isEnabled();

    protected abstract String getReportContent(TestResults testResults);

    protected abstract String getReportFileName();

    /**
     * Creates the HTML report file
     * @param reportFileName The report file to write
     * @param content The String content of the report file
     */
    private void createReportFile(String reportFileName, String content) {
        File targetDirectory = new File(getReportDirectory());
        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                throw new CitrusRuntimeException("Unable to create report output directory: " + getReportDirectory());
            }
        }

        try (Writer fileWriter = new FileWriter(new File(targetDirectory, reportFileName))) {
            fileWriter.append(content);
            fileWriter.flush();
            logger.info("Generated test report: " + targetDirectory + File.separator + reportFileName);
        } catch (IOException e) {
            logger.error("Failed to create test report", e);
        }
    }
}
