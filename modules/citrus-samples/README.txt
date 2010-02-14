Citrus Samples

The Citrus samples application should give you an impression how to use Citrus 
and how you can execute test cases using ANT.

In the reference documentation you can find a detailed description of the sample
applications.

====================

FlightBooking sample

To start the FlightBooking sample application call 'flightBookingDemo.bat' (Windows)
or 'flightBookingDemo.sh' (Unix). The FlightBooking sample application will start up 
and listen for TravelBooking requests.

To start the Citrus sample test cases, you need ANT installed on your local machine (see
http://ant.apache.org/ for download an installation instructions)

Execute following ANT command to see which targets are offered by the sample Citrus 
installation:

> ant -p

Buildfile: build.xml

Main targets:

 citrus.run.single.test  Runs a single test by name
 citrus.run.tests        Runs all Citrus tests
 create.test             Creates a new empty test case
Default target: citrus.run.tests

You can now create new test cases or execute test cases by calling the respective
target, for example:

> ant citrus.run.tests

The sample test case contains a simple workflow to handle a TravelBookingRequest with
two flight bookings. See the log output for detailed information about the test progress and
create your own test case to get in touch with Citrus.

Have fun with it!

The Citrus Team