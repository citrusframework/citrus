package com.consol.citrus.report;

import java.util.ArrayList;
import java.util.Collection;

import com.consol.citrus.report.TestResult.RESULT;

public class TestResults extends ArrayList<TestResult> {
    private boolean cached = false;
    
    int cntSuccess = 0;
    int cntFailed = 0;
    int cntSkipped = 0;
    
    private final Object cacheLock = new Object();
    
    public boolean addResult(TestResult result) {
        synchronized (cacheLock) {
            cached = false;
            
            return add(result);
        }
    }
    
    @Override
    public boolean add(TestResult o) {
        synchronized (cacheLock) {
            cached = false;
            
            return super.add(o);
        }
    }
    
    @Override
    public boolean addAll(Collection<? extends TestResult> c) {
        synchronized (cacheLock) {
            cached = false;
            
            return super.addAll(c);
        }
    }

    public int getSuccess() {
        synchronized (cacheLock) {
            if(cached == false) {
                cntSuccess = getCountForResult(RESULT.SUCCESS);
            }
            
            return cntSuccess;
        }
    }
    
    public int getFailed() {
        synchronized (cacheLock) {
            if(cached == false) {
                cntFailed = getCountForResult(RESULT.FAILURE);
            }
            
            return cntFailed;
        }
    }
    
    public int getSkipped() {
        synchronized (cacheLock) {
            if(cached == false) {
                cntSkipped = getCountForResult(RESULT.SKIP);
            }
            
            return cntSkipped;
        }
    }
    
    private int getCountForResult(RESULT result) {
        int count = 0;
        
        for (TestResult testResult : this) {
            if(testResult.getResult().equals(result)) {
                count++;
            }
        }
        
        return count;
    }
}
