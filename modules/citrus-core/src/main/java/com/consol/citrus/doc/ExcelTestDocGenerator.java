/*
 * Copyright 2006-2009 ConSol* Software GmbH.
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

package com.consol.citrus.doc;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.xml.transform.StringSource;
import org.xml.sax.SAXException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.PropertyUtils;

/**
 * Class to automatically generate a list of all available tests.
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 02.03.2007
 */
public class ExcelTestDocGenerator {
    private final static String BODY_PLACEHOLDER = "+++++ BODY +++++";

    private String testDirectory = "src/citrus/tests";
    
    private String outputFile = "CitrusTests";
    
    private String testDocTemplate = "testdoc.xls.template";
    
    private String pageTitle = "Citrus Test Documentation";
    
    private String company = "Unknown";
    
    private String author = "Citrus Testframework";
    
    private Resource headers = new ClassPathResource("testdoc-header.xml", ExcelTestDocGenerator.class);
    
    private String customHeaders = "";
    
    public void generateDoc() {
        try {
            List<File> testFiles = FileUtils.getTestFiles(testDirectory);

            Properties props = new Properties();
            props.setProperty("page.title", pageTitle);
            props.setProperty("company", company);
            props.setProperty("author", author);
            props.setProperty("date", String.format("%1$tY-%1$tm-%1$td", new GregorianCalendar()));
            
            Source xsl = new StreamSource(new ClassPathResource("generate-xls-doc.xslt", ExcelTestDocGenerator.class).getInputStream());
            
            TransformerFactory factory = TransformerFactory.newInstance();

            Transformer t = factory.newTransformer(xsl);

            t.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
            t.setOutputProperty(OutputKeys.METHOD, "xml");

            FileOutputStream file = new FileOutputStream("target/" + outputFile + ".xls");
            OutputStream buffered = new BufferedOutputStream(file);
            StreamResult res = new StreamResult(buffered);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            documentBuilderFactory.setNamespaceAware(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(ExcelTestDocGenerator.class.getResourceAsStream(testDocTemplate)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(BODY_PLACEHOLDER) == false) {
                    buffered.write(PropertyUtils.replacePropertiesInString(line, props).getBytes());
                } else {
                    break;
                }
            }

            if(StringUtils.hasText(customHeaders)) {
                t.transform(new StringSource(buildHeaderXml()), res);
            } else {
                //first generate header row
                t.transform(new StreamSource(headers.getInputStream()), res);
            }
            
            int testNumber = 1;
            for (File testFile : testFiles) {
            	buffered.write("<Row>".getBytes());

                Source xml = new DOMSource(builder.parse(testFile));
                buffered.write(("<Cell><Data ss:Type=\"Number\">" + testNumber + "</Data></Cell>").getBytes());

                t.transform(xml, res);

                buffered.write(("<Cell><Data ss:Type=\"String\">" + testFile.getName() + "</Data></Cell>").getBytes());

                buffered.write("</Row>".getBytes());
                
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
        }
    }
    
    private String buildHeaderXml() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("<headers xmlns=\"http://www.citrusframework.org/schema/doc/header\">");
        
        StringTokenizer tok = new StringTokenizer(customHeaders, ";");
        
        while (tok.hasMoreElements()) {
            buf.append("<header>" + tok.nextToken() + "</header>");
        }
        
        buf.append("</headers>");
        
        return buf.toString();
    }

    public static ExcelTestDocGenerator build() {
        return new ExcelTestDocGenerator();
    }
    
    public ExcelTestDocGenerator withOutputFile(String filename) {
        this.setOutputFile(filename);
        return this;
    }
    
    public ExcelTestDocGenerator withPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }
    
    public ExcelTestDocGenerator useTestDirectory(String testDir) {
        this.setTestDirectory(testDir);
        return this;
    }
    
    public ExcelTestDocGenerator withAuthor(String author) {
        this.author = author;
        return this;
    }
    
    public ExcelTestDocGenerator withCompany(String company) {
        this.company = company;
        return this;
    }
    
    public ExcelTestDocGenerator withCustomHeaders(String customHeaders) {
        this.customHeaders = customHeaders;
        return this;
    }
    
    public static void main(String[] args) {
        try {    
            ExcelTestDocGenerator creator = ExcelTestDocGenerator.build();
            
            creator.useTestDirectory(args.length == 1 ? args[0] : creator.testDirectory)
                .withOutputFile(args.length == 2 ? args[1] : creator.outputFile)
                .withPageTitle(args.length == 3 ? args[2] : creator.pageTitle)
                .withAuthor(args.length == 4 ? args[3] : creator.author)
                .withCompany(args.length == 5 ? args[4] : creator.company)
                .withCustomHeaders(args.length == 6 ? args[5] : "");
            
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

    /**
     * @return the company
     */
    public String getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }
}
