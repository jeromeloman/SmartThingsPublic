/**
 *  Onkyo IP Control Device Type for SmartThings
 *  JEROME LOMAN
 *  originally based on Allan Klein (@allanak) 
 *  Originally based on: Mike Maxwell's code
 *
 *  Usage:
 *  1. Be sure you have enabled control of the receiver via the network under the settings on your receiver.
 *  2. Add this code as a device handler in the SmartThings IDE
 *  3. Create a device using OnkyoIP as the device handler using a hexadecimal representation of IP:port as the device network ID value
 *  For example, a receiver at 192.168.1.222:60128 would have a device network ID of C0A801DE:EAE0
 *  Note: Port 60128 is the default Onkyo eISCP port so you shouldn't need to change anything after the colon
 *  4. Enjoy the new functionality of the SmartThings app
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
 * Some future commands that might be useful to incorporate reading:
 * Power Status: PWRQSTN
 * Input Selected: SLIQSTN
 * Volume Level: MVLQSTN (value in hex)
 * Artist Name: NATQSTN
 * Track Name: NTIQSTN
 * Zone 2 Mute: ZMTQSTN
 * Zone 2 Volume: ZVLQSTN
 * Zone 2 Input Selected: SLZQSTN
 * ISCP commands were found at https://github.com/miracle2k/onkyo-eiscp/blob/master/eiscp-commands.yaml
 */

metadata {
	definition (name: "onkyoIP", namespace: "jeromeloman", author: "Jerome Loman") {
    capability "Actuator" //device can change things
	capability "Switch" //on off
    capability "Switch Level"
    attribute "source", "string"//"enum", ["tv", "cd", "stream", "aux"]
    attribute "muteState", "string"//"enum", ["mute", "unmute"]
    command "tv"
    command "stream"
    command "cd"
	command "net"
    command "mute"
    command "unmute"
	command "makeNetworkId", ["string","string"]
	}

simulator {
		// TODO: define status and reply messages here
	}

tiles(scale:2) {
		standardTile("source", "device.source", width: 2, height: 2, decoration: "flat") {
        	state "tv", label: 'source: tv', icon:"st.Electronics.electronics18", backgroundColor:"#79b821"
            state "stream", label: 'source: stream', icon:"st.Electronics.electronics6", backgroundColor:"#79b821"
            state "cd", label: 'source: cd', icon:"st.Electronics.electronics4", backgroundColor:"#79b821"
            state "net", label: 'source: net',icon:"st.Electronics.electronics2", backgroundColor:"#79b821"
   		}
		standardTile("switch", "device.switch", width: 4, height: 4) {
        	state "off", label: '${name}', action: "switch.on", icon: "st.Electronics.electronics19", backgroundColor: "#ffffff"
        	state "on", label: '${name}', action: "switch.off", icon: "st.Electronics.electronics19", backgroundColor: "#79b821"
   		}
        standardTile("mutestate", "device.muteState", width: 2, height: 2) {
			state "unmuted", label:'', action:"mute", icon:"st.custom.sonos.unmuted", backgroundColor:"#79b821", nextState:"muted"
			state "muted", label:'', action:"unmute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff", nextState:"unmuted"
        }
        valueTile("level", "device.level", width: 2, height: 1) {
			state "val", label:'${currentValue}'
        }
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 4, range:"(0..70)") {
			state "level", label:'${currentValue}', action:"switch level.setLevel", backgroundColor:"#ffffff"
		}
        standardTile("tv", "device.switch", width: 2, height: 2){
        	state "tv", label: 'tv', action: "tv", icon:"st.Electronics.electronics18"
        }
        standardTile("stream", "device.switch", width: 2, height: 2){
        	state "stream", label: 'stream', action: "stream", icon:"st.Electronics.electronics8"
        }
        standardTile("cd", "device.switch", width: 2, height: 2){
        	state "cd", label: 'cd', action: "cd", icon:"st.Electronics.electronics4"
        }
        standardTile("net", "device.switch", width: 2, height: 2){
        	state "net", label: 'net', action: "net", icon:"st.Electronics.electronics2"
        }
        /*   Commenting this out as it doesn't work yet     
        valueTile("currentSong", "device.trackDescription", inactiveLabel: true, height:1, width:3, decoration: "flat") {
		state "default", label:'${currentValue}', backgroundColor:"#ffffff"
		}
	*/
	}
    main "switch"
    details(["source","switch", "mutestate", "level", "levelSliderControl","tv","cd","stream","net"])
}
/*
def refresh() {
    log.debug "Refresh: pwr status"
    def msg = getEiscpMessage("PWRQSTN")
	//def ha = new physicalgraph.device.HubAction(msg, physicalgraph.device.Protocol.LAN, null, [callback: calledBackHandler] )
   	//sendHubCommand(new physicalgraph.device.HubAction(msg, physicalgraph.device.Protocol.LAN, null, [callback: calledBackHandler]))
	//ha
}
// the below calledBackHandler() is triggered when the device responds to the sendHubCommand()

void calledBackHandler(physicalgraph.device.HubResponse hubResponse) {
    log.debug "Entered calledBackHandler()..."
    def body = hubResponse.description
   
    log.debug "body in calledBackHandler() is: ${body}"
}
*/

def installed() {
	sendEvent(name: "source", value: "tv")
    sendEvent(name: "mutestate", value: "unmuted")
	//create child virtual device for voice commands
    //create momentary tile for TV, CD and STREAM input
    def a=addChildDevice("smartthings","Momentary Button Tile", "${device.deviceNetworkId}-tv", null, [completedSetup: true, label: "Onkyo TV", isComponent: true, componentName: "onkyo_tv", componentLabel: "Onkyo TV"])
    a.refresh()
    def d=addChildDevice("smartthings","Momentary Button Tile", "${device.deviceNetworkId}-cd", null, [completedSetup: true, label: "Onkyo CD", isComponent: true, componentName: "onkyo_cd", componentLabel: "Onkyo Stream"])
    d.refresh()
    def da = addChildDevice("smartthings","Momentary Button Tile", "${device.deviceNetworkId}-stream", null, [completedSetup: true, label: "Onkyo STREAM", isComponent: true, componentName: "onkyo_stream", componentLabel: "Onkyo Stream"])
    da.refresh()
    runIn(1, subscribetoevents)
    runIn(1, initializeLevel)
}
def initializeLevel()
{
	sendEvent(name:"level", value:36)
    sendEvent(name:"switch.setLevel", value:36)
}
def subscribetoevents()
{
	getChildDevices().each {device->
    	log.debug("subscribe device: ${device.deviceNetworkId}")
    	subscribe(childDevice, "momentary", momentarySwitchHandler)
    }
}

def poll() {
	//refresh()
}

def momentarySwitchHandler(evt)
{
	if(evt.getDevice().deviceNetworkId=="${device.deviceNetworkId}-cd")
    {
    	cd()
    }
    else if(evt.getDevice().deviceNetworkId=="${device.deviceNetworkId}-tv")
    {
    	tv()
    }
    else if(evt.getDevice().deviceNetworkId=="${device.deviceNetworkId}-stream")
    {
    	stream()
    }
}



// parse events into attributes
def parse(description) {
	log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)
    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
    
    //log.debug "Parsing '${msg}'"
    //log.debug "Parsing '${headersAsString}'"
    //log.debug "End parsing"
}
//device.deviceNetworkId should be writeable now..., and its not...
def makeNetworkId(ipaddr, port) { 
	String hexIp = ipaddr.tokenize('.').collect {String.format('%02X', it.toInteger()) }.join() 
	String hexPort = String.format('%04X', port.toInteger()) 
	log.debug "The target device is configured as: ${hexIp}:${hexPort}" 
	return "${hexIp}:${hexPort}" 
	}
def updated() {
	//device.deviceNetworkId = makeNetworkId(settings.deviceIP,settings.devicePort)	
	}
def mute(){
	log.debug "Muting receiver"
	sendEvent(name: "muteState", value: "muted")
	def msg = getEiscpMessage("AMT01")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN )
	return ha
	}

def unmute(){
	log.debug "Unmuting receiver"
	sendEvent(name: "muteState", value: "unmuted")
	def msg = getEiscpMessage("AMT00")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN )
	return ha
	}

def setLevel(vol){
	log.debug "Setting volume level $vol"
	if (vol < 0) vol = 0
	else if( vol > 70) vol = 70
	
    sendEvent(name:"level", value:vol)
    sendEvent(name:"switch.setLevel", value:vol)
    
    String volhex = vol.bytes.encodeHex()
    // Strip the first six zeroes of the hex encoded value because we send volume as 2 digit hex
    volhex = volhex.replaceFirst("\\u0030{6}","")
    log.debug "Converted volume $vol into hex: $volhex"
    def msg = getEiscpMessage("MVL${volhex}")
    log.debug "Setting volume to MVL${volhex}"
    def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN )
    return ha
		
	}

def on() {
	log.debug "Powering on receiver"
	sendEvent(name: "switch", value: "on")
	def msg = getEiscpMessage("PWR01")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}

def off() {
	log.debug "Powering off receiver"
	sendEvent(name: "switch", value: "off")
	def msg = getEiscpMessage("PWR00")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}
/*
def cable() {
	log.debug "Setting input to Cable"
	def msg = getEiscpMessage("SLI01")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}

def stb() {
	log.debug "Setting input to STB"
	def msg = getEiscpMessage("SLI02")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}

def pc() {
	log.debug "Setting input to PC"
	def msg = getEiscpMessage("SLI05")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}
*/
def tv() {
	log.debug "Setting input to TV"
    sendEvent(name: "source", value: "tv")
	def msg = getEiscpMessage("SLI12")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}

def stream() {
	log.debug "Setting input to STREAM"
    sendEvent(name: "source", value: "stream")
	def msg = getEiscpMessage("SLI11")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}

def cd() {
	log.debug "Setting input to CD"
    sendEvent(name: "source", value: "cd")
	def msg = getEiscpMessage("SLI23")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}

def net() {
	log.debug "Setting input to NET"
    sendEvent(name: "source", value: "net")
	def msg = getEiscpMessage("SLI2B")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	log.debug "Pressing play"
	def msg2 = getEiscpMessage("NSTP--")
	def ha2 = new physicalgraph.device.HubAction(msg2,physicalgraph.device.Protocol.LAN)    
	return ha
    return ha2
	}

def aux() {
	log.debug "Setting input to AUX"
    sendEvent(name: "source", value: "aux")
	def msg = getEiscpMessage("SLI03")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}
def z2on() {
	log.debug "Turning on Zone 2"
	def msg = getEiscpMessage("ZPW01")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}
def z2off() {
	log.debug "Turning off Zone 2"
	def msg = getEiscpMessage("ZPW00")
	def ha = new physicalgraph.device.HubAction(msg,physicalgraph.device.Protocol.LAN)
	return ha
	}


def getEiscpMessage(command){
	def sb = StringBuilder.newInstance()
	def eiscpDataSize = command.length() + 3  // this is the eISCP data size
	def eiscpMsgSize = eiscpDataSize + 1 + 16  // this is the size of the entire eISCP msg

	/* This is where I construct the entire message
        character by character. Each char is represented by a 2 disgit hex value */
	sb.append("ISCP")
	// the following are all in HEX representing one char

	// 4 char Big Endian Header
	sb.append((char)Integer.parseInt("00", 16))
	sb.append((char)Integer.parseInt("00", 16))
	sb.append((char)Integer.parseInt("00", 16))
	sb.append((char)Integer.parseInt("10", 16))

	// 4 char  Big Endian data size
	sb.append((char)Integer.parseInt("00", 16))
	sb.append((char)Integer.parseInt("00", 16))
	sb.append((char)Integer.parseInt("00", 16))
	// the official ISCP docs say this is supposed to be just the data size  (eiscpDataSize)
	// ** BUT **
	// It only works if you send the size of the entire Message size (eiscpMsgSize)
	// Changing eiscpMsgSize to eiscpDataSize for testing
	sb.append((char)Integer.parseInt(Integer.toHexString(eiscpDataSize), 16))
	//sb.append((char)Integer.parseInt(Integer.toHexString(eiscpMsgSize), 16))


	// eiscp_version = "01";
	sb.append((char)Integer.parseInt("01", 16))

	// 3 chars reserved = "00"+"00"+"00";
	sb.append((char)Integer.parseInt("00", 16))
	sb.append((char)Integer.parseInt("00", 16))
	sb.append((char)Integer.parseInt("00", 16))

	//  eISCP data
	// Start Character
	sb.append("!")

	// eISCP data - unittype char '1' is receiver
	sb.append("1")

	// eISCP data - 3 char command and param    ie PWR01
	sb.append(command)

	// msg end - this can be a few different cahrs depending on you receiver
	// my NR5008 works when I use  'EOF'
	//OD is CR
	//0A is LF
	/*
	[CR]			Carriage Return					ASCII Code 0x0D			
	[LF]			Line Feed						ASCII Code 0x0A			
	[EOF]			End of File						ASCII Code 0x1A			
	*/
	//works with cr or crlf
	sb.append((char)Integer.parseInt("0D", 16)) //cr
	//sb.append((char)Integer.parseInt("0A", 16))

	return sb.toString()
	}