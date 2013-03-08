                           Citrus Samples
                        ====================

FLIGHTBOOKING SAMPLE

  The FlightBooking sample application receives request messages from a travel agency over
  JMS (async). The application splits the request into several flight bookings and forwards 
  the messages to respective airline applications (Http or JMS). The consolidated response 
  message is sent back to the calling travel agency asynchronous over JMS.
    
  The test cases contain simple workflows for handling TravelBookingRequest messages with 
  several flight bookings. See the log output for detailed information how Citrus validates 
  the received messages.
  
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