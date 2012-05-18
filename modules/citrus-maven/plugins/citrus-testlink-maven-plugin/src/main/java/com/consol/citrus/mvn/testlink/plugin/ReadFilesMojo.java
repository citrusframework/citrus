/*
 * File: ReadFilesMojo.java
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
 * last modified: Friday, May 18, 2012 (18:54) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.consol.citrus.testlink.CitrusTestLinkBean;
import com.consol.citrus.testlink.CitrusTestLinkFileBean;
import com.consol.citrus.testlink.CitrusTestLinkFileHandler;
import com.consol.citrus.testlink.CitrusTestLinkHandler;
import com.consol.citrus.testlink.citrus.CitrusTestLinkFileHandlerImpl;
import com.consol.citrus.testlink.citrus.CitrusTestLinkHandlerImpl;
import com.consol.citrus.testlink.utils.ConvertUtils;
import com.consol.citrus.testlink.utils.FileUtils;

/**
 * Read from a directory all CITRUS test case files as JSON files and write them to TestLink.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 * @goal read
 */
public class ReadFilesMojo extends AbstractMojo {

    // ~ Instance fields -------------------------------------------------------------------------

    /**
     * URL pointing to the TestLink URL. The path to the XML-RPC call will be append.
     * 
     * @parameter
     * @required
     */
    protected String url;

    /**
     * Development needed for authorization to TestLink. This must be generated within TestLink. For
     * this TestLink must be configured.
     * 
     * @parameter
     * @required
     */
    protected String devKey;

    /**
     * Directory from where to read all CITRUS test case result files.
     * 
     * @parameter
     * @required
     */
    protected String directory;

    /** handler. */
    private final CitrusTestLinkHandler handler;

    /** fileHandler. */
    private final CitrusTestLinkFileHandler fileHandler;

    // ~ Constructors ----------------------------------------------------------------------------

    /**
     * Constructor for {@code ReadFilesMojo} class.
     */
    public ReadFilesMojo() {

        this(new CitrusTestLinkHandlerImpl(), new CitrusTestLinkFileHandlerImpl());
    }

    /**
     * Constructor for {@code ReadFilesMojo} class. Constructor to be used for JUnit test classes.
     * 
     * @param handlerIn
     *            CITRUS handler for writing to TestLink.
     * @param fileHandlerIn
     *            CITRUS file handler.
     */
    protected ReadFilesMojo(final CitrusTestLinkHandler handlerIn,
            final CitrusTestLinkFileHandler fileHandlerIn) {

        super();

        this.handler = handlerIn;
        this.fileHandler = fileHandlerIn;
    }

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {

            final List<CitrusTestLinkFileBean> beanList = this.fileHandler
                    .readFromDirectory(this.directory);

            if ((null != beanList) && (!beanList.isEmpty())) {

                for (final CitrusTestLinkFileBean beanFile : beanList) {

                    final CitrusTestLinkBean bean = beanFile.getBean();

                    bean.setUrl(this.url);
                    bean.setKey(this.devKey);

                    this.handler.writeToTestLink(bean);

                    this.handleResponse(bean, beanFile.getFile());
                }
            } else {

                this.getLog().error(
                        "No CITRUS test cases found in directory [ " + this.directory + " ]");
            }
        } catch (final Exception ex) {

            this.getLog().error("Exception caught!", ex);
        }
    }

    /**
     * Depending on the response values build response and log the result.
     * 
     * @param bean
     *            CITRUS TestLink bean holding response.
     * @param file
     *            DOCUMENT ME!
     */
    private void handleResponse(final CitrusTestLinkBean bean, final File file) {

        // there was some error writing to TestLink, log it
        final StringBuilder builder = new StringBuilder();

        // check if there was some writing to TestLink
        if (null != bean.getResponseState()) {

            // check if writing to TestLink was successful
            if (bean.getResponseState().booleanValue()) {

                // YEAH it was
                this.getLog().info(
                        "+++===+++ Writing to TestLink was successful for [ " + bean.getId()
                                + " ] +++===+++");

                if (!FileUtils.delete(file)) {

                    this.getLog().error("Could not delete file [ " + file + " ]");
                }

                // done with response successful
                return;
            }
        }

        builder.append("\n+++===+++\n");
        builder.append("Failure writing to TestLink");

        if (!bean.getResponseList().isEmpty()) {

            builder.append(" due to \n");

            for (final String response : bean.getResponseList()) {

                builder.append(response);
                builder.append("\n");
            }
        } else {

            builder.append("!\n");
        }

        if (null != bean.getResponseCause()) {

            builder.append("\nException caught:\n");
            builder.append(ConvertUtils.throwableToString(bean.getResponseCause()));
            builder.append("\n");
        }

        builder.append("\n+++===+++\n");

        this.getLog().error(builder.toString());
    }
}
