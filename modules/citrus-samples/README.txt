                           Citrus Samples
                        ====================

CITRUS SAMPLE APPLICATIONS

  The Citrus samples applications try to demonstrate how Citrus works with
  different message transports. The projects are executable with ANT and should give you
  a detailed picture how to execute Citrus test cases.

  In the reference documentation you can find a detailed description of the sample
  applications.

INSTALLATION & PRECONDITIONS

  See the preconditions for using the Citrus sample applications:
  
  * Java 5.0 or higher
    Installed JDK 5.0 or higher plus JAVA_HOME environment variable set 
    up and pointing to your Java installation directory
    
  * Apache ANT 1.7.1 or higher
    The sample projects are executed with Apache ANT (http://ant.apache.org/). You need 
    ANT installed and running an your machine in order to use the Citrus samples.

  In each sample subfolder you will find the ANT executable build script (build.xml).
  Execute following ANT command to see which targets are offered by the sample Citrus 
  installation:

  > ant -p

  Buildfile: build.xml

  Main targets:

   citrus.run.single.test  Runs a single test by name
   citrus.run.tests        Runs all Citrus tests
   create.test             Creates a new empty test case
  Default target: citrus.run.tests

  The different targets are not very difficult to understand. You can run all tests, a 
  single test case by its name or create new test cases.
  
  Just try to call the different options like this:
  
  > ant citrus.run.tests

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
  BookStore       |  X  |      |  X   |         |      |  X   |       |
  
FLIGHTBOOKING SAMPLE

  The FlightBooking sample application receives request messages from a travel agency over
  JMS (async). The application splits the request into several flight bookings and forwards 
  the messages to respective airline applications (Http or JMS). The consolidated response 
  message is sent back to the calling travel agency asynchronous over JMS.
    
  The Citrus test cases will automatically start the sample application before the tests are 
  executed. To start the FlightBooking sample application in standalone mode call 
  'flightBookingDemo.bat' (Windows) or 'flightBookingDemo.sh' (Unix).

  The test cases contain simple workflows for handling TravelBookingRequest messages with 
  several flight bookings. See the log output for detailed information how Citrus validates 
  the received messages.

GREETING SAMPLE
  
  The greeting sample project is separated into two parts each one handling another type of 
  message transport. One part is dealing with simple JMS messaging (synchronous). The other part 
  is handling messages on Spring Integration message channels. The application receives greeting 
  requests messages and creates proper greeting responses according to the chosen message 
  transport (JMS or message channel).
  
  The test cases will start and stop the sample application automatically before any test is 
  executed. If you want to start the JMS Greeting sample application in standalone mode call 
  'greetingDemo.bat' (Windows) or 'greetingDemo.sh' (Unix).
  
BOOKSTORE SAMPLE

  The BookStore sample application offers a SOAP WebService with following supported 
  operations:
  
  * addBook
  * getBookDetails
  * listBooks 
  
  Each operation will result in a synchronous SOAP response to the calling client. Duplicate 
  books (isbn) or unknown books will generate SOAP Faults in the response. The different sample
  test cases will call the WebService as client and test the complete functionality for the 
  available operations.
  
  The BookStore WebService application is started automatically before the test cases are 
  executed, but you can also start the BookStore sample application in standalone mode 
  ('bookStoreDemo.bat' (Windows) or 'bookStoreDemo.sh' (Unix)).
  
WHAT'S NEXT?!

  Have fun with Citrus!

  The Citrus Team
  ConSol* Software GmbH
  Christoph Deppisch
  christoph.deppisch@consol.de
  
  http://www.citrusframework.org