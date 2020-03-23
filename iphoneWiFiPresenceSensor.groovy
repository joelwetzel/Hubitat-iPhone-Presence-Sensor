/**
 *  iPhone WiFi Presence Sensor
 *
 *  Copyright 2019 Joel Wetzel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

import groovy.json.*
	
metadata {
	definition (name: "iPhone WiFi Presence Sensor", namespace: "joelwetzel", author: "Joel Wetzel") {
		capability "Refresh"
		capability "Sensor"
        capability "Presence Sensor"
	}

	preferences {
		section {
			input (
				type: "string",
				name: "ipAddress",
				title: "iPhone IP Address",
				required: true				
			)
			input (
				type: "number",
				name: "timeoutMinutes",
				title: "Timeout Minutes",
				description: "Approximate number of minutes without a response before deciding the device is away/offline.",
				required: true,
				defaultValue: 3
			)
			input (
				type: "bool",
				name: "enableDebugLogging",
				title: "Enable Debug Logging?",
				required: true,
				defaultValue: true
			)
            input (
				type: "bool",
				name: "enableDevice",
				title: "Enable Device?",
				required: true,
				defaultValue: true
			)
		}
	}
}


def log(msg) {
	if (enableDebugLogging) {
		log.debug(msg)	
	}
}


def installed () {
	log.info "${device.displayName}.installed()"
    updated()
}


def updated () {
	log.info "${device.displayName}.updated()"
    
    state.tryCount = 0
    
	unschedule()
    
    if (enableDevice) {
        runEvery1Minute(refresh)		// Option 1: test it every minute.  Have a 10 second timeout on the requests.
        state.triesPerMinute = 1

	//schedule("*/15 * * * * ? *", refresh)    // Option 2: run every 15 seconds, but now we have a 10 second timeout on the requests.
        //state.triesPerMinute = 4
    }
    
    runIn(2, refresh)				// But test it once, right after we install or update it too.
}


def refresh() {
	log "${device.displayName}.refresh()"

	state.tryCount = state.tryCount + 1
    
    if ((state.tryCount / state.triesPerMinute) > (timeoutMinutes < 1 ? 1 : timeoutMinutes) && device.currentValue('presence') != "not present") {
        def descriptionText = "${device.displayName} is OFFLINE";
        log descriptionText
        sendEvent(name: "presence", value: "not present", linkText: deviceName, descriptionText: descriptionText)
    }
    
	if (ipAddress == null || ipAddress.size() == 0) {
		return
	}
	
	asynchttpGet("httpGetCallback", [
		uri: "http://${ipAddress}/",
        timeout: 10
	]);
}


def httpGetCallback(response, data) {
	log "${device.displayName}: httpGetCallback(${groovy.json.JsonOutput.toJson(response)}, data)"
	
	if (response != null && response.status == 408 && response.errorMessage.contains("Connection refused")) {
		state.tryCount = 0
		
		if (device.currentValue('presence') != "present") {
			def descriptionText = "${device.displayName} is ONLINE";
			log descriptionText
			sendEvent(name: "presence", value: "present", linkText: deviceName, descriptionText: descriptionText)
		}
	}
}



