package com.consol.citrus.report;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestSuite;

public class TestSuiteListeners implements TestSuiteListener {
    
    /** List of testsuite listeners **/
    @Autowired
    private List<TestSuiteListener> tesSuiteListeners = new ArrayList<TestSuiteListener>();
    
    public void onFinish(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinish(testsuite);
        }
    }

    public void onFinishFailure(TestSuite testsuite, Throwable cause) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinishFailure(testsuite, cause);
        }
    }

    public void onFinishSuccess(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinishSuccess(testsuite);
        }
    }

    public void onStart(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStart(testsuite);
        }
    }

    public void onStartFailure(TestSuite testsuite, Throwable cause) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStartFailure(testsuite, cause);
        }
    }

    public void onStartSuccess(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStartSuccess(testsuite);
        }
    }
}
