package com.consol.citrus.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Properties;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.exceptions.CitrusRuntimeException;

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
    
    private String targetPackage;
    
    public static void main(String[] args) {
        try {    
            TestCaseCreator creator = TestCaseCreator.build()
                .withName(args[0])
                .withAuthor(args[1])
                .withDescription(args[2])
                .usePackage(args[3]);
            
            creator.createTestCase();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new CitrusRuntimeException("Wrong usage exception! " +
            		"Use parameters in the following way: [test.name] [test.author] [test.description] [test.subfolder]", e);
        }
    }
    
    public void createTestCase() {
        Properties properties = new Properties();
        properties.put("test.name", name);
        properties.put("test.author", author);
        properties.put("test.description", description);
        
        properties.put("test.updatedon.datetime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(GregorianCalendar.getInstance().getTime()));
        properties.put("test.creation.date", new SimpleDateFormat("yyyy-MM-dd").format(GregorianCalendar.getInstance().getTime()));
        
        properties.put("test.method.name", name.substring(0,1).toLowerCase() + name.substring(1));
        properties.put("test.package", targetPackage);
        
        targetPackage = targetPackage.replace('.', '/');
        
        createXMLFile(properties);    
        createJavaFile(properties);
    }
    
    private void createJavaFile(Properties properties) {
        BufferedReader reader = null;
        OutputStream buffered = null;
        
        try {
            File file = new File(CitrusConstants.DEFAULT_JAVA_DIRECTORY + targetPackage + "/" + name + ".java");
            if(file.getParentFile().exists() == false) {
                file.getParentFile().mkdirs();
            }
                
            FileOutputStream fos = new FileOutputStream(file);
            buffered = new BufferedOutputStream(fos);
    
            reader = new BufferedReader(new InputStreamReader(TestCaseCreator.class.getResourceAsStream("java-template.txt")));
            
            StringWriter sWriter = new StringWriter();
            
            String line;
            while ((line = reader.readLine()) != null) {
                sWriter.append(PropertyUtils.replacePropertiesInString(line, properties) + "\n");
            }
    
            buffered.write(sWriter.toString().getBytes());
            buffered.flush();
            fos.flush();
            fos.close();
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
    
    private void createXMLFile(Properties properties) {
        BufferedReader reader = null;
        OutputStream buffered = null;
        
        try {
            File file = new File(CitrusConstants.DEFAULT_TEST_DIRECTORY + targetPackage + "/" + name + ".xml");
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
    
    public static TestCaseCreator build() {
        return new TestCaseCreator();
    }
    
    public TestCaseCreator withName(String name) {
        this.name = name;
        return this;
    }
    
    public TestCaseCreator withAuthor(String author) {
        this.author = author;
        return this;
    }
    
    public TestCaseCreator withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public TestCaseCreator usePackage(String targetPackage) {
        this.targetPackage = targetPackage;
        return this;
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
     * @param targetPackage the targetPackage to set
     */
    public void setPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /**
     * @return the targetPackage
     */
    public String getPackage() {
        return targetPackage;
    }
}
