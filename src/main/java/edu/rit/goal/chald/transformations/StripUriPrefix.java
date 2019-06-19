package edu.rit.goal.chald.transformations;

public class StripUriPrefix implements IStringTransformation {

	@Override
	public String transform(String s) {
		return s.replaceAll("http\\:[a-zA-Z0-9/\\.\\:\\-\\_]+/", "");
	}

}
