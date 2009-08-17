package com.consol.citrus.report;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestCase;

public class TestListeners implements TestListener {
    
    /** List of test listeners **/
    @Autowired
    private List<TestListener> testListeners = new ArrayList<TestListener>();
    
    public void onTestFailure(TestCase test, Throwable cause) {
        for (TestListener listener : testListeners) {
            listener.onTestFailure(test, cause);
        }
    }

    public void onTestFinish(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestFinish(test);
        }
    }

    public void onTestSkipped(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSkipped(test);
        }
    }

    public void onTestStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestStart(test);
        }
    }

    public void onTestSuccess(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSuccess(test);
        }
    }
}
