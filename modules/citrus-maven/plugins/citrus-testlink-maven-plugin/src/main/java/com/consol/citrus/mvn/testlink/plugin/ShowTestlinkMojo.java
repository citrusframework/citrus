/*
 * Copyright 2006-2011 the original author or authors.
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
 * File: ShowTestlinkMojo.java
 * last modified: Thursday, December 29, 2011 (15:55) by: Matthias Beil
 */
package com.consol.citrus.mvn.testlink.plugin;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.consol.citrus.testlink.TestCaseBean;
import com.consol.citrus.testlink.TestLinkHandler;
import com.consol.citrus.testlink.TestLinkHandlerImpl;

/**
 * Show all available info's from {@code TestLink}. For this the URL to TestLink and the generated development key must
 * be provided.
 * 
 * @author Matthias Beil
 * @since Thursday, December 29, 2011
 * @goal show
 */
public class ShowTestlinkMojo extends AbstractMojo {

    // ~ Instance fields -----------------------------------------------------------------------------------------------

    /**
     * URL pointing to the TestLink URL. The path to the XML-RPC call will be append.
     * 
     * @parameter
     */
    private String url;

    /**
     * Development needed for authorization to TestLink. This must be generated within TestLink. For this TestLink must
     * be configured.
     * 
     * @parameter
     */
    private String devKey;

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code ShowTestlinkMojo} class.
     */
    public ShowTestlinkMojo() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        // make sure mandatory fields are set
        if ((null == this.url) || (this.url.isEmpty())) {

            throw new MojoFailureException("Parameter <url> may not be null or empty!");
        }

        if ((null == this.devKey) || (this.devKey.isEmpty())) {

            throw new MojoFailureException("Parameter <devKey> may not be null or empty!");
        }

        try {

            // get new test link container
            final TestLinkHandler container = new TestLinkHandlerImpl(this.url, this.devKey, this.getLog());

            // get all available test cases
            final List<TestCaseBean> testCaseList = container.readTestCases();

            // make sure there are some test case(s)
            if ((null != testCaseList) && (!testCaseList.isEmpty())) {

                // iterate over all test case(s) and log them
                for (final TestCaseBean bean : testCaseList) {

                    this.getLog().info(bean.toString());
                }
            } else {

                // no test case(s) found
                this.getLog().info("No test case(s) found!");
            }
        } catch (final MojoExecutionException moex) {

            throw moex;
        } catch (final Exception ex) {

            throw new MojoExecutionException("Exception caught for showing TestLink informations!", ex);
        }
    }

}
