package hudson.plugins.bruceschneier;

import hudson.model.Action;

public class ZeroDayAction implements Action {

	private Style style;
	private String fact;

	public ZeroDayAction(Style style, String fact) {
		super();
		this.style = style;
		this.fact = fact;
	}

	public String getDisplayName() {
		return "Bruce Schneier";
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return "bruceschneier";
	}

	public Style getStyle() {
		return style;
	}

	public String getFact() {
		return fact;
	}
}
