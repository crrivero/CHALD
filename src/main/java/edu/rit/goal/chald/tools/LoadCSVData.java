package edu.rit.goal.chald.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.OWL;

public class LoadCSVData {
	public static void main(String[] args) throws Exception {
//		String s = "Abt", t = "Buy", n = "1";
//		String s = "Amazon", t = "GoogleProducts", n = "2";
//		String s = "DBLP", t = "ACM", n = "3";
		String s = "DBLP", t = "Scholar", n = "4";
		
		String source = "scenarios/" + s + "-" + t + "/" + s + ".csv", target = "scenarios/" + s + "-" + t + "/" + t + ".csv", 
				gt = "scenarios/" + s + "-" + t + "/" + s + "_" + t + "_perfectMapping.csv";
		String sourceName = "tdb-data/" + s + "", targetName = "tdb-data/" + t + "", gtName = "tdb-data/" + s + "-" + t + "-GroundTruth";
		String srcOut = "data/" + n + "/Source.nt", tgtOut = "data/" + n + "/Target.nt", gtOut = "data/" + n + "/GroundTruth.nt";
		
		Dataset datasetSrc = TDBFactory.createDataset(sourceName),
				datasetTgt = TDBFactory.createDataset(targetName),
				datasetGT = TDBFactory.createDataset(gtName);
		
		Model modelSrc = datasetSrc.getDefaultModel(),
				modelTgt = datasetTgt.getDefaultModel(),
				modelGT = datasetGT.getDefaultModel();
		
		load(source, modelSrc, srcOut);
		load(target, modelTgt, tgtOut);
		loadGT(gt, modelGT, gtOut);
		
		modelGT.removeAll();
		modelTgt.removeAll();
		modelSrc.removeAll();
		
		modelGT.close();
		modelTgt.close();
		modelSrc.close();
	}
	
	private static void load(String file, Model model, String out) throws Exception {
		Map<Integer, Property> propMap = new HashMap<>();
		
		Scanner sc = new Scanner(new File(file));
		int count = 0;
		while (sc.hasNextLine()) {
			String[] line = null;
			
			if (count == 0) {
				line = sc.nextLine().split("\",\"");
				line[0] = line[0].substring(1, line[0].length());
				line[line.length - 1] = line[line.length - 1].substring(0, line[line.length - 1].length() - 1);
			} else
				line = parseLine(sc.nextLine());
			
			for (int i = 1; i < line.length; i++)
				if (count == 0)
					propMap.put(i, model.createProperty(line[i]));
				else {
					if (line[i].length() > 0)
						model.add(model.createResource(line[0]), propMap.get(i), model.createLiteral(line[i]));
				}
			
			count++;
		}
		sc.close();
		
		FileOutputStream fos = new FileOutputStream(new File(out));
		model.write(fos, "N-TRIPLE");
		fos.close();
	}
	
	private static void loadGT(String file, Model model, String out) throws Exception {
		Scanner sc = new Scanner(new File(file));
		int count = 0;
		while (sc.hasNextLine()) {
			String[] line = null;
			String current = sc.nextLine();
			
			if (count > 0) {
				line = current.replace("\"", "").split(",");
				model.add(model.createResource(line[0]), OWL.sameAs, model.createResource(line[1]));
			}
			
			count++;
		}
		sc.close();
		
		FileOutputStream fos = new FileOutputStream(new File(out));
		model.write(fos, "N-TRIPLE");
		fos.close();
	}
	
	private static String[] parseLine(String line) {
		List<String> list = new ArrayList<>();
		
		StringBuffer buf = new StringBuffer();
		boolean isOpenQuote = false;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ',' && !isOpenQuote) {
				list.add(buf.toString());
				buf = new StringBuffer();
			} else if (line.charAt(i) == '\"' && !isOpenQuote)
				isOpenQuote = true;
			else if (line.charAt(i) == '\"' && isOpenQuote)
				isOpenQuote = false;
			else
				buf.append(line.charAt(i));
		}
		
		String[] ret = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			ret[i] = list.get(i);
		return ret;
	}
	
}
