package com.consol.citrus.report;

import org.springframework.util.StringUtils;


public class TestResult {

    public static enum RESULT {SUCCESS, FAILURE, SKIP};

    private RESULT result;
    
    private String testName;
    
    private Throwable cause;
    
    public TestResult(String name, RESULT result) {
        this.testName = name;
        this.result = result;
    }

    public TestResult(String name, RESULT result, Throwable cause) {
        this.testName = name;
        this.result = result;
        this.cause = cause;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("  " + testName);

        int spaces = 50 - testName.length();
        for (int i = 0; i < spaces; i++) {
            buf.append(" ");
        }
        
        switch (result) {
            case SUCCESS:
                buf.append(": successful");
                break;
            case SKIP:
                buf.append(": skipped - because excluded from test run");
                break;
            case FAILURE:
                if(cause != null && StringUtils.hasText(cause.getLocalizedMessage())) {
                    buf.append(": failed - with exception: " + cause.getLocalizedMessage());
                } else {
                    buf.append(": failed - No exception available");
                }
                break;
            default:
                break;
        }

        String resultString = buf.toString();
        
        if (resultString.length() > 100) {
            return resultString.substring(0, 100) + " ...";
        } else {
            return resultString;
        }
    }

    /**
     * @param cause the cause to set
     */
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    /**
     * @return the cause
     */
    public Throwable getCause() {
        return cause;
    }
    
    /**
     * @return the testName
     */
    public String getTestName() {
        return testName;
    }

    /**
     * @param testName the testName to set
     */
    public void setTestName(String testName) {
        this.testName = testName;
    }

    /**
     * @return the result
     */
    public RESULT getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(RESULT result) {
        this.result = result;
    }
}
