package edu.rit.goal.chald.scores;

import java.util.Map;

import org.apache.jena.rdf.model.Property;

public class ChaldPropertyRanking extends PropertyRanking {
	private Map<Property, Double> universalScores, uniquenessScores;
	private double beta;
	
	public ChaldPropertyRanking(Map<Property, Double> universalScores, Map<Property, Double> uniquenessScores, double beta) {
		super();
		this.universalScores = universalScores;
		this.uniquenessScores = uniquenessScores;
		this.beta = beta;
	}

	@Override
	public double compute(Property p) {
		return Chald.fMeasure(universalScores.get(p), uniquenessScores.get(p), beta);
	}

}
