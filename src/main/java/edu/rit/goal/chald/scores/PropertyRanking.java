package edu.rit.goal.chald.scores;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Property;

public abstract class PropertyRanking implements Comparator<Property> {
	protected Map<Property, Double> combined = new HashMap<>();
	
	@Override
	public final int compare(Property p1, Property p2) {
		if (!combined.containsKey(p1))
			combined.put(p1, compute(p1));
		if (!combined.containsKey(p2))
			combined.put(p2, compute(p2));
		return Double.compare(combined.get(p2), combined.get(p1));
	}
	
	public abstract double compute(Property p);
	
	public double get(Property p) {
		return combined.get(p);
	}
}
