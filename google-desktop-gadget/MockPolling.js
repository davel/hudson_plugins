function MockPolling(callback) {

	// constructor
	this.parentCallback = callback;

	// methods
	this.updateViewStatus = function(hudsonView) {
		// var v = this.setJobStatus(hudsonView);
		var v = this.setRandomJobStatus(hudsonView);
		// var v = this.setUnreachableViewStatus(hudsonView);

        this.parentCallback(v);
    }

	this.setJobStatus = function(hudsonView) {

		var v1j1 = new HudsonJob();
		v1j1.name = "Job-1";
		v1j1.color = "blue";
		v1j1.url = view.url + "/job-1.html";
		var v1j2 = new HudsonJob();
		v1j2.name = "Job-2";
		v1j2.color = "red";
		v1j2.url = view.url + "/job-2.html";

		v1Jobs = new Array();
		v1Jobs.push(v1j1);
		v1Jobs.push(v1j2);
		hudsonView.setJobs(v1Jobs);
		hudsonView.setNetworkStatus = 200;

		return hudsonView;
	}

	this.setRandomJobStatus = function(hudsonView) {

		var jobCount = Math.floor(Math.random()*20+1);
		var jobs = new Array();

		for (var i=0; i<= jobCount; i++) {
			var j = new HudsonJob();
			j.name = "Job-"+i;
			j.url = view.url + "/job-"+i+".html";
			var jobColorIndex = Math.floor(Math.random()*HudsonJob.statusColors.length);
			j.color = HudsonJob.statusColors[jobColorIndex];
			jobs.push(j);
		}

		hudsonView.setJobs(jobs);
		hudsonView.setNetworkStatus = 200;

		return hudsonView;
	}

	this.setUnreachableViewStatus = function(hudsonView) {
		hudsonView.setJobs([]);
		hudsonView.setNetworkStatus(404);
		return view;
	}

}