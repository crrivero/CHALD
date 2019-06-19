package edu.rit.goal.chald.tools;

import java.io.File;
import java.io.FileOutputStream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class LoadOWLData {

	public static void main(String[] args) throws Exception {
//		String folder = "im_oaei2015_author_dis_sandbox";
		String folder = "im_oaei2015_author_rec_sandbox";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File srcFile = new File("scenarios/" + folder + "/ontoA.owl"),
				tgtFile = new File("scenarios/" + folder + "/ontoB.owl");
		OWLOntology srcOntology = manager.loadOntologyFromOntologyDocument(srcFile),
				tgtOntology = manager.loadOntologyFromOntologyDocument(tgtFile);
		FileOutputStream srcOut = new FileOutputStream(new File("scenarios/" + folder + "/ontoA.rdf")),
				tgtOut = new FileOutputStream(new File("scenarios/" + folder + "/ontoB.rdf"));
		manager.saveOntology(srcOntology, new RDFXMLOntologyFormat(), srcOut);
		manager.saveOntology(tgtOntology, new RDFXMLOntologyFormat(), tgtOut);
		srcOut.close();
		tgtOut.close();
	}

}
