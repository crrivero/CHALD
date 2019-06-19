package edu.rit.goal.chald.tools;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.Lists;

import edu.rit.goal.chald.scores.Chald;
import edu.rit.goal.chald.scores.ChaldConfigurationScore;
import edu.rit.goal.chald.scores.ChaldPropertyRanking;
import edu.rit.goal.chald.scores.ConfigurationScore;
import edu.rit.goal.chald.scores.PropertyRanking;
import edu.rit.goal.chald.scores.Slint;
import edu.rit.goal.chald.scores.SlintConfigurationScore;
import edu.rit.goal.chald.scores.SlintPropertyRanking;

public class ConfigurationSelection {
	public static void main(String[] args) throws Exception {
		for (Integer i : Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
			for (String algo : Lists.newArrayList(Chald.NAME, Slint.NAME)) {
				System.out.println(new Date() + " -- Scn: " + i + "; Algo: " + algo);
				//long startingTime = System.nanoTime();
				String n = "" + i;
				String source = "data/" + n + "/Source.nt", target = "data/" + n + "/Target.nt";
				
				double srcPropThreshold = .50, tgtPropThreshold = .50, confThreshold = .10;
				
				File cpFile = new File("results/Sweep-" + i + ".txt");
				if (!cpFile.exists())
					continue;
				
				PrintWriter writer = new PrintWriter("results/Results-" + algo + "-" + n + ".txt");
//				PrintWriter writer = new PrintWriter(new NullOutputStream());
				
				Model modelSrc = ModelFactory.createDefaultModel(),
						modelTgt = ModelFactory.createDefaultModel();
				
				modelSrc.read(source);
				modelTgt.read(target);
				
				// Extract source and target URIs.
				Map<RDFNode, Integer> srcTypes = new HashMap<>(), tgtTypes = new HashMap<>();
				Set<Resource> srcURIs = new HashSet<>(), tgtURIs = new HashSet<>();
				Set<Property> srcPropsSet = new HashSet<>(), tgtPropsSet = new HashSet<>();
				modelSrc.listStatements().forEachRemaining(t -> {
					srcURIs.add(t.getSubject());
					srcPropsSet.add(t.getPredicate());
					if (t.getPredicate().equals(RDF.type)) {
						if (!srcTypes.containsKey(t.getObject()))
							srcTypes.put(t.getObject(), 0);
						srcTypes.put(t.getObject(), srcTypes.get(t.getObject()) + 1);
					}
				});
				modelTgt.listStatements().forEachRemaining(t -> {
					tgtURIs.add(t.getSubject());
					tgtPropsSet.add(t.getPredicate());
					if (t.getPredicate().equals(RDF.type)) {
						if (!tgtTypes.containsKey(t.getObject()))
							tgtTypes.put(t.getObject(), 0);
						tgtTypes.put(t.getObject(), tgtTypes.get(t.getObject()) + 1);
					}
				});
				List<Property> srcProps = new ArrayList<>(srcPropsSet), tgtProps = new ArrayList<>(tgtPropsSet);
				
				writer.println("Source types: " + srcTypes);
				writer.println("Target types: " + tgtTypes);
				
				// Scores.
				Map<Property, Double> firstScoreSrc = null, firstScoreTgt = null;
				Map<Property, Double> secondScoreSrc = null, secondScoreTgt = null;
				PropertyRanking srcRanking = null, tgtRanking = null;
				
				if (algo.equals(Chald.NAME)) {
					firstScoreSrc = Chald.computeUniversalityScore(modelSrc, srcURIs);
					firstScoreTgt = Chald.computeUniversalityScore(modelTgt, tgtURIs);
					secondScoreSrc = Chald.computeUniquenessScore(modelSrc, srcURIs);
					secondScoreTgt = Chald.computeUniquenessScore(modelTgt, tgtURIs);
					srcRanking = new ChaldPropertyRanking(firstScoreSrc, secondScoreSrc, 2.0);
					tgtRanking = new ChaldPropertyRanking(firstScoreTgt, secondScoreTgt, 2.0);
				}
				
				if (algo.equals(Slint.NAME)) {
					firstScoreSrc = Slint.computeCoverageScore(modelSrc, srcProps);
					firstScoreTgt = Slint.computeCoverageScore(modelTgt, tgtProps);
					secondScoreSrc = Slint.computeDiscriminationScore(modelSrc, srcProps);
					secondScoreTgt = Slint.computeDiscriminationScore(modelTgt, tgtProps);
					srcRanking = new SlintPropertyRanking(firstScoreSrc, secondScoreSrc);
					tgtRanking = new SlintPropertyRanking(firstScoreTgt, secondScoreTgt);
				}
				
				Collections.sort(srcProps, srcRanking);
				Collections.sort(tgtProps, tgtRanking);
				
				// Select source and target properties based on tolerance.
				List<Property> selectedSrcProperties = selectProperties(srcProps, srcRanking, srcPropThreshold),
						selectedTgtProperties = selectProperties(tgtProps, tgtRanking, tgtPropThreshold);
				writer.println("Src (score): ");
				for (Property p : srcProps) {
					writer.print("\t\t Property: " + p + "; First score: " + firstScoreSrc.get(p) + 
							"; Second score: " + secondScoreSrc.get(p) + "; Combined: " + srcRanking.compute(p));
					if (selectedSrcProperties.contains(p))
						writer.print("; Selected");
					writer.println();
				}
				writer.println("Tgt (score): ");
				for (Property p : tgtProps) {
					writer.print("\t\t Property: " + p + "; First score: " + firstScoreTgt.get(p) + 
							"; Second score: " + secondScoreTgt.get(p) + "; Combined: " + tgtRanking.compute(p));
					if (selectedTgtProperties.contains(p))
						writer.print("; Selected");
					writer.println();
				}
				
				System.out.println("Src. selected prop.: " + selectedSrcProperties.size() + 
						"; Src. reduction: " + (1 - (double)selectedSrcProperties.size()/srcProps.size()));
				System.out.println("Tgt. selected prop.: " + selectedTgtProperties.size() + 
						"; Tgt. reduction: " + (1 - (double)selectedTgtProperties.size()/tgtProps.size()));
				
				List<Pair<String, ConfigurationScore>> selectedConfigs = new ArrayList<>();
				Scanner sc = new Scanner(cpFile);
				while (sc.hasNextLine()) {
					String l = sc.nextLine();
					String[] line = l.split("; ");
					
					Property srcProp = modelSrc.getProperty(line[0].replace("Src. prop.: ", "")), 
							tgtProp = modelTgt.getProperty(line[1].replace("Tgt. prop.: ", ""));
					if (selectedSrcProperties.contains(srcProp) && selectedTgtProperties.contains(tgtProp)) {
						ConfigurationScore cs = null;
						if (algo.equals(Chald.NAME)) {
							double sing = Double.valueOf(line[6].replace("Sing.: ", ""));
							// Small correction.
							if (Integer.valueOf(line[11].replace("Links: ", "")) == 1)
								sing = 1.0;
							cs = new ChaldConfigurationScore(sing);
						}
						if (algo.equals(Slint.NAME))
							cs = new SlintConfigurationScore(Double.valueOf(line[10].replace("Conf.: ", "")), 
									/*Double.valueOf(line[9].replace("F1: ", ""))*/.0);
						selectedConfigs.add(new Pair<>(l, cs));
					}
				}
				sc.close();
				
				Collections.sort(selectedConfigs, new Comparator<Pair<String, ConfigurationScore>>() {
					@Override
					public int compare(Pair<String, ConfigurationScore> a, Pair<String, ConfigurationScore> b) {
						return b.getSecond().compareTo(a.getSecond());
					}
				});
				for (Pair<String, ConfigurationScore> s : selectConfigurations(selectedConfigs, confThreshold))
					writer.println(s.getFirst());
				//long endingTime = System.nanoTime();
				
				//writer.println("Total time (seconds): " + (endingTime - startingTime)/1e9);
				
				writer.close();
				modelTgt.close();
				modelSrc.close();
			}
	}
	
	private static List<Property> selectProperties(List<Property> ranking, PropertyRanking pr, double threshold) {
		List<Property> selected = new ArrayList<>();
		int i = 0;
		for (i = 0; i < Math.max(1, Math.round(ranking.size()*threshold)); i++)
			selected.add(ranking.get(i));
		Property last = selected.get(i - 1);
		// Check if there are others with the same value as the last one.
		for (; i < ranking.size(); i++)
			if (pr.get(last) == pr.get(ranking.get(i)))
				selected.add(ranking.get(i));
			else
				break;
		return selected;
	}
	
	private static List<Pair<String, ConfigurationScore>> selectConfigurations(List<Pair<String, ConfigurationScore>> ranking, double threshold) {
		List<Pair<String, ConfigurationScore>> selected = new ArrayList<>();
		int i = 0;
		for (i = 0; i < Math.max(1, Math.round(ranking.size()*threshold)); i++)
			selected.add(ranking.get(i));
		Pair<String, ConfigurationScore> last = selected.get(i - 1);
		// Check if there are others with the same value as the last one.
		for (; i < ranking.size(); i++)
			if (last.getSecond().compareTo(ranking.get(i).getSecond()) == 0)
				selected.add(ranking.get(i));
			else
				break;
		return selected;
	}

}
