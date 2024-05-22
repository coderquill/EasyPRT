package edu.cmu.cs.prt;

import java.util.List;

record TrueTimePattern(String id, double length, RouteDirection routeDirection, List<TrueTimeWaypoint> waypoints) {
}
