# iPhone WiFi Presence Sensor for Hubitat
A virtual presence sensor for Hubitat that checks if an iPhone is on the WiFi network.

Note: iPhones can put their WiFi to sleep, so you should not use this as your only way of detecting presence.  However, it can be used to augment other presence sensors.  Interpret results this way:

- If this sensor shows "present", the iPhone is DEFINITELY present.
- If this sensor shows "not present", the iPhone may or may not be present.  We don't know.

## Installation
1. Open your Hubitat web page
2. Go to the "Drivers Code" page
3. Click "+ New Driver"
4. Paste in the contents of iPhoneWiFiPresenceSensor.groovy
5. Click "Save"
6. Go to the "Devices" page
7. Click "+ Add Virtual Device"
8. Set "Device Name" and "Device Network Id" to anything you like.  Set "Type" to "iPhone Wifi Presence Sensor".
9. Click "Save Device"
10. On the device list, click the name of your new sensor
11. Set "IP Address" to the local static IP address of the iPhone.
12. Click "Save Preferences"
