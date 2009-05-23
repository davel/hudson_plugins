function MockPolling(callback) {

	// private variables
	var value = 0;

	// constructor
	this.parentCallback = callback;

	// methods
	this.updateViewStatus = function(view) {
		var v = this.setJobStatus(view);
        this.parentCallback(v);
    }

	this.setJobStatus = function(view) {

		var v1j1 = new HudsonJob();
		v1j1.name = "Job 1";
		v1j1.color = "blue";
		v1j1.url = view.url + "/job1.html";
		var v1j2 = new HudsonJob();
		v1j2.name = "Job 2";
		v1j2.color = "red";
		v1j2.url = view.url + "/job2.html";

		v1Jobs = new Array();
		v1Jobs.push(v1j1);
		v1Jobs.push(v1j2);
		view.setJobs(v1Jobs);
		view.color = "blue";

		return view;
	}

}