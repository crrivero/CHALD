package edu.rit.goal.chald.transformations;

public class Lowercase implements IStringTransformation {

	@Override
	public String transform(String s) {
		return s.toLowerCase();
	}

}
