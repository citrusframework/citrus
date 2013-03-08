                           Citrus Samples
                        ====================

CITRUS SAMPLE APPLICATIONS

  The Citrus samples applications try to demonstrate how Citrus works in
  different integration test scenarios. The projects are executable with Maven 
  or ANT and should give you a detailed picture how Citrus testing works.

  In the reference documentation you can also find detailed descriptions of the sample
  applications.

PRECONDITIONS

  See the preconditions for using the Citrus sample applications:
  
  * Java 5.0 or higher
    Installed JDK 5.0 or higher plus JAVA_HOME environment variable set 
    up and pointing to your Java installation directory
    
  * Apache Maven 3.0.x or higher
    The sample projects are executable via Apache Maven (http://maven.apache.org/). You need 
    ANT installed and running an your machine in order to use this way of executing the 
    sample applications.
    
  * Apache ANT 1.8.x or higher
    The sample projects are executable via Apache ANT (http://ant.apache.org/). You need 
    ANT installed and running an your machine in order to use this way of executing the 
    sample applications.

  In each of the samples folders you will find the Maven pom.xml and ANT executable build script (build.xml) files.

OVERVIEW

  The Citrus samples section contains following projects:
  
  * FlightBooking (/flightbooking)
  * Greeting (/greeting)
  * BookStore (/bookstore)
  
  The projects cover following message transports and technologies:
  
  Transport          | JMS | Http | SOAP | Channel | JDBC | SYNC | ASYNC |
  ------------------------------------------------------------------------
  FlightBooking      |  X  |  X   |      |         |  X   |      |   X   |
  Greeting           |  X  |      |      |    X    |      |  X   |   X   |
  BookStore          |  X  |      |  X   |         |      |  X   |       |  
  
  Pick your sample application for try out and got to the respective folder.

RUNNING THE SAMPLES

  All samples hold a web application project (war folder) and a Citrus test project (citrus-test). First of all start 
  the sample application within the war folder. You can do this either by calling "mvn jetty:run" command using an 
  embedded Jetty Web Server Container or you call "mvn package" and deploy the resulting war archive to a separate 
  Web container of your choice.
  
  Once the sample application is deployed and running you can execute the Citrus test cases in citrus-test folder. 
  Open a separate command line terminal and navigate to the citrus-test folder.
  
  Execute all Citrus tests by calling "mvn integration-test". You can also pick a single test by calling "mvn integration-test -Ptest=TestName". 
  You should see Citrus performing several tests with lots of debugging output in both terminals (sample application server 
  and Citrus test client). And of course green tests at the very end of the build.
  
  You can also use Apache ANT to execute the tests. Run the following command to see which targets are offered:

  > ant -p

  Buildfile: build.xml

  Main targets:

   citrus.run.single.test  Runs a single test by name
   citrus.run.tests        Runs all Citrus tests
   create.test             Creates a new empty test case
  Default target: citrus.run.tests

  The different targets are not very difficult to understand. You can run all tests, a single test case by its name or create 
  new test cases.
  
  Just try to call the different options like this:
  
  > ant citrus.run.tests

WHAT'S NEXT?!

  Have fun with Citrus! Write your own test cases and kill all bugs!

  The Citrus Team
  ConSol* Software GmbH
  Christoph Deppisch
  christoph.deppisch@consol.de
  
  http://www.citrusframework.org