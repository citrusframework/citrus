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

package com.consol.citrus.report;

import java.util.ArrayList;
import java.util.Collection;

import com.consol.citrus.report.TestResult.RESULT;

/**
 * Multiple {@link TestResult} instances combined to a {@link TestResults}.
 * 
 * @author Christoph Deppisch
 */
public class TestResults extends ArrayList<TestResult> {

    private static final long serialVersionUID = 1L;

    /** Is result cached right now */
    private boolean cached = false;
    
    /** Success, failure and skipped counter */
    private int cntSuccess = 0;
    private int cntFailed = 0;
    private int cntSkipped = 0;
    
    /** Monitor for caching logic */
    private final Object cacheLock = new Object();
    
    /**
     * Adds a test result to the result list.
     * @param result
     * @return
     */
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

    /**
     * Get number of tests in success.
     * @return
     */
    public int getSuccess() {
        synchronized (cacheLock) {
            if(!cached) {
                cntSuccess = getCountForResult(RESULT.SUCCESS);
            }
            
            return cntSuccess;
        }
    }
    
    /**
     * Get number of tests failed.
     * @return
     */
    public int getFailed() {
        synchronized (cacheLock) {
            if(!cached) {
                cntFailed = getCountForResult(RESULT.FAILURE);
            }
            
            return cntFailed;
        }
    }
    
    /**
     * Get number of skipped tests.
     * @return
     */
    public int getSkipped() {
        synchronized (cacheLock) {
            if(!cached) {
                cntSkipped = getCountForResult(RESULT.SKIP);
            }
            
            return cntSkipped;
        }
    }
    
    /**
     * Get number of tests matching a {@link RESULT}.
     * @param result
     * @return
     */
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
