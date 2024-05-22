# Pittsburgh Regional Transit Java API

A Java API for getting information about the Pittsburgh Regional Transit (PRT) system. Provides information related to stops, routes, predicted arrivals, etc. for both buses and light rail.

Uses the [TrueTime web API](http://truetime.rideprt.org/home) to get real-time information about vehicles in the system, and information about the schedule and stops from the [GTFS dataset](https://www.rideprt.org/business-center/developer-resources/) released by PRT.

## Documentation
To view the full JavaDoc, download `pgh-prt-truetime/javadoc` and open `pgh-prt-truetime/javadoc/index.html` with a web browser.

## Directory Structure
### history-parsing
Contains code to retrieve real-time data about the arrival of vehicles at stops, to be later used for historical analysis.
### pgh-prt-truetime
Contains the Java API to get information about the PRT system. Build instructions and documentation for the Java API are contained in this directory.
### schedule-parsing
Contains code to parse the scheduled trips and scheduled arrival times at stops from the GTFS data files released by PRT.
