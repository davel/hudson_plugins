package hudson.plugins.bruceschneier;

import hudson.model.Result;

public enum Style {

	BRUCE_LEE, GUNS, STALK;

	public static Style get(Result result) {
		Style style;
		if (Result.FAILURE.equals(result)) {
			style = STALK;
		} else if (Result.SUCCESS.equals(result)) {
			style = GUNS;
		} else {
			style = BRUCE_LEE;
		}
		return style;
	}
}
