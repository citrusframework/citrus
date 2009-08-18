package com.consol.citrus.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Cass to provide general file utils, such as listing all xml files in a directory. Or finding certain
 * tests in a directory.
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 26.01.2007
 *
 */
public class FileUtils {
    /**
     * Method to retrieve all test defining xml files in given directory.
     * Subfolders are supported.
     *
     * @param startDir the directory to hold the files
     * @return list of test files as filename paths
     */
    public static List<String> getTestFiles(final String startDir) {
        /* file names to be returned */
        final List<String> files = new ArrayList<String>();

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
                    if ((name.endsWith(".xml") || tmp.isDirectory()) && !name.startsWith("CVS")) {
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
                    files.add(file.getPath().substring(startDir.length()));
                }
            }
        }

        return files;
    }
}
