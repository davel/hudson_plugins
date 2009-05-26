function NetworkPolling(callback) {

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

	var updateFailCount = 0;
	var httpRequest;

	// constructor
	this.parentCallback = callback;

	// methods
	this.updateViewStatus = function(view) {
		var v = this.updateNetworkStatus(view);
        this.parentCallback(v);
    }

	/**
	 * send request to hudson url using ajax
	 * 
	 * @param {String} url The url to hudson dashboard
	 */ 
	function updateNetworkStatus() {
		// make sure we don't get a cached request by changing the url every poll
		var apiUrl = hudsonUrl + "api/json" + "?noCache="+Math.random();
		httpRequest = new XMLHttpRequest();
		httpRequest.open("GET", apiUrl, true);
		httpRequest.onreadystatechange = parseJSONResponse;
		httpRequest.send(null);
	}

	function parseJSONResponse() {
	  if (httpRequest.readyState == 4) {
		var nextRefreshTime = 2 * 60 * 1000;  // in error cases try after 2 min
		if (httpRequest.status == 200) {
		  try {

			// clear the old results
			jobs = [];
			var jsonObj = eval("(" + httpRequest.responseText + ")");
			var viewName = jsonObj.name;
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
		  hudsonViewJobList1.removeAllElements();
		  contentDiv.visible = false;
		  errorDiv.visible = true;
		}
	}
}
