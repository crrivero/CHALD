package edu.rit.goal.chald.scores;

public class SlintConfigurationScore extends ConfigurationScore {
	private double conf, f1;

	public SlintConfigurationScore(double conf, double f1) {
		super();
		this.conf = conf;
		this.f1 = f1;
	}

	@Override
	public int compareTo(ConfigurationScore o) {
		int ret = Double.compare(this.conf, ((SlintConfigurationScore) o).conf);
		if (ret == 0)
			ret = Double.compare(this.f1, ((SlintConfigurationScore) o).f1);
		return ret;
	}
	
}
