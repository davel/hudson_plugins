function HudsonView(url, poller) {

	this.id = Math.floor(Math.random()*1000+1);
	this.url = url;
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

	this.getColor = function() {

		if (this.networkStatus != 200) {
			return "warning";
		}

		var rollupColor = "blue";
		jobColors:
		for (jobIndex in this.jobs) {
			var job = this.jobs[jobIndex];
			switch (job.color) {
			case 'red':
				rollupColor = 'red';
				break jobColors;
			case 'red_anime':
				rollupColor = 'red_anime';
				break jobColors;
			case 'yellow':
				rollupColor = 'yellow';
				break;
			case 'yellow_anime':
				rollupColor = 'yellow_anime';
				break;
			default:
				break;
			}
		}
        return rollupColor;
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
	}
}