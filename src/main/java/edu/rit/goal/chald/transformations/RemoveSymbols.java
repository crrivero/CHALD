package edu.rit.goal.chald.transformations;

public class RemoveSymbols implements IStringTransformation {

	@Override
	public String transform(String s) {
		return s.replaceAll("[^a-zA-Z0-9\\s]", "");
	}

}
