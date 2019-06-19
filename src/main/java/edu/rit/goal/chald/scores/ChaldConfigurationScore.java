package edu.rit.goal.chald.scores;

public class ChaldConfigurationScore extends ConfigurationScore {
	private double singularity;

	public ChaldConfigurationScore(double singularity) {
		super();
		this.singularity = singularity;
	}

	@Override
	public int compareTo(ConfigurationScore o) {
		return Double.compare(this.singularity, ((ChaldConfigurationScore) o).singularity);
	}
	
}
