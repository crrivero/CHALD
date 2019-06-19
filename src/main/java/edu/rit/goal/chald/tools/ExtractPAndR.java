package edu.rit.goal.chald.tools;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.rit.goal.chald.scores.Chald;
import edu.rit.goal.chald.scores.Slint;

public class ExtractPAndR {
	private static final String TXT = ".txt";
	private static final String SW = "Sweep-";
	private static final String RE = "Results-";
	
	private static final String SRC_PROP = "Src. prop.: ";
	private static final String P = "P: ", R = "R: ";

	public static void main(String[] args) throws Exception {
		ExtractPAndR aux = new ExtractPAndR();
		Table<String, String, Integer> table = HashBasedTable.create();
		
		List<Integer> scns = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8 , 9, 10);
		List<String> techs = Lists.newArrayList(SW, RE+Chald.NAME+"-", RE+Slint.NAME+"-");
		
		for (Integer scn : scns)
			for (String x : techs) {
				int count = 0;
				File results = new File("results/" + x+scn+TXT);
				if (results.exists()) {
					Set<Point> points = new HashSet<>();
					
					Scanner resultsSc = new Scanner(results);
					while (resultsSc.hasNextLine()) {
						String line = resultsSc.nextLine();
						if (line.startsWith(SRC_PROP)) {
							String[] split = line.split("; ");
							double p = Double.valueOf(split[7].replace(P, "")), r = Double.valueOf(split[8].replace(R, ""));
							if (!Double.isNaN(p) && !Double.isNaN(r)) {
								p = (double) Math.round(p * 100.0) / 100.0;
								r = (double) Math.round(r * 100.0) / 100.0;
								
								Point point = aux.new Point();
								point.x = p;
								point.y = r;
								points.add(point);
								
								count++;
							} else
								throw new Error("NaN present!");
						}
					}
					resultsSc.close();
					
					PrintWriter writer = new PrintWriter(new File("results/PAndR-"+x+scn+TXT));
					for (Point p : points)
						writer.println(p.x + "\t" + p.y);
					writer.close();
				}
				
				table.put(scn + "", x, count);
			}
		
		System.out.println("\t");
		for (String x : techs)
			System.out.print(x + "\t");
		System.out.println();
		for (Integer scn : scns) {
			System.out.print("Scn: " + scn + "\t");
			for (String x : techs)
				if (table.contains(scn + "", x))
					System.out.print(table.get(scn + "", x) + "\t");
				else
					System.out.print(0 + "\t");
			System.out.println();
		}
	}
	
	private class Point {
		double x, y;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			long temp;
			temp = Double.doubleToLongBits(x);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
				return false;
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
				return false;
			return true;
		}

		private ExtractPAndR getOuterType() {
			return ExtractPAndR.this;
		}
	}

}
