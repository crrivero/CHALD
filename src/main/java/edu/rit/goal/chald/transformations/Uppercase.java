package edu.rit.goal.chald.transformations;

public class Uppercase implements IStringTransformation {

	@Override
	public String transform(String s) {
		return s.toUpperCase();
	}

}
