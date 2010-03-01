@echo off

setlocal ENABLEDELAYEDEXPANSION

for /r ..\..\dist %%j IN (*.jar) do set CP=!CP!;%%j
for /r ..\..\lib %%j IN (*.jar) do set CP=!CP!;%%j
for /r ..\lib %%j IN (*.jar) do set CP=!CP!;%%j
for /r lib %%j IN (*.jar) do set CP=!CP!;%%j

@java -cp %CP%;src\citrus\resources com.consol.citrus.samples.flightbooking.FlightBookingDemo %1