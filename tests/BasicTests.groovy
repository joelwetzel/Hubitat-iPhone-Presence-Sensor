package joelwetzel.iphone_presence_sensor.tests

import me.biocomp.hubitat_ci.device.MockAsyncHttpBuilder
import me.biocomp.hubitat_ci.device.helpers.DeviceHelper
import me.biocomp.hubitat_ci.util.device_fixtures.DeviceScriptFixtureFactory
import me.biocomp.hubitat_ci.validation.Flags

import spock.lang.Specification

/**
* Basic tests for iphoneWiFiPresenceSensor.groovy
*/
class BasicTests extends Specification {
    DeviceHelper deviceHelper = new DeviceHelper()
    def device

    def setup() {
        device = deviceHelper.createDevice(
            scriptFilename: "iphoneWiFiPresenceSensor.groovy",
            userSettingValues: [
                ipAddress: "192.168.1.100",
                timeoutMinutes: 3,
                enableDebugLogging: true,
                enableDevice: true
            ],
            validationFlags: [Flags.DontValidateDefinition]
        )
    }

    void "installed() calls updated()"() {
        when:
        device.installed()

        then:
        device.log.infoMessages.any { it.contains("installed()") }
        device.log.infoMessages.any { it.contains("updated()") }
    }

    void "updated() initializes state and schedules refresh"() {
        when:
        device.updated()

        then:
        device.state.tryCount == 0
        device.log.infoMessages.any { it.contains("updated()") }
    }

    void "ensureStateVariables() sets default state values"() {
        when:
        device.state.triesPerMinute = null
        device.ensureStateVariables()

        then:
        device.state.triesPerMinute == 1
    }

    void "refresh() logs device name"() {
        when:
        device.refresh()

        then:
        device.log.debugMessages.any { it.contains("refresh()") }
    }

    void "refresh() increments tryCount"() {
        given:
        device.state.tryCount = 0

        when:
        device.refresh()

        then:
        device.state.tryCount == 1
    }

    void "httpGetCallback() with connection refused marks device as present"() {
        given:
        device.sendEvent(name: "presence", value: "not present")
        def response = [
            status: 408,
            errorMessage: "Connection refused"
        ]

        when:
        device.httpGetCallback(response, null)

        then:
        device.state.tryCount == 0
        device.currentValue('presence') == "present"
    }

    void "refresh() marks device as not present after timeout"() {
        given:
        device.state.tryCount = 5
        device.state.triesPerMinute = 1
        device.sendEvent(name: "presence", value: "present")

        when:
        device.refresh()

        then:
        device.currentValue('presence') == "not present"
    }
}
