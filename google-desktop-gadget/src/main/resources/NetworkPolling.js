/*
 * Class that gets the jobs statuses for a view and returns the view model
 * to the main class for gui rendering
 *
 */
function NetworkPolling(callback) {

	// constructor
	this.parentCallback = callback;

	/**
	 * Make the request to hudson to get all job statuses for a view
	 * 
	 * @param {HudsonView} view of a hudson dashboard containing the url
	 */ 
	this.updateViewStatus = function(hudsonView) {

		// make sure we don't get a cached request by changing the url every poll
		var apiUrl = hudsonView.url + "/api/json" + "?noCache="+Math.random();

		var _this = this;

		var httpRequest = new XMLHttpRequest();
		httpRequest.open("GET", apiUrl, true);
		httpRequest.onreadystatechange = function() {
			if (httpRequest.readyState == 4) {
				hudsonView.setNetworkStatus(httpRequest.status);
				if (httpRequest.status == 200) {
					_this.handleOkCode(hudsonView, httpRequest.responseText);
				} else {
					// only try once here, gadget will retry after normal polling period
					_this.handleErrorCode(hudsonView);
				}
				// httpRequest = null;
			}
		};
		httpRequest.send();
	}

	/**
	 * Parse json response from hudson url
	 * 
	 * @param {HudsonView} hudsonView, of a hudson dashboard containing the url
	 * @param {String} jsonResponse, json response text
	 */ 
	this.handleOkCode = function(hudsonView, jsonResponse) {

		try {
			var jsonObj = eval("(" + jsonResponse + ")");
			var viewName = jsonObj.name;
			jobs = jsonObj.jobs;

			var viewJobs = new Array();

			for (var i=0; i<jobs.length;++i) {
				var job = jobs[i];
				var hudsonJob = new HudsonJob();
				hudsonJob.name = job.name;
				hudsonJob.url = job.url;
				hudsonJob.color = job.color;
				viewJobs.push(hudsonJob);
			}

			hudsonView.setJobs(viewJobs);

		} catch(e) {
			hudsonView.setNetworkStatus = "unparseable";
		}

		this.parentCallback(hudsonView);
	}

	/**
	 * Handle a non-200 response code.  This could be 4xx or 5xx or a IE
	 * specific code such as 12150
	 * 
	 * @param {HudsonView} hudsonView, of a hudson dashboard containing the url
	 */ 
	this.handleErrorCode = function(hudsonView) {
		hudsonView.setJobs([]);
		this.parentCallback(hudsonView);
	}
}
