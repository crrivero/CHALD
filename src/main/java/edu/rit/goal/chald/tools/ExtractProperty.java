package edu.rit.goal.chald.tools;

import java.io.File;
import java.util.Scanner;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class ExtractProperty {
	private static final String SRC_PROP = "Src. prop.: ",  TGT_PROP = "Tgt. prop.: ";
	private static final String P = "P: ", R = "R: ", F1 = "F1: ";

	public static void main(String[] args) throws Exception {
		String property = "http://dbpedia.org/property/equipment", scn = "10";
		int pos = 0; // 0: source; 1: target.
		
		SummaryStatistics pStats = new SummaryStatistics(), rStats = new SummaryStatistics(), f1Stats = new SummaryStatistics();
		File results = new File("results/Sweep-" + scn + ".txt");
		if (results.exists()) {
			Scanner resultsSc = new Scanner(results);
			while (resultsSc.hasNextLine()) {
				String line = resultsSc.nextLine();
				if (line.startsWith(SRC_PROP)) {
					String[] split = line.split("; ");
					
					if (split[pos].replace(SRC_PROP, "").replace(TGT_PROP, "").equals(property)) {
						double p = Double.valueOf(split[7].replace(P, "")), r = Double.valueOf(split[8].replace(R, "")),
								f1 = Double.valueOf(split[9].replace(F1, ""));
						pStats.addValue(p);
						rStats.addValue(r);
						f1Stats.addValue(f1);
					}
				}
			}
			resultsSc.close();
			System.out.println("Property: " + property);
			System.out.println("P -- Max: " + pStats.getMax() + "; Min: " + pStats.getMin() + "; Mean: " + pStats.getMean() + " ± " + pStats.getStandardDeviation());
			System.out.println("R -- Max: " + rStats.getMax() + "; Min: " + rStats.getMin() + "; Mean: " + rStats.getMean() + " ± " + rStats.getStandardDeviation());
			System.out.println("F1 -- Max: " + f1Stats.getMax() + "; Min: " + f1Stats.getMin() + "; Mean: " + f1Stats.getMean() + " ± " + f1Stats.getStandardDeviation());
		}
		

	}

}
