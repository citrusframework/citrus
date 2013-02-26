#!/bin/sh
JARS=`find lib ../lib ../../lib ../../dist -name *.jar`
CP=`echo $JARS | sed 's/ /:/g'`
java -cp $CP:src/citrus/resources com.consol.citrus.samples.flightbooking.FlightBookingDemo $*