function NetworkPolling(callback) {

	// constructor
	this.parentCallback = callback;

	// methods
	this.updateViewStatus = function(hudsonView) {

		// make sure we don't get a cached request by changing the url every poll
		var apiUrl = hudsonView.url + "/api/json" + "?noCache="+Math.random();

		var _this = this;

		var httpRequest = new XMLHttpRequest();
		httpRequest.open("GET", apiUrl, true);
		httpRequest.onreadystatechange = function() {
			if (httpRequest.readyState == 4) {
				if (httpRequest.status == 200) {
					_this.parseJSONResponse(hudsonView, httpRequest.responseText);
				} else {
					// only try once here, gadget will retry after normal polling period
					_this.handleErrorCode(hudsonView, httpRequest.status);
				}
				// httpRequest = null;
			}
		};
		httpRequest.send();
	}

	/**
	 * Parse response from hudson url
	 * 
	 * @param {String} url The url to hudson dashboard
	 */ 
	this.parseJSONResponse = function(hudsonView, jsonResponse) {

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
			// TODO: calculate rollup status color
			hudsonView.color = "blue";

		} catch(e) {
			hudsonView.setNetworkStatus = "unparseable";
		}

		this.parentCallback(hudsonView);
	}

	this.handleErrorCode = function(hudsonView, status) {
		hudsonView.setJobs([]);
		hudsonView.setNetworkStatus(status);
		this.parentCallback(hudsonView);
	}
}
