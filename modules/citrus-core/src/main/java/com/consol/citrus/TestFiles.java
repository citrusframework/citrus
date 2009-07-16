package com.consol.citrus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestFiles {
    private List<String> fileNames = new ArrayList<String>();
    private Map<String, String> folders = new HashMap();

    /**
     * @return the fileNames
     */
    public List<String> getFileNames() {
        return fileNames;
    }

    public String getTestFolder(String testName) {
        return (String)folders.get(testName);
    }

    public void addFile(File file, String startDir) {
        fileNames.add(file.getPath().substring(startDir.length()));
        folders.put(file.getName(), file.getParent());
    }
    
    /**
     * Delegate to file names list
     * @return number of test files available
     */
    public int size() {
        return fileNames.size();
    }
}
