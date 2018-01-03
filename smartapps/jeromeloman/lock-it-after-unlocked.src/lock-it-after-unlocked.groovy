/**
 *  Copyright 2015 SmartThings
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
 *  Lock It When I Leave
 *
 *  Author: SmartThings
 *  Date: 2013-02-11
 */

definition(
    name: "Lock It after unlocked",
    namespace: "jeromeloman",
    author: "Jerome Loman",
    description: "Locks a deadbolt or lever lock when unlocked after some time.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
   // oauth: true
)
preferences {
    section("lock this lock") {
        input "thelock", "capability.lock", required: true//, multiple: true
    }
    section("lock after minutes:") {
        input "minutes", "number", required: true, title: "Minutes?"
    }
}

def testMinutesMultiplier()
{
	return 60
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(thelock, "lock", lockHandler)
    subscribe(location, "mode", changedLocationMode)
}

def changedLocationMode(evt) {
    if (evt.value == "Away")
    {
    	log.debug "eventMode Away"
        if(lock.currentState("lock").value=="unlocked")
        {
        	log.debug "lock is unlocked: let's lock it"
			thelock.lock()
        }
    }
}

def lockHandler(evt) {
	if(evt.value == "locked")
    {
    	log.debug "event door locked so unschedule"
    	unschedule(lockInMinutes)
    }
    else if (evt.value == "unlocked")
    {
    	log.debug "event unlocked so schedule lock"
    	runIn(testMinutesMultiplier() * minutes, lockInMinutes)
    }
    else
    {
    sendPush("Your door lock: ${thelock.label} appears to be jammed")
    }
    
    
}

def lockCheckIfStillUnlocked()
{
	def lockState = lock.currentState("lock")
    if (lockState.value == "unlocked") {
        // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - lockState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * testMinutesMultiplier() * minutes// if elapsed is still greater than last recorded then someting is wrong.
        if (elapsed >= threshold) {
            log.debug "Your door lock: ${thelock.label} appears to be jammed"
            sendPush("Your door lock: ${thelock.label} appears to be jammed")
        }
       }
}
def lockInMinutes()
{
	//thelock.each {lock->
    def lock = thelock
	def lockState = lock.currentState("lock")
    if (lockState.value == "unlocked") {
        // get the time elapsed between now and when the motion reported inactive
        def elapsed = now() - lockState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * testMinutesMultiplier() * minutes

        if (elapsed >= threshold) {
            log.debug "lock ${lock}"
            lock.lock()
        } else {
            log.debug "${lock}  state has been modified within ($elapsed ms):  doing nothing"
            runIn(testMinutesMultiplier() * minutes, lockInMinutes,  [overwrite: false])
        }
    } else {
        log.debug "${lock} is not unlocked"
    }
    //}
}