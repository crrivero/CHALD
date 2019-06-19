package edu.rit.goal.chald.scores;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import edu.rit.goal.chald.transformations.StripUriPrefix;

public class Slint {
	public static final String NAME = "SLINT";
	public static Map<Property, Double> computeDiscriminationScore(Model m, Collection<Property> properties) {
		Map<Property, Integer> count = new HashMap<>();
		
		Map<Property, Set<RDFNode>> objectsOfProperty = new HashMap<>();
		Table<Property, RDFNode, Integer> objectsOfPropertyFreq = HashBasedTable.create();
		for (Property p : properties) {
			StmtIterator it = m.listStatements(null, p, (RDFNode) null);
			while (it.hasNext()) {
				Statement stmt = it.next();
				if (!count.containsKey(p))
					count.put(p, 0);
				count.put(p, count.get(p) + 1);
				
				if (!objectsOfProperty.containsKey(p))
					objectsOfProperty.put(p, new HashSet<>());
				objectsOfProperty.get(p).add(stmt.getObject());
				
				if (!objectsOfPropertyFreq.contains(p, stmt.getObject()))
					objectsOfPropertyFreq.put(p, stmt.getObject(), 0);
				objectsOfPropertyFreq.put(p, stmt.getObject(), objectsOfPropertyFreq.get(p, stmt.getObject()) + 1);
			}
		}
		
		Map<Property, Double> vPD = new HashMap<>();
		for (Property p : properties)
			vPD.put(p, (double) objectsOfProperty.get(p).size()/count.get(p));
		
		Map<Property, Double> hPD = new HashMap<>();
		for (Property p : properties) {
			double entropy = .0, sumFreq = .0;
			for (RDFNode o : objectsOfPropertyFreq.row(p).keySet())
				sumFreq += objectsOfPropertyFreq.get(p, o);
			for (RDFNode o : objectsOfPropertyFreq.row(p).keySet())
				entropy += (double)objectsOfPropertyFreq.get(p, o)/sumFreq * Math.log10((double)objectsOfPropertyFreq.get(p, o)/sumFreq);
			hPD.put(p, entropy);
		}
		
		Map<Property, Double> ret = new HashMap<>();
		for (Property p : properties)
			ret.put(p, hmean(vPD.get(p), hPD.get(p)));
		return ret;
	}
	
	public static Map<Property, Double> computeCoverageScore(Model m, Collection<Property> properties) {
		Map<Property, Integer> count = new HashMap<>();
		Map<Property, Double> ret = new HashMap<>();
		
		for (Property p : properties) {
			StmtIterator it = m.listStatements(null, p, (RDFNode) null);
			while (it.hasNext()) {
				it.next();
				if (!count.containsKey(p))
					count.put(p, 0);
				count.put(p, count.get(p) + 1);
			}
		}
		
		for (Property p : count.keySet())
			ret.put(p, (double)count.get(p)/m.size());
		
		return ret;
	}
	
	public static double hmean(double x1, double x2) {
		double ret = .0;
		if (x1 > .0 || x2 > .0)
			ret = (2*x1*x2)/(x1+x2);
		return ret;
	}
	
	private static final Set<RDFDatatype> stringDatatypes = Sets.newHashSet(RDFLangString.rdfLangString, XSDBaseStringType.XSDstring);
	
	public static Set<String> getObjects(RDFNode node) {
		Set<String> ret = new HashSet<>();
		if (node.isLiteral() && !node.asLiteral().getDatatype().equals(XSDBaseStringType.XSDanyURI)) {
			if (stringDatatypes.contains(node.asLiteral().getDatatype())) {
				StringTokenizer st = new StringTokenizer(node.asLiteral().getString());
			    while (st.hasMoreTokens())
			    	ret.add(st.nextToken());
			} else
				ret.add(node.asLiteral().toString());
		} else {
			StripUriPrefix t = new StripUriPrefix();
			for (String str : t.transform(node.toString()).split("/"))
				ret.add(str);
		}
		return ret;
	}
	
	public static double computeConfidence(Set<String> srcRep, Set<String> tgtRep) {
		double ret = .0;
		if (srcRep.size() > 0 || tgtRep.size() > 0)
			ret = 2.0 * Sets.intersection(srcRep, tgtRep).size() / ((srcRep.size() * 1.0) + (tgtRep.size() * 1.0));
		return ret;
	}
	
}
