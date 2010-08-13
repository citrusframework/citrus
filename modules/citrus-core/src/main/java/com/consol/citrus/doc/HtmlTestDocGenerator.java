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

package com.consol.citrus.doc;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.PropertyUtils;

/**
 * Class to automatically generate a list of all available tests in HTML.
 * 
 * @author Christoph Deppisch
 * @since 2007
 */
public class HtmlTestDocGenerator {
    private final static String BODY_PLACEHOLDER = "+++++ BODY +++++";
    private final static String OVERVIEW_PLACEHOLDER = "+++++ OVERVIEW +++++";
    
    private String testDirectory = "src/citrus/tests";
    
    private String pageTitle = "Citrus Test Documentation";
    
    private String overviewTitle = "Overview";
    
    private String overviewColumns = "1";
    
    private String logoFilePath = "logo.png";
    
    private String outputFile = "CitrusTests";
    
    private String testDocTemplate = "testdoc.html.template";
    
    public void generateDoc() {
        BufferedReader reader = null;
        FileOutputStream file = null;
        OutputStream buffered = null;
        
        try {
            List<File> testFiles = FileUtils.getTestFiles(testDirectory);

            Properties props = new Properties();
            props.setProperty("page.title", pageTitle);
            props.setProperty("overview.title", overviewTitle);
            props.setProperty("overview.columns", overviewColumns);
            props.setProperty("logo.file.path", logoFilePath);
            props.setProperty("date", String.format("%1$tY-%1$tm-%1$td", new GregorianCalendar()));
            
            Source xsl = new StreamSource(new ClassPathResource("generate-html-doc.xslt", HtmlTestDocGenerator.class).getInputStream());
            
            TransformerFactory factory = TransformerFactory.newInstance();

            Transformer t = factory.newTransformer(xsl);

            t.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/html");
            t.setOutputProperty(OutputKeys.METHOD, "html");

            file = new FileOutputStream("target/" + outputFile + ".html");
            buffered = new BufferedOutputStream(file);
            StreamResult res = new StreamResult(buffered);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            documentBuilderFactory.setNamespaceAware(true);

            reader = new BufferedReader(new InputStreamReader(HtmlTestDocGenerator.class.getResourceAsStream(testDocTemplate)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().equalsIgnoreCase(OVERVIEW_PLACEHOLDER)) {
                    buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
                } else {
                    break;
                }
            }

            int maxEntries = testFiles.size() / new Integer(props.getProperty("overview.columns")).intValue();

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

            while ((line = reader.readLine()) != null) {
                if (!line.trim().equalsIgnoreCase(BODY_PLACEHOLDER)) {
                    buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
                } else {
                    break;
                }
            }

            int testNumber = 1;
            for (File testFile : testFiles) {
                buffered.write("<tr>".getBytes());

                Source xml = new DOMSource(builder.parse(testFile));
                buffered.write(("<td style=\"border:1px solid #bbbbbb\">" + testNumber + ".</td>").getBytes());

                buffered.write("<td style=\"border:1px solid #bbbbbb\">".getBytes());
                t.transform(xml, res);
                buffered.write(("<a name=\"" + testNumber + "\" href=\"file:///" + testFile.getAbsolutePath() + "\">" + testFile.getName() + "</a>").getBytes());
                buffered.write("</td>".getBytes());

                buffered.write("</tr>".getBytes());
                
                testNumber++;
            }

            while ((line = reader.readLine()) != null) {
                buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
            }

            buffered.flush();
            file.close();
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (TransformerException e) {
            throw new CitrusRuntimeException(e);
        } catch (SAXException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if(buffered != null) {
                try {
                    buffered.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if(file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static HtmlTestDocGenerator build() {
        return new HtmlTestDocGenerator();
    }
    
    public HtmlTestDocGenerator withOutputFile(String filename) {
        this.setOutputFile(filename);
        return this;
    }
    
    public HtmlTestDocGenerator withPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }
    
    public HtmlTestDocGenerator withOverviewTitle(String overvieTitle) {
        this.overviewTitle = overvieTitle;
        return this;
    }
    
    public HtmlTestDocGenerator withColumns(String columns) {
        this.overviewColumns = columns;
        return this;
    }
    
    public HtmlTestDocGenerator withLogo(String logoFilePath) {
        this.logoFilePath = logoFilePath;
        return this;
    }
    
    public HtmlTestDocGenerator useTestDirectory(String testDir) {
        this.setTestDirectory(testDir);
        return this;
    }
    
    public static void main(String[] args) {
        try {    
            HtmlTestDocGenerator creator = HtmlTestDocGenerator.build();
            
            creator.useTestDirectory(args.length == 1 ? args[0] : creator.testDirectory)
                .withOutputFile(args.length == 2 ? args[1] : creator.outputFile)
                .withPageTitle(args.length == 3 ? args[2] : creator.pageTitle)
                .withLogo(args.length == 4 ? args[3] : creator.logoFilePath)
                .withOverviewTitle(args.length == 5 ? args[4] : creator.overviewTitle)
                .withColumns(args.length == 6 ? args[5] : creator.overviewColumns);
                
            
            creator.generateDoc();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CitrusRuntimeException("Wrong usage exception! " +
                    "Use parameters in the following way: [test.directory] [output.file]", e);
        }
    }

    /**
     * @param testDirectory the testDirectory to set
     */
    public void setTestDirectory(String testDirectory) {
        this.testDirectory = testDirectory;
    }

    /**
     * @return the testDirectory
     */
    public String getTestDirectory() {
        return testDirectory;
    }

    /**
     * @param outputFile the outputFile to set
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * @return the outputFile
     */
    public String getOutputFile() {
        return outputFile;
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
