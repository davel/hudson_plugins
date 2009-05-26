﻿/*
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
var pollingIntervalMinutes = DEFAULT_POLLING_INTERVAL_MINUTES;

var hudsonViewUrls = "";			    // the urls as a single string
var hudsonViewList = new Array();	// array of urls
var hudsonViewData = new Array();	// hash of views and jobs

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
			hudsonViewData.push(new HudsonView(viewUrl));
		}
	}

	if (hudsonViewData.length > 0) {
		updateStatus();
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

	pollingIntervalMinutes = options.getValue("intervalMinutesProp");
    hudsonViewUrls = options.getValue("hudsonUrlsProp");

	// split urls between each comma
	hudsonViewList = hudsonViewUrls.split(",");
	hudsonViewData = [];
	for (viewUrlIndex in hudsonViewList) {
		var viewUrl = hudsonViewList[viewUrlIndex];
		hudsonViewData.push(new HudsonView(viewUrl));
	}

	if (hudsonViewData.length > 0) {
		updateStatus();
	}
}

/**
 * delete all previous jobs and statuses and recreate with latest jobs and statuses
 */ 
function renderView(updatedHudsonView) {

	if (hudsonViewData.length >= 1) {

		contentListbox.removeAllElements();

		// if view is defined, set the new job status info for view in view hash
		if (updatedHudsonView) {
			for (viewIndex in hudsonViewData) {
				var existingView = hudsonViewData[viewIndex];
				if (updatedHudsonView.url == existingView.url) {
					hudsonViewData[viewIndex] = updatedHudsonView;
					break;
				}
			}
		}

		var listboxY = 0;
		var listboxWidth = view.width - 10;
		var imgX = view.width - 30;

		// add the view as a list element header in the listbox
		for (viewIndex in hudsonViewData) {

			var viewToRender = hudsonViewData[viewIndex];

			var viewExpander = "<a width='20' height='16' x='0' onclick='toggleViewCollapse(" + viewToRender.id + ")'>[+]</a>";
			var viewLink = "<a width='100' height='16' x='20' href='" + view.url + "'>" + viewToRender.url +"</a>";
			var viewImg = "<img name='" + viewToRender.url + "Img' width='16' height='16' x='" + imgX + "' y='2' src='images/" + viewToRender.color + ".gif'/>";
			var header = contentListbox.appendElement("<item name='"+viewToRender.url+"' background='#AAAAAA'>" + viewExpander + viewLink + viewImg + "</item>");

			if (viewToRender.expanded) {
				var jobs = viewToRender.getJobs();

				// each job in the view is rendered as an item under the view element in the listbox
				for (jobIndex in jobs) {
					var job = jobs[jobIndex];
					var jobLink = "<a width='120' height='16' x='0' href='" + job.url + "'>" + job.name + "</a>";
					var jobImg = "<img name='" + job.name + "Img' width='16' height='16' x='" + imgX + "' y='2' src='images/" + job.color + ".gif'/>";
					contentListbox.appendElement("<item name='"+job.name+"' valign='center'>" + jobLink + jobImg + "</item>");
				}
				
			}
		}

	}	

}

function getViewById(id) {
	for (viewIndex in hudsonViewData) {
		var existingView = hudsonViewData[viewIndex];
		if (id == existingView.id) {
			return existingView;
		}
	}
}

function getViewCount() {
	var count = 0;
	for (viewIndex in hudsonViewData) {
		count += 1;
		var existingView = hudsonViewData[viewIndex];
		count += existingView.getJobs().length;
	}
	return count;
}

function toggleViewCollapse(viewId) {
	var hudsonView = getViewById(viewId);
	hudsonView.toogleExpanded();
	renderView(hudsonView);
}

function updateStatus() {
	setViewPollTime();
    if (hudsonViewData.length >= 1) {
		for (viewIndex in hudsonViewData) {
			var viewToUpdate = hudsonViewData[viewIndex];
			viewPoller.updateViewStatus(viewToUpdate);
		}
	}
	debug.trace('polling complete...');

	// make sure updateStatus gets called again
	registerUpdateStatus();
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

function setViewPollTime() {
	var currentTime = new Date();
	var hours = currentTime.getHours();
	var minutes = currentTime.getMinutes();
	if (minutes < 10) {
		minutes = "0" + minutes;
	}
	lastPollTime.value = hours + ":" + minutes;
}

function onOpenOptionsClick() {
    pluginHelper.ShowOptionsDialog();
} 

function sb_onchange() {
	var viewCount = getViewCount();

	contentListbox.height = Math.max(viewCount * contentListbox.itemHeight, contentDiv.height);
	contentScrollbar.max = contentListbox.height - contentDiv.height;
	if (contentListbox.height > (viewCount * contentListbox.itemHeight) ) {
		contentScrollbar.visible = false;
	} else {
		contentScrollbar.visible = true;
	}
	contentListbox.y = Math.min(0, -contentScrollbar.value)  
}

function view_onSize() {
	//Minimum SIze
	//120x60

	main.height = Math.max(view.height, 60);
	main.width = Math.max(view.width, 120);
	contentDiv.height = Math.max(main.height - 20, 20);
	contentDiv.width =  Math.max(main.width, 120);

	contentListbox.width = contentDiv.width - 10
	contentListbox.itemWidth = contentListbox.width;

	contentScrollbar.height = contentDiv.height;
	contentScrollbar.width = 10
	contentScrollbar.x = contentDiv.width - 10;
	contentScrollbar.max = Math.max(contentDiv.height);
	contentScrollbar.value = 0;
	sb_onchange();
	renderView();
}