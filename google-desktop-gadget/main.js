/*
Copyright (C) 2007 Olga Khylkouskaya

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/ 

/**
 * list of hudson jobs to check status
 * @type Array
 */ 
var jobs = new Array();

/**
 * Amount of time to wait until displaying 'timed out' message
 * @type Number
 */
var TIMEOUT_MS = 10000;  // 10 seconds

/**
 * How often to check for new messages, under normal circumstances
 * @type Number
 */
var BASE_UPDATE_INTERVAL_MS = 300000;  // 300 seconds
// var BASE_UPDATE_INTERVAL_MS = 20000;  // 20 seconds
// var BASE_UPDATE_INTERVAL_MS = 1000;  // 1 seconds

/**
 * How often to check for new job status, if we're offline
 * @type Number
 */
var CHECK_ONLINE_STATUS_INTERVAL_MS = 30000;  // 30 seconds

/**
 * Keeps track of the current updateStatus timer. Used to ensure that
 * we don't have multiple updateStatus timers at the same time.
 */
var g_updateStatusTimer = null;

/**
 * Default to a local Hudson instance on port 8080
 */
var DEFAULT_HUDSON_URL = "";
// var defaultHudsonUrl = "http://simile.mit.edu/hudson/";
// var defaultHudsonUrl = "http://build.sourcelabs.org/hudson/";
// var DEFAULT_HUDSON_URL = "http://localhost:8080/";

/**
 * Default to a polling every 5 minutes
 */
var DEFAULT_POLLING_INTERVAL_MINUTES = 5;
// var DEFAULT_POLLING_INTERVAL_MINUTES = 1;

/**
 * Default to a local Hudson instance on port 8080
 */
var DEFAULT_HUDSON_URL = "localhost";
// var defaultHudsonUrl = "http://simile.mit.edu/hudson/";
// var defaultHudsonUrl = "http://build.sourcelabs.org/hudson/";
// var DEFAULT_HUDSON_URL = "http://localhost:8080/";

var pollingIntervalMinutes = DEFAULT_POLLING_INTERVAL_MINUTES;
var updateFailCount = 0;

var hudsonViewUrls = "";			    // the urls as a single string
var hudsonViewList = new Array();	// array of urls
var hudsonViewData = new Array();	// hash of views and jobs

var contentDivElements = new Array();

var viewPoller = null;

function view_onOpen() {
    initializeStoredOptions();
	viewPoller = new MockPolling(renderView);

    // load urls setting from data base
	pollingIntervalMinutes = options.getValue("intervalMinutesProp");
    hudsonViewUrls = options.getValue("hudsonUrlsProp");
	
hudsonViewUrls = "http://mocktest/hudson,http://mocktest2/hudson2";

	// split urls between each comma
	hudsonViewList = hudsonViewUrls.split(",");

	for (viewUrlIndex in hudsonViewList) {
		var viewUrl = hudsonViewList[viewUrlIndex];
		if (viewUrl.length > 0) {
			hudsonViewData.push(new View(viewUrl));
		}
	}

	if (hudsonViewData.length> 0) {
		updateStatus();
	} else {
		
	}
}

function initializeStoredOptions() {
	options.putDefaultValue('hudsonUrlsProp', "");
	options.putDefaultValue('intervalMinutesProp', DEFAULT_POLLING_INTERVAL_MINUTES);
}

/**
 * force a refresh if hudson url or polling interval changes
 * 
 */
function onOptionChanged() {

	// stop any current timer
	if (g_updateStatusTimer) {
		view.clearTimeout(g_updateStatusTimer);
		g_updateStatusTimer = null;
	}

	// remove the "configure your hudson url" msg, if still there
	contentDiv.removeAllElements();

	pollingIntervalMinutes = options.getValue("intervalMinutesProp");
    hudsonViewUrls = options.getValue("hudsonUrlsProp");

	// split urls between each comma
	hudsonViewList = hudsonViewUrls.split(",");
	hudsonViewData = [];
	for (viewUrlIndex in hudsonViewList) {
		var viewUrl = hudsonViewList[viewUrlIndex];
		hudsonViewData.push(new View(viewUrl));
	}

	updateStatus();
}

/**
 * delete all previous jobs and statuses and recreate with latest jobs and statuses
 */ 
function renderView(view) {

	if (hudsonViewData.length >= 1) {

		errorDiv.visible = false;
		contentDiv.visible = true;
		contentDiv.removeAllElements();

		// set the new job status info for view in view hash
		for (viewIndex in hudsonViewData) {
			var existingView = hudsonViewData[viewIndex];
			if (view.url == existingView.url) {
				hudsonViewData[viewIndex] = view;
				break;
			}
		}

		var listboxY = 0;

		// each view is rendered as listbox
		for (viewIndex in hudsonViewData) {
			var view = hudsonViewData[viewIndex];
			var viewListbox = contentDiv.appendElement("<listbox height='85' name='"+view.getUrl+"' width='87%' y='"+listboxY+"' background='#CCCCCC' itemHeight='20' itemOverColor='#CCFFCC' itemSelectedColor='#99FF99' />");
			var viewLink = "<a width='120' height='16' x='0' href='" + view.url + "'>[+] " + view.url + "</a>";
			var viewImg = "<img name='" + view.name + "Img' width='16' height='16' x='130' src='images/" + view.color + ".gif'/>";
			var header = viewListbox.appendElement("<item name='"+view.getUrl+"' background='#AAAAAA'>" + viewLink + viewImg + "</item>");

			var jobs = view.getJobs();

			// each job in the view is rendered as an item in the view listbox
			for (jobIndex in jobs) {
				var job = jobs[jobIndex];
				var jobLink = "<a width='120' height='16' x='0' href='" + job.url + "'>" + job.name + "</a>";
				var jobImg = "<img name='" + job.name + "Img' width='16' height='16' x='130' src='images/" + job.color + ".gif'/>";
				viewListbox.appendElement("<item name='"+job.name+"'>" + jobLink + jobImg + "</item>");
			}

			listboxY += (jobs.length+1) * 20;
		}

	} else {
		errorDiv.visible = true;
		contentDiv.visible = false;
	}	

}

function updateStatus() {

	setViewPollTime();

    if (hudsonViewData.length >= 1) {
		for (viewIndex in hudsonViewData) {
			var viewToUpdate = hudsonViewData[viewIndex];
			viewPoller.updateViewStatus(viewToUpdate);
		}
    } else {
		errorDiv.visible = true;
		contentDiv.visible = false;
	}

	debug.trace('polling complete...');

	// make sure updateStatus gets called again
	// registerUpdateStatus();
}

function setViewPollTime() {
  var currentTime = new Date();
  var hours = currentTime.getHours();
  var minutes = currentTime.getMinutes();
  if (minutes < 10) {
    minutes = "0" + minutes;
  }
  lastPollTime.value = hours + ":" + minutes;
}

/**
 * Sets a timeout for updateStatus to be called depending on the
 * online/offline state
 */
function registerUpdateStatus() {
  var timeout;
  if (framework.system.network.online == false) {
    timeout = CHECK_ONLINE_STATUS_INTERVAL_MS;
  } else {
    timeout = pollingIntervalMinutes * 60000;
  }

  if (g_updateStatusTimer) {
    view.clearTimeout(g_updateStatusTimer);
    g_updateStatusTimer = null;
  }

  g_updateStatusTimer = view.setTimeout(updateStatus, timeout);
}

/**
 * change size of view
 * 
 * @param {Number} val on how much to change size, can be negative
 */ 
function changeViewSize(val) {
    var zoomHeight = view.height;
    view.resizable = 'true';
    var normalHeight = view.height;
    var koeff = zoomHeight/normalHeight;
    view.resizable = 'zoom';
    var newViewHeight = view.height + val*koeff;
    view.height = newViewHeight
}

function onOpenOptionsClick() {
    pluginHelper.ShowOptionsDialog();
} 

// clear all items
function clearContentDivElements(){
    contentDiv.height = 0;
    contentDiv.removeAllElements();
    contentDivElements = null;
}