function HudsonView(url, poller) {

	this.id = Math.floor(Math.random()*100+1);
	this.url = url;
	this.color = "";
	this.jobs = new Array();
	this.expanded = true;
	this.networkStatus = 200;
	this.poller = poller;

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

	this.getPoller = function() {
		return this.poller;
	}

	this.setPoller = function(poller) {
		this.poller = poller;
		poller.setView(this);
	}

	this.updateViewStatus = function() {
		poller.updateViewStatus(this);
	}

	this.getNetworkStatus = function() {
		return this.networkStatus;
	}

	this.setNetworkStatus = function(status) {
		this.networkStatus = status;
		if (status != 200) {
			this.color = "warning";
		}
	}
}