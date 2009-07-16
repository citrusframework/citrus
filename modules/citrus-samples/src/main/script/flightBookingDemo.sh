#!/bin/sh
JARS=`find lib ../dist ../lib -iname *.jar`
CP=`echo $JARS | sed 's/ /:/g'`
java -cp $CP com.consol.citrus.samples.flightbooking.FlightBookingDemo $*