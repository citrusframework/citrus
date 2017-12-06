---
layout: sample
title: Reporting sample
sample: sample-reporting
description: Add custom test reporting
categories: [samples]
permalink: /samples/reporting/
---

This sample deals with customized reporting features in Citrus. The sample writes extent Html reports using custom test listeners that report
test success and failure in that special way. Also the default Html reporting in Citrus is customized using application properties. 
Read about this feature in [reference guide](http://www.citrusframework.org/reference/html/index.html#reporting-and-test-results)

Objectives
---------

Citrus as a framework gives us extension points to add customized test reporting. The framework provides listener and reporting interfaces for that.
We want to write custom Html reports using the [Extent](http://extentreports.com/) reporting framework. We simply implement a custom reporting class that generates the Extent reports.

{% highlight java %}
public class ExtentReporter extends AbstractTestListener implements TestReporter {

    private ExtentHtmlReporter extentHtmlReporter;
    private ExtentReports extentReports;

    @Override
    public void onTestSuccess(TestCase test) {
        ExtentTest extentTest = extentReports.createTest(test.getName());
        extentTest.pass(getTestDetails(test.getMetaInfo()));
    }

    @Override
    public void onTestSkipped(TestCase test) {
        ExtentTest extentTest = extentReports.createTest(test.getName());
        extentTest.skip(getTestDetails(test.getMetaInfo()));
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        ExtentTest extentTest = extentReports.createTest(test.getName());
        extentTest.fail(cause);
    }

    @Override
    public void generateTestResults() {
        extentReports.flush();
    }
}
{% endhighlight %}
        
You can review the complete custom reporter on this sample's [github repository](https://github.com/christophd/citrus-samples/tree/master/sample-reporting/src/test/java/com/consol/citrus/samples/todolist/reporting/ExtentReporter.java).       

We add the new reporter implementation as Spring bean to the application context.
    
{% highlight java %}    
@Bean
public ExtentReporter extentReporter() {
    return new ExtentReporter();
}
{% endhighlight %}
        
That completes the reporting extension. Citrus will automatically see the new bean and add it to the list of reporters. The reporter is called when tests pass and fail. 
As a result the reporter writes Extent styled Html reports to the folder `target/citrus-reports/extent-reports.html` when Citrus tests are executed.

In addition to that Citrus provides some default Html reporting that is also customizable. We can use application properties to customize the report. Lets add a new application
property file to the sample sources in [resources/citrus-application.properties](https://github.com/christophd/citrus-samples/tree/master/sample-reporting/src/test/resources/citrus-application.properties).

The file contains some property settings that customize the default Citrus Html reporting.

    citrus.html.report.directory=target/citrus-reports
    citrus.html.report.file=citrus-reports.html
    citrus.html.report.logo:classpath:logo/reporting-logo.png
    
The tests now write default Html reports to the file `target/citrus-reports/citrus-reports.html`. Also we use a custom reporting logo that is automatically added to the reporting page header. 
You can now execute the tests in this sample and review the reports.

Run
---------

You can execute some sample Citrus test cases in this sample in order to write the reports.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

     mvn integration-test

You should see Citrus performing several tests with lots of debugging output. 
And of course green tests at the very end of the build and some new reporting files in `target/citrus-reports` folder.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.