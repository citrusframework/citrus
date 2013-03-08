                           Citrus Samples
                        ====================

GREETING SAMPLE
  
  The greeting sample project is separated into two parts each one handling another type of 
  message transport. One part is dealing with simple JMS messaging (synchronous). The other part 
  is handling messages on Spring Integration message channels. The application receives greeting 
  requests messages and creates proper greeting responses according to the chosen message 
  transport (JMS or message channel).
  
SERVER

  Got to the war folder and start the BookStore WebService application in a Web Container. Easiest 
  way for you to do this is to execute
  
  > mvn jetty:run
  
  here!
  
  An embedded Jetty Web Server Container is started with the BookStore application deployed. You can
  alsp call "mvn package" and deploy the resulting war archive to a separate Web container of your choice.
  
CITRUS TEST

  Once the sample application is deployed and running you can execute the Citrus test cases in citrus-test folder. 
  Open a separate command line terminal and navigate to the citrus-test folder.
  
  Execute all Citrus tests by calling 
  
  > mvn integration-test
  
  You can also pick a single test by calling 
  
  > mvn integration-test -Ptest=TestName
   
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