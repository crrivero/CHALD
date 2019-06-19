package edu.rit.goal.chald.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.rit.goal.chald.scores.Chald;
import edu.rit.goal.chald.scores.Slint;
import edu.rit.goal.chald.transformations.IStringTransformation;
import edu.rit.goal.chald.transformations.Lowercase;
import edu.rit.goal.chald.transformations.RemoveSymbols;
import edu.rit.goal.chald.transformations.StripUriPrefix;
import edu.rit.goal.chald.transformations.Uppercase;
import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.MetricLCS;
import info.debatty.java.stringsimilarity.NGram;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.SorensenDice;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;

public class Sweep {
	private static final String SRC_PROP = "Src. prop.: ", TGT_PROP = "Tgt. prop.: ", SRC_TRANS = "Src. trans.: ", TGT_TRANS = "Tgt. trans.: ", METRIC = "Metric: ";
	
	public static List<NormalizedStringDistance> getSimilarities() {
		return Lists.newArrayList(new NormalizedLevenshtein(), new JaroWinkler(), new MetricLCS(), 
				new NGram(), new Cosine(), new Jaccard(), new SorensenDice());
	}
	
	public static List<IStringTransformation> getTransformations() {
		return Lists.newArrayList(new Lowercase(), new StripUriPrefix(), new RemoveSymbols(), new Uppercase(), new edu.rit.goal.chald.transformations.Void());
	}
	
	public static void main(String[] args) throws Exception {
		String n = args[0];
		String source = "data/" + n + "/Source.nt", target = "data/" + n + "/Target.nt", goldStd = "data/" + n + "/GroundTruth.nt";
		
		Set<String> combinationsPerformed = new HashSet<>();
		File cp = new File("Sweep-" + n + ".txt");
		if (cp.exists()) {
			Scanner sc = new Scanner(cp);
			while (sc.hasNextLine()) {
				String[] line = sc.nextLine().split("; ");
				
				String currentSrcProp = line[0].replace(SRC_PROP, "");
				String currentTgtProp = line[1].replace(TGT_PROP, "");
				String currentSrcTrans = line[2].replace(SRC_TRANS, "");
				String currentTgtTrans = line[3].replace(TGT_TRANS, "");
				String currentMetric = line[4].replace(METRIC, "");
				
				combinationsPerformed.add(currentSrcProp + "--" + currentTgtProp + "--" + currentSrcTrans + "--" + currentTgtTrans + "--" + currentMetric);
			}
			sc.close();
		}
		
		PrintWriter writer = new PrintWriter(new FileOutputStream(cp, true));
		
		Model modelSrc = ModelFactory.createDefaultModel(),
				modelTgt = ModelFactory.createDefaultModel();
		modelSrc.read(source);
		modelTgt.read(target);
		Model modelGoldStd = ModelFactory.createDefaultModel();
		modelGoldStd.read(goldStd);
		
		// Using normalized distances (https://github.com/tdebatty/java-string-similarity).
		List<NormalizedStringDistance> similarities = getSimilarities();
		List<IStringTransformation> transformations = getTransformations();
		
		// Extract source and target URIs.
		Set<Resource> srcURIs = new HashSet<>(), tgtURIs = new HashSet<>();
		Set<Link> groundTruth = new HashSet<>();
		StmtIterator it = modelGoldStd.listStatements();
		while (it.hasNext()) {
			Statement t = it.next();
			srcURIs.add(t.getSubject().asResource());
			tgtURIs.add(t.getObject().asResource());
			groundTruth.add(new Link(t.getSubject().asResource(), t.getObject().asResource()));
		}
		
		// Get unique properties.
		Set<Property> srcProperties = new HashSet<>(), tgtProperties = new HashSet<>();
		modelSrc.listStatements().forEachRemaining(t -> {srcProperties.add(t.getPredicate());});
		modelTgt.listStatements().forEachRemaining(t -> {tgtProperties.add(t.getPredicate());});
		
		int performed = 0, total = srcProperties.size() * tgtProperties.size() * similarities.size() * transformations.size() * transformations.size();
		
		System.out.println(new Date() + " -- Caching target...");
		// Cache all target prop. and trans.
		Table<Property, IStringTransformation, Map<Resource, String>> targetValuesByPropAndTrans = HashBasedTable.create();
		Map<Property, Set<String>> srcPropRepresentatives = new HashMap<>(), tgtPropRepresentatives = new HashMap<>();
		for (Property srcProp : srcProperties)
			srcPropRepresentatives.put(srcProp, Collections.synchronizedSet(new HashSet<>()));
		for (Property tgtProp : tgtProperties) {
			tgtPropRepresentatives.put(tgtProp, Collections.synchronizedSet(new HashSet<>()));
			for (IStringTransformation tgtTrans : transformations)
				targetValuesByPropAndTrans.put(tgtProp, tgtTrans, new ConcurrentHashMap<>());
		}
		
		Table<Property, Property, Double> slintConfidence = HashBasedTable.create();
		for (Property srcProp : srcProperties)
			for (Property tgtProp : tgtProperties)
				slintConfidence.put(srcProp, tgtProp, .0);
		
		srcProperties.parallelStream().forEach(srcProp -> {
			srcURIs.parallelStream().forEach(srcURI -> {
				Statement srcStmt = modelSrc.getProperty(srcURI, srcProp);
				if (srcStmt != null && srcStmt.getObject() != null)
					srcPropRepresentatives.get(srcProp).addAll(Slint.getObjects(srcStmt.getObject()));
			});
		});
		
		tgtProperties.parallelStream().forEach(tgtProp -> {
			tgtURIs.parallelStream().forEach(tgtURI -> {
				Statement tgtStmt = modelTgt.getProperty(tgtURI, tgtProp);
				if (tgtStmt != null && tgtStmt.getObject() != null) {
					tgtPropRepresentatives.get(tgtProp).addAll(Slint.getObjects(tgtStmt.getObject()));
					transformations.parallelStream().forEach(tgtTrans -> {
						targetValuesByPropAndTrans.get(tgtProp, tgtTrans).put(tgtURI, tgtTrans.transform(tgtStmt.getObject().toString()));
					});
				}
			});
			
			for (Property srcProp : srcProperties)
				slintConfidence.put(srcProp, tgtProp, Slint.computeConfidence(srcPropRepresentatives.get(srcProp), tgtPropRepresentatives.get(tgtProp)));
		});
		
		System.out.println(new Date() + " -- Starting: " + total);
		for (Property srcProp : srcProperties)
			for (IStringTransformation srcTrans : transformations) {
				Map<Resource, String> srcValues = new ConcurrentHashMap<>();
				srcURIs.parallelStream().forEach(srcURI -> {
					Statement srcStmt = modelSrc.getProperty(srcURI, srcProp);
					RDFNode objectSrc = null;
					
					if (srcStmt != null)
						objectSrc = srcStmt.getObject();
					
					if (objectSrc != null)
						srcValues.put(srcURI, srcTrans.transform(objectSrc.toString()));
				});
				
				for (Property tgtProp : tgtProperties)
					for (IStringTransformation tgtTrans : transformations)
						for (NormalizedStringDistance sim : similarities) {
							total--;
							if (total % 100 == 0)
								System.out.println(new Date() + " -- Pending: " + total + "; Performed: " + performed);
							
							String current = srcProp + "--" + tgtProp + "--" + srcTrans.getClass().getName() + "--" + tgtTrans.getClass().getName() + "--" + sim.getClass().getName();
							if (combinationsPerformed.contains(current))
								continue;
							
							performed++;
							
							Map<Double, Set<Link>> valuesMap = new ConcurrentHashMap<>();
							srcValues.keySet().parallelStream().forEach(srcURI -> {
								Map<Resource, String> tgtValues = targetValuesByPropAndTrans.get(tgtProp, tgtTrans);
								tgtValues.keySet().parallelStream().forEach(tgtURI -> {
									try {
										double d = sim.distance(srcValues.get(srcURI), tgtValues.get(tgtURI));
										d = (double) Math.round(d * 100.0) / 100.0;
										
										if (!valuesMap.containsKey(d))
											valuesMap.put(d, Collections.synchronizedSet(new HashSet<>()));
										valuesMap.get(d).add(new Link(srcURI, tgtURI));
									} catch (Exception oops) {
										// TODO 0: Deal with this?
										oops.toString();
									}
								});
							});
							
							// For each unique value, how many other values are smaller?
							List<Double> uniqueValues = new ArrayList<>(valuesMap.keySet());
							Collections.sort(uniqueValues);
							
							for (Double x : uniqueValues) {
								List<Link> links = new ArrayList<>();
								for (Double y : uniqueValues)
									if (y <= x)
										links.addAll(valuesMap.get(y));
									else
										// It is sorted.
										break;
								
								Map<Resource, Integer> srcFreq = new ConcurrentHashMap<>(), tgtFreq = new ConcurrentHashMap<>();
								AtomicInteger tp = new AtomicInteger(), fp = new AtomicInteger(), fn = new AtomicInteger();
								
								links.parallelStream().forEach(l -> {
									if (groundTruth.contains(l))
										tp.incrementAndGet();
									else
										fp.incrementAndGet();
									
									if (!srcFreq.containsKey(l.src))
										srcFreq.put(l.src, 1);
									else
										srcFreq.put(l.src, srcFreq.get(l.src) + 1);
									
									if (!tgtFreq.containsKey(l.tgt))
										tgtFreq.put(l.tgt, 1);
									else
										tgtFreq.put(l.tgt, tgtFreq.get(l.tgt) + 1);
								});
								
								groundTruth.parallelStream().forEach(l -> {
									if (!links.contains(l))
										fn.incrementAndGet();
								});
								
								double p = (1.0*tp.get() / (1.0*tp.get() + fp.get())), r = (1.0*tp.get() / (1.0*tp.get() + fn.get())), f1 = (2.0*tp.get() / (2.0*tp.get() + fn.get() + fp.get()));
								writer.println(SRC_PROP + srcProp + "; " + TGT_PROP + tgtProp + "; " + SRC_TRANS + srcTrans.getClass().getName() + "; " + TGT_TRANS + tgtTrans.getClass().getName() + 
									"; " + METRIC + sim.getClass().getName() + "; Threshold: " + x + "; Sing.: " + Chald.computeSingularityScore(srcFreq, tgtFreq, links) + 
									"; P: " + p + "; R: " + r + "; F1: " + f1 + "; Conf.: " + slintConfidence.get(srcProp, tgtProp) + "; Links: " + links.size());
							}
						}
			}
		writer.close();
		
		modelGoldStd.close();
		modelTgt.close();
		modelSrc.close();
	}

}
