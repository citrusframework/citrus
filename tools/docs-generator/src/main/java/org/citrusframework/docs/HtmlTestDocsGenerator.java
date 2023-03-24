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

package org.citrusframework.docs;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * Class to automatically generate a list of all available tests in HTML.
 * 
 * @author Christoph Deppisch
 * @since 2007
 */
public class HtmlTestDocsGenerator extends AbstractTestDocsGenerator {
    
    /** Test doc specific information */
    private String pageTitle = "Citrus Test Documentation";
    private String overviewTitle = "Overview";
    private String overviewColumns = "1";
    private String logoFilePath = "logo.png";
    
    /**
     * Default constructor with test doc template name.
     */
    public HtmlTestDocsGenerator() {
        super("CitrusTests.html", "testdoc.html.template");
    }
    
    @Override
    public void doHeader(OutputStream buffered) throws TransformerException, IOException, SAXException {
        List<File> testFiles = getTestFiles();
        int maxEntries = testFiles.size() / Integer.valueOf(getTestDocProperties().getProperty("overview.columns"));

        buffered.write("<td style=\"border:1px solid #bbbbbb;\">".getBytes());
        buffered.write("<ol>".getBytes());

        for (int i = 0; i < testFiles.size(); i++) {
            if (i != 0 && i % maxEntries == 0 && testFiles.size() - i >= maxEntries) {
                buffered.write("</ol>".getBytes());
                buffered.write("</td>".getBytes());
                buffered.write("<td style=\"border:1px solid #bbbbbb;\">".getBytes());
                buffered.write(("<ol start=\"" + (i+1) + "\">").getBytes());
            }

            buffered.write("<li>".getBytes());
            buffered.write(("<a href=\"#" + i + "\">").getBytes());
            buffered.write(testFiles.get(i).getName().getBytes());
            buffered.write("</a>".getBytes());
        }

        buffered.write("</ol>".getBytes());
        buffered.write("</td>".getBytes());
    }
    
    @Override
    public void doBody(OutputStream buffered) throws TransformerException, IOException, SAXException {
        StreamResult res = new StreamResult(buffered);
        Transformer t = getTransformer("generate-html-doc.xslt", "text/html", "html");
        
        int testNumber = 1;
        for (File testFile : getTestFiles()) {
            buffered.write("<tr>".getBytes());

            Source xml = new DOMSource(getDocumentBuilder().parse(testFile));
            buffered.write(("<td style=\"border:1px solid #bbbbbb\">" + testNumber + ".</td>").getBytes());

            buffered.write("<td style=\"border:1px solid #bbbbbb\">".getBytes());
            t.transform(xml, res);
            buffered.write(("<a name=\"" + testNumber + "\" href=\"file:///" + testFile.getAbsolutePath() + "\">" + testFile.getName() + "</a>").getBytes());
            buffered.write("</td>".getBytes());

            buffered.write("</tr>".getBytes());
            
            testNumber++;
        }
    }
    
    /**
     * Builds a new test doc generator.
     * @return
     */
    public static HtmlTestDocsGenerator build() {
        return new HtmlTestDocsGenerator();
    }
    
    /**
     * Adds a custom output file.
     * @param filename the output file name.
     * @return
     */
    public HtmlTestDocsGenerator withOutputFile(String filename) {
        this.setOutputFile(filename);
        return this;
    }
    
    /**
     * Adds a custom page title.
     * @param pageTitle the page title.
     * @return
     */
    public HtmlTestDocsGenerator withPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }
    
    /**
     * Adds a custom overview title.
     * @param overvieTitle the title.
     * @return
     */
    public HtmlTestDocsGenerator withOverviewTitle(String overvieTitle) {
        this.overviewTitle = overvieTitle;
        return this;
    }
    
    /**
     * Adds a column configuration.
     * @param columns the column names.
     * @return
     */
    public HtmlTestDocsGenerator withColumns(String columns) {
        this.overviewColumns = columns;
        return this;
    }
    
    /**
     * Adds a custom logo file path.
     * @param logoFilePath the file path.
     * @return
     */
    public HtmlTestDocsGenerator withLogo(String logoFilePath) {
        this.logoFilePath = logoFilePath;
        return this;
    }
    
    /**
     * Adds a custom test source directory.
     * @param testDir the test source directory.
     * @return
     */
    public HtmlTestDocsGenerator useSrcDirectory(String testDir) {
        this.setSrcDirectory(testDir);
        return this;
    }
    
    /**
     * Executable application cli.
     * @param args
     */
    public static void main(String[] args) {
        try {    
            HtmlTestDocsGenerator generator = HtmlTestDocsGenerator.build();

            generator.useSrcDirectory(args.length == 1 ? args[0] : generator.srcDirectory)
                .withOutputFile(args.length == 2 ? args[1] : generator.outputFile)
                .withPageTitle(args.length == 3 ? args[2] : generator.pageTitle)
                .withLogo(args.length == 4 ? args[3] : generator.logoFilePath)
                .withOverviewTitle(args.length == 5 ? args[4] : generator.overviewTitle)
                .withColumns(args.length == 6 ? args[5] : generator.overviewColumns);


            generator.generateDoc();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CitrusRuntimeException("Wrong usage exception! " +
                    "Use parameters in the following way: [test.directory] [output.file]", e);
        }
    }
    
    @Override
    protected Properties getTestDocProperties() {
        Properties props = new Properties();
        props.setProperty("page.title", pageTitle);
        props.setProperty("overview.title", overviewTitle);
        props.setProperty("overview.columns", overviewColumns);
        props.setProperty("logo.file.path", logoFilePath);
        props.setProperty("date", String.format("%1$tY-%1$tm-%1$td", new GregorianCalendar()));
        
        return props;
    }

    /**
     * @param pageTitle the pageTitle to set
     */
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    /**
     * @return the pageTitle
     */
    public String getPageTitle() {
        return pageTitle;
    }

}
