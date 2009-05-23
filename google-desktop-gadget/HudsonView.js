function HudsonView(url) {

	this.id = Math.floor(Math.random()*100+1);
	this.url = url;
	this.color = "";
	this.jobs = new Array();
	this.expanded = new Boolean(false);

	// methods
	this.setJobs = function(jobs) {
        this.jobs = jobs;
    }

	this.getJobs = function() {
        return this.jobs;
    }

	this.toogleExpanded = function() {
		this.expanded = !this.expanded;
	}

}