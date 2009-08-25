#!/bin/sh
JARS=`find lib ../dist ../lib -iname *.jar`
CP=`echo $JARS | sed 's/ /:/g'`
java -cp $CP:src/citrus/resources com.consol.citrus.samples.flightbooking.FlightBookingDemo $*