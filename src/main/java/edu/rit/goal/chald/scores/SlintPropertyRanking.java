package edu.rit.goal.chald.scores;

import java.util.Map;

import org.apache.jena.rdf.model.Property;

public class SlintPropertyRanking extends PropertyRanking {
	private Map<Property, Double> coverageScores, discriminationScores;
	
	public SlintPropertyRanking(Map<Property, Double> coverageScores, Map<Property, Double> discriminationScores) {
		super();
		this.coverageScores = coverageScores;
		this.discriminationScores = discriminationScores;
	}

	@Override
	public double compute(Property p) {
		return Slint.hmean(coverageScores.get(p), discriminationScores.get(p));
	}

}
