package hudson.plugins.seleniumGrails;

public class SeleniumGrailsResultBean {

	private int passed;
	private int failed;

	private String[] details;


	public int getPassed(){ return passed;}
	public void setPassed(int passed){this.passed = passed;}

	public int getFailed(){return failed;}
	public void setFailed(int failed){this.failed = failed;}

	public String[] getDetails(){return details;}
	public void setDetails(String[] details){this.details = details;}

}
