package edu.rit.goal.chald.scores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.rit.goal.chald.tools.Link;

public class Chald {
	public static final String NAME = "CHALD";
	public static Map<Property, Double> computeUniversalityScore(Model m, Collection<Resource> uris) {
		Map<Property, Set<Resource>> count = new HashMap<>();
		Map<Property, Double> ret = new HashMap<>();
		
		for (Resource r : uris) {
			StmtIterator it = m.listStatements(r, (Property) null, (RDFNode) null);
			while (it.hasNext()) {
				Statement stmt = it.next();
				Property p = stmt.getPredicate();
				if (!count.containsKey(p))
					count.put(p, new HashSet<>());
				count.get(p).add(r);
			}
		}
		
		for (Property p : count.keySet())
			ret.put(p, (double)count.get(p).size()/uris.size());
		
		return ret;
	}
	
	public static Map<Property, Double> computeUniquenessScore(Model m, Collection<Resource> uris) {
		Map<Property, List<RDFNode>> mapOfListValues = new HashMap<>();
		Map<Property, Set<RDFNode>> mapOfSetValues = new HashMap<>();
		Map<Property, Double> ret = new HashMap<>();
		
		for (Resource r : uris) {
			StmtIterator it = m.listStatements(r, (Property) null, (RDFNode) null);
			while (it.hasNext()) {
				Statement stmt = it.next();
				Property p = stmt.getPredicate();
				if (!mapOfListValues.containsKey(p)) {
					mapOfListValues.put(p, new ArrayList<>());
					mapOfSetValues.put(p, new HashSet<>());
				}
				mapOfListValues.get(p).add(stmt.getObject());
				mapOfSetValues.get(p).add(stmt.getObject());
			}
		}
		
		for (Property p : mapOfListValues.keySet())
			ret.put(p, mapOfSetValues.get(p).size() / (double) mapOfListValues.get(p).size());
		
		return ret;
	}
	
	public static double computeSingularityScore(Map<Resource, Integer> srcFreq, Map<Resource, Integer> tgtFreq, Collection<Link> links) {
		double singularity = .0;
		if (links.size() == 1)
			singularity = 1.0;
		if (links.size() > 1) {
			for (Resource r : srcFreq.keySet())
				singularity += compute(srcFreq, r, links);
			for (Resource r : tgtFreq.keySet())
				singularity += compute(tgtFreq, r, links);
			singularity /= 2*Math.log10(links.size());
			if (Double.isNaN(singularity))
				throw new Error("Singularity was NaN.");
			// Rounding.
			singularity = Math.round(singularity * 10000.0)*1.0/10000.0;
			if (singularity > 1.0)
				throw new Error("Singularity was greater than one.");
		}
		return singularity;
	}
	
	private static double compute(Map<Resource, Integer> freq, Resource r, Collection<Link> links) {
		double ret = .0;
		double get = (1.0*freq.get(r))/links.size();
		if (get != 0)
			ret = -get * Math.log10(get);
		if (Double.isNaN(get))
			throw new Error("It was NaN");
		return ret;
	}
	
	public static double fMeasure(double a, double b, double beta) {
		double ret = .0;
		if (a > .0 || b > .0)
			ret = (1+Math.pow(beta, 2))*a*b/((Math.pow(beta, 2)*a) + b);
		if (ret > 1.0)
			throw new Error("Greater than one");
		return ret;
	}
	
}
