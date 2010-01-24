/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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

    /** Is result cahed right now */
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
