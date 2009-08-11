package com.consol.citrus.util;

import java.io.*;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestFiles;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Cass to provide general file utils, such as listing all xml files in a directory. Or finding certain
 * tests in a directory.
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 26.01.2007
 *
 */
public class FileUtils {
    public static final String XML_FILE_EXTENSION = "xml";
    public static final String SQL_FILE_EXTENSION = "sql";

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Method to retrieve all test defining context files in given directory.
     * Subfolders are supported as well as free naming of files.
     *
     * @param startDir the directory to hold the files
     * @param fileEtension file extension to search for
     * @return list of test files as filenames
     */
    public static TestFiles getTestFiles(final String startDir, final String fileExtension) {
        /* file names to be returned */
        final TestFiles files = new TestFiles();

        /* Stack to hold potential sub directories */
        final Stack dirs = new Stack();
        /* start directory */
        final File startdir = new File(startDir);
        
        if (startdir.isDirectory())
            dirs.push(startdir);

        /* walk through the directories */
        while (dirs.size() > 0) {
            File file = (File) dirs.pop();
            File[] found = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File tmp = new File(dir.getPath() + "/" + name);

                    /* Only allowing XML files as spring configuration files */
                    if ((name.endsWith(fileExtension) || tmp.isDirectory()) && !name.startsWith("CVS")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            for (int i = 0; i < found.length; i++) {
                /* Subfolder support */
                if (found[i].isDirectory())
                    dirs.push(found[i]);
                else {
                    files.addFile(found[i], startDir);
                }
            }
        }

        return files;
    }

    /**
     * Method to retrieve the specific context file that contains the test name searched for.
     * Subfolders are supported as well as free naming of files.
     *
     * @param startDir directory where to start the search
     * @param testName test name to search for
     * @param fileExtension file extension to search for
     * @throws CitrusRuntimeException
     * @return the name of test file
     */
    public static String getTestFileForTest(final String startDir, final String testName, final String fileExtension)
        throws IOException, FileNotFoundException {
        /* Stack to hold potential sub directories */
        final Stack dirs = new Stack();
        /* start directory */
        final File startdir = new File(startDir);

        if (startdir.isDirectory())
            dirs.push(startdir);

        /* walk through the directories */
        while (dirs.size() > 0) {
            File file = (File) dirs.pop();
            File[] found = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File tmp = new File(dir.getPath() + "/" + name);

                    /* Only allowing XML files as spring configuration files */
                    if ((name.endsWith(fileExtension) || tmp.isDirectory()) && !name.startsWith("CVS")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            for (int i = 0; i < found.length; i++) {
                /* Subfolder support */
                if (found[i].isDirectory())
                    dirs.push(found[i]);
                else {
                    /* look into the test file and search for the test name */
                    RandomAccessFile raf = new RandomAccessFile(found[i], "r");

                    String line;
                    while ((line = raf.readLine()) != null) {
                        /* assuming the test to inherit from basic test case bean "testCase" */
                        if (line.indexOf("parent=\"testCase\"") != -1 || line.indexOf("<testcase name=\"") != -1) {
                            int startIndex = line.indexOf("name=\"")
                            + "name=\"".length();
                            int index = startIndex;
                            StringBuffer name = new StringBuffer();
                            while (Character.isJavaIdentifierPart(line
                                    .charAt(index))) {
                                name.append(line.charAt(index));
                                index++;
                            }

                            raf.close();

                            if (testName.equalsIgnoreCase(name.toString())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Found file "
                                            + found[i].getPath().substring(startDir.length())
                                            + " containing test " + testName);
                                }
                                return found[i].getPath().substring(startDir.length());
                            } else
                                break;
                        }
                    }
                }
            }
        }
        throw new CitrusRuntimeException("Could not find test with name "
                + testName + " in test files. Test directory is: " + startDir);
    }
}
