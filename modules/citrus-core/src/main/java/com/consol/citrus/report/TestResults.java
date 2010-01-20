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

public class TestResults extends ArrayList<TestResult> {

    private static final long serialVersionUID = 1L;

    private boolean cached = false;
    
    private int cntSuccess = 0;
    private int cntFailed = 0;
    private int cntSkipped = 0;
    
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
