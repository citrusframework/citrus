package com.consol.citrus.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestConstants;

/**
 * CLI creating a new test case from template.
 * 
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 21.04.2009
 */
public class TestCaseCreator {
    private String name;
    
    private String author;
    
    private String description;
    
    private String directory;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestCaseCreator.class);
    
    public static void main(String[] args) {
        try {    
            TestCaseCreator creator = new TestCaseCreator();
            
            creator.setName(args[0]);
            creator.setAuthor(args[1]);
            creator.setDescription(args[2]);
            creator.setDirectory(args[3]);
            
            creator.createTestCase();
        } catch (ArrayIndexOutOfBoundsException e) {
            log.info("Wrong usage exception!");
            log.info("Use parameters in the following way: [test.name] [test.author] [test.description] [test.subfolder]");
        }
    }
    
    public void createTestCase() {
        BufferedReader reader = null;
        OutputStream buffered = null;
        
        try {
            Properties properties = new Properties();
            properties.put("test.name", name);
            properties.put("test.author", author);
            properties.put("test.description", description);
            
            properties.put("test.updatedon.datetime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(GregorianCalendar.getInstance().getTime()));
            properties.put("test.creation.date", new SimpleDateFormat("yyyy-MM-dd").format(GregorianCalendar.getInstance().getTime()));
            
            File file = new File(directory + "/" + name + "." + TestConstants.TESTCASE_FILE_EXTENSION);
            if(file.getParentFile().exists() == false) {
                file.getParentFile().mkdirs();
            }
                
            FileOutputStream fos = new FileOutputStream(file);
            buffered = new BufferedOutputStream(fos);

            reader = new BufferedReader(new InputStreamReader(TestCaseCreator.class.getResourceAsStream("test-template.xml")));
            
            StringWriter sWriter = new StringWriter();
            
            String line;
            while ((line = reader.readLine()) != null) {
                sWriter.append(PropertyUtils.replacePropertiesInString(line, properties) + "\n");
            }

            buffered.write(sWriter.toString().getBytes());
            buffered.flush();
            fos.flush();
            fos.close();
            
            log.info("Successfully created new test case " + directory + "/" + name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(buffered != null) {
                    buffered.close();
                }
                
                if(reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param directory the directory to set
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }
}
