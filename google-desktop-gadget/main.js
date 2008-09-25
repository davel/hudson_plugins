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

var hudsonUrl = DEFAULT_HUDSON_URL;
var pollingIntervalMinutes = DEFAULT_POLLING_INTERVAL_MINUTES;
var updateFailCount = 0;
var httpRequest;
var strurls = "";				// the urls as a single string
var hudsonUrls = new Array();	// array of urls


function view_onOpen() {
    initializeStoredOptions();

    // load urls setting from data base
    strurls = options.getValue("hudsonUrlsProp");
    if (strurls != "") {
      // split urls between each comma
      hudsonUrls = strurls.split(",");
      hudsonUrl = hudsonUrls[0]; // @TODO: loop thru all urls
      updateStatus();
    }
}

function initializeStoredOptions() {
  options.putDefaultValue('hudsonUrlsProp', DEFAULT_HUDSON_URL);
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
  jobList.removeAllElements();

  hudsonUrl = options.getValue('hudsonUrlsProp');
  pollingIntervalMinutes = options.getValue('intervalMinutesProp');

  updateStatus();
}

/**
 * delete all previous jobs and statuses and recreate with latest jobs and statuses
 */ 
function createElements() {
  errorDiv.visible = false;
  contentDiv.visible = true;

  jobList.removeAllElements();
  for (var i=0; i<jobs.length;++i) {
    var job = jobs[i];
    var jobLink = "<a width='120' height='16' x='0' href='" + job.url + "'>" + job.name + "</a>";
    var jobImg = "<img name='" + job.name + "Img' width='16' height='16' x='130' src='images/" + job.color + ".gif'/>";
    jobList.appendElement("<item name='"+job.name+"' height='20'>" + jobLink + jobImg + "</item>");
  }

  contentDiv.height = jobs.length * 20;
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
 * send request to hudson url using ajax
 * 
 * @param {String} url The url to hudson dashboard
 */ 
function updateStatus() {

  // make sure we don't get a cached request by changing the url every poll
  var apiUrl = hudsonUrl + "api/json" + "?noCache="+Math.random();
  httpRequest = new XMLHttpRequest();
  httpRequest.open("GET", apiUrl, true);
  httpRequest.onreadystatechange = parseJSON;
  httpRequest.send(null);

  setViewPollTime();

  // make sure updateStatus gets called again
  registerUpdateStatus();
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

function parseJSON() {
  if (httpRequest.readyState == 4) {
    var nextRefreshTime = 2 * 60 * 1000;  // in error cases try after 2 min
    if (httpRequest.status == 200) {
      try {

        // clear the old results
        jobs = [];
        var jsonObj = eval("(" + httpRequest.responseText + ")");
        jobs = jsonObj.jobs;
        updateFailCount = 0;  // successfully parsed stuff
        createElements();
      } catch(e) {
        jobs = null;
        updateFailCount++;
      }
    } else {
      updateFailCount++;
    }

    httpRequest = null;

    if (updateFailCount > 3) {
      debug.trace("network failure");
      // too many failures so stop retrying
      jobList.removeAllElements();
      contentDiv.visible = false;
      errorDiv.visible = true;
    }
  }
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
