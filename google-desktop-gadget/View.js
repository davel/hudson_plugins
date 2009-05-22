function View(url) {

	this.url = url;
	this.color = "";
	this.jobs = new Array();

	// methods
	this.setJobs = function(jobs) {
        this.jobs = jobs;
    }

	this.getJobs = function() {
        return this.jobs;
    }

}