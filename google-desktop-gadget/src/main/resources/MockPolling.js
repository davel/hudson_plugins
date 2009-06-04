/*
 * This class is for testing various gui use cases
 *
 * For select a testable usecase for this class, use these view urls:
 *   http://www.twojob.com
 *   http://www.unreachable.com
 *   http://www.random.com
 *
 * Live urls to use for additional non-mocked testing:
 *   http://hudson.jboss.org/hudson/view/Infinispan
 *   http://hudson.jboss.org/hudson
 *   http://simile.mit.edu/hudson
 *
 */
function MockPolling(callback) {

	// constructor
	this.parentCallback = callback;

	this.unreachableToggle = true;

	// methods
	this.updateViewStatus = function(hudsonView) {
		var v;

		var mockType = hudsonView.url.split(".")[1];
		switch (mockType) {
		case 'twojob':
			v = this.setTwoJobStatus(hudsonView);
			break;
		case 'unreachable':
			v = this.setUnreachableViewStatus(hudsonView);
			break;
		case 'random':
			v = this.setRandomJobStatus(hudsonView);
			break;
		default:
			v = this.setRandomJobStatus(hudsonView);
			break;
		}

        this.parentCallback(v);
    }

	this.setTwoJobStatus = function(hudsonView) {

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
		if (this.unreachableToggle) {
			hudsonView.setNetworkStatus(404);
		} else {
			hudsonView.setNetworkStatus(200);
		}
		this.unreachableToggle = !this.unreachableToggle;

		return view;
	}

}