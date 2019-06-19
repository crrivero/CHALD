package edu.rit.goal.chald.tools;

import org.apache.jena.rdf.model.Resource;

public class Link {
	Resource src, tgt;

	Link(Resource src, Resource tgt) {
		super();
		this.src = src;
		this.tgt = tgt;
	}

	@Override
	public String toString() {
		return "(" + src + "==" + tgt + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result + ((tgt == null) ? 0 : tgt.hashCode());
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
		Link other = (Link) obj;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (tgt == null) {
			if (other.tgt != null)
				return false;
		} else if (!tgt.equals(other.tgt))
			return false;
		return true;
	}
	
}
