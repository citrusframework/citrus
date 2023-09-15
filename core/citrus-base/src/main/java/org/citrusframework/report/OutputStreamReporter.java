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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class OutputStreamReporter extends LoggingReporter {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(OutputStreamReporter.class);

    /** Buffered writer to write logging events to */
    private Writer logWriter;

    /** Line format */
    private String format = "%s\t| %s%n";

    /**
     * Count down for failed output stream write operations.
     */
    private CountDownLatch failedCounter = new CountDownLatch(5);

    /**
     * Default constructor using output stream.
     * @param logStream
     */
    public OutputStreamReporter(OutputStream logStream) {
        this(new BufferedWriter(new OutputStreamWriter(logStream)));
    }

    /**
     * Constructor using writer instances.
     * @param logWriter
     */
    public OutputStreamReporter(Writer logWriter) {
        this.logWriter = logWriter;
    }

    @Override
    protected void info(String line) {
        writeSafely("INFO", line);
    }

    @Override
    protected void debug(String line) {
        if (isDebugEnabled()) {
            writeSafely("DEBUG", line);
        }
    }

    @Override
    protected void error(String line, Throwable cause) {
        writeSafely("ERROR", line + ": " +  cause.getMessage());
    }

    @Override
    protected boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * @param level
     * @param line
     */
    private synchronized void writeSafely(String level, String line) {
        if (logWriter != null && failedCounter.getCount() > 0) {
            try {
                logWriter.write(String.format(format, level ,line));
            } catch (IOException e) {
                failedCounter.countDown();
                logger.warn("Failed to write logging event to output stream", e);
            }
        }
    }

    /**
     * Gets the format.
     *
     * @return
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format.
     *
     * @param format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the logger writer.
     *
     * @return
     */
    public Writer getLogWriter() {
        return logWriter;
    }

    /**
     * Sets the logger writer.
     * @param writer
     */
    protected void setLogWriter(Writer writer) {
        this.logWriter = writer;
    }
}
