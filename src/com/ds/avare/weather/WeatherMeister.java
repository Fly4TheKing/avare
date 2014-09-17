/*
Copyright (c) 2014, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare.weather;

import com.ds.avare.StorageService;
import com.ds.avare.place.Airport;
import com.ds.avare.place.Destination;
import com.ds.avare.place.Plan;

/***
 * Essentially a static class that handles the strings to interact with the Weathermeister 
 * web site.
 * @author Ron
 *
 */
public class WeatherMeister {
	
	// Some constant values
    static String WEATHERMEISTER_URL = "http://www.weathermeister.com/premium/";
    static String ROUTE_BRIEF        = "route_briefing.jsp?a=1&dt=30&tz=MINUTES_FROM_NOW&v=VFR&cw=50&c=default&dep=";
    static String AREA_BRIEF         = "area_briefing.jsp?&radius=50&c=default&base=";
    static String WAYPOINTS          = "&wpts=";
    static String WAYPOINT_SEP       = "-";
    static String DEST               = "&dest=";
        
    /***
     * Generate the web query that we use to get the weather briefing
     * @param service
     * @return Web query string to request
     */
    public static String generate(StorageService service) {

    	// We need to have a running valid service
		if(null == service) {
			return null;
		}
		
        //
        // Pull up the weathermeister interface using the following logic:
        //
        // 1) If we have an active flight plan, then pull up the plan page feeding 
        // the waypoints into it
        // 2) If we have an active destination, then pull up the area briefing using 
        // the destination as the target
        // 3) Pull up the area briefing using the nearest airport as the target
        //
        String webQuery = "";
        String airportID = "";

        Plan plan = service.getPlan();
        Destination dest = service.getDestination();
        int nearestNum = service.getArea().getAirportsNumber();
        if(nearestNum > 0) {
            Airport nearest = service.getArea().getAirport(0);
            airportID = nearest.getId();
        }

        // If a flight plan is defined and it's active
        if(null != plan && false != plan.isActive()) {
        	// How many points in this plan
      		int max = plan.getDestinationNumber();

      		// If there's only 1 point, then treat this as an area
      		// briefing.
      		if(1 == max) {
          		webQuery = WEATHERMEISTER_URL + AREA_BRIEF + adjustLeadingK(plan.getDestination(0).getID());
      		} else  {
      			// Build up a flight plan route query
	      		webQuery = WEATHERMEISTER_URL + ROUTE_BRIEF + adjustLeadingK(plan.getDestination(0).getID());
	      		
	      		// If more than 2 points, then there are intermediate waypoints
	      		if (max > 2) {
	      			webQuery += WAYPOINTS;

	      			// Add waypoints 
		      		for(int idx = 1; idx < max - 1; idx++) {
		      			webQuery += adjustLeadingK(plan.getDestination(idx).getID());
		      			if(idx < (max - 2)) {
		      				webQuery += WAYPOINT_SEP;
		      			}
		      		}
	      		}
	      		
	      		// Now set the destination of the plan into the query
	  			webQuery += DEST + adjustLeadingK(plan.getDestination(max - 1).getID());
      		}

      	// Do we have a destination set ?
        } else if(null != dest) {
      		webQuery = WEATHERMEISTER_URL + AREA_BRIEF + adjustLeadingK(dest.getID());

     	// Otherwise, just get an area query based upon where we are
        } else {
        	webQuery = WEATHERMEISTER_URL + AREA_BRIEF + adjustLeadingK(airportID);
        }
        
        // All done, return what we have built.
        return webQuery;
	}

    /***
     * Return a string adjusted to handle prefixing the "K" to the airport
     * identifier
     * @param airportID
     * @return the new 4 character identifier if applicable
     */
    static String adjustLeadingK(String airportID)
    {
    	// If the ID is 3 chars in length and ALL ALPHABETIC, then prefix the
    	// K to it and return
    	// All other cases just return with what was passed to us.
    	if(airportID.length() == 3) {
    		for(int idx = 0; idx < 3; idx++) {
    			if(true == Character.isDigit(airportID.charAt(idx))) {
    				return airportID;
    			}
    		}
    		return "K" + airportID;
    	}
    	return airportID;
    }
}
