/*
 * Copyright the original author or authors.
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
    private final CountDownLatch failedCounter = new CountDownLatch(5);

    /**
     * Default constructor using output stream.
     * @param logStream
     */
    public OutputStreamReporter(OutputStream logStream) {
        this(new BufferedWriter(new OutputStreamWriter(logStream)));
    }

    /**
     * Constructor using writer instances.
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
     * Write safely to the output stream.
     * Catches IO errors and increases failed counter to avoid endless error chains.
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
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the logger writer.
     */
    public Writer getLogWriter() {
        return logWriter;
    }

    /**
     * Sets the logger writer.
     */
    protected void setLogWriter(Writer writer) {
        this.logWriter = writer;
    }
}
