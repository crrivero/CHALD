package edu.rit.goal.chald.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

public class LoadRDFData {

	public static void main(String[] args) throws Exception {
//		String n = "5", src = "scenarios/im_oaei2015_author_dis_sandbox/ontoA.rdf",
//				tgt = "scenarios/im_oaei2015_author_dis_sandbox/ontoB.rdf",
//				gt = "scenarios/im_oaei2015_author_dis_sandbox/refalign.rdf";
		
		
//		String n = "6", src = "scenarios/im_oaei2015_author_rec_sandbox/ontoA.rdf",
//				tgt = "scenarios/im_oaei2015_author_rec_sandbox/ontoB.rdf",
//				gt = "scenarios/im_oaei2015_author_rec_sandbox/refalign.rdf";
		
		
//		String n = "7", src = "scenarios/person1/person11.rdf",
//				tgt = "scenarios/person1/person12.rdf",
//				gt = "scenarios/person1/dataset11_dataset12_goldstandard_person.xml";
		
		
//		String n = "8", src = "scenarios/person2/person21.rdf",
//				tgt = "scenarios/person2/person22.rdf",
//				gt = "scenarios/person2/dataset21_dataset22_goldstandard_person.xml";
		
		
		String n = "9", src = "scenarios/restaurants/restaurant1.rdf",
				tgt = "scenarios/restaurants/restaurant2.rdf",
				gt = "scenarios/restaurants/restaurant1_restaurant2_goldstandard.rdf";
		
		
//		String n = "10", src = "scenarios/SPIMBENCH_small/Abox1.nt",
//				tgt = "scenarios/SPIMBENCH_small/Abox2.nt",
//				gt = "scenarios/SPIMBENCH_small/refalign.rdf";
		
		Model modelSrc = ModelFactory.createDefaultModel(),
				modelTgt = ModelFactory.createDefaultModel(),
				modelGT = ModelFactory.createDefaultModel();
		
		FileInputStream fisSrc = new FileInputStream(new File(src)),
				fisTgt = new FileInputStream(new File(tgt)),
						fisGT = new FileInputStream(new File(gt));
		modelSrc.read(fisSrc, "", (!n.equals("10") ? "RDF/XML" : "N-TRIPLE"));
		modelTgt.read(fisTgt, "", (!n.equals("10") ? "RDF/XML" : "N-TRIPLE"));
		modelGT.read(fisGT, "", "RDF/XML");
		fisGT.close();
		fisTgt.close();
		fisSrc.close();
		
		String srcOut = "data/" + n + "/Source.nt", tgtOut = "data/" + n + "/Target.nt", gtOut = "data/" + n + "/GroundTruth.nt";
		FileOutputStream fosSrc = new FileOutputStream(new File(srcOut)),
				fosTgt = new FileOutputStream(new File(tgtOut)),
						fosGT = new FileOutputStream(new File(gtOut));
		modelSrc.write(fosSrc, "N-TRIPLE");
		modelTgt.write(fosTgt, "N-TRIPLE");
		
		// Deal with ground truth.
		Model modelNewGT = ModelFactory.createDefaultModel();
		StmtIterator it = modelGT.listStatements(
				null, RDF.type, modelGT.getResource("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#Cell"));
		while (it.hasNext()) {
			Statement st = it.next();
			
			StmtIterator innerIt = modelGT.listStatements(st.getSubject(), (Property) null, (RDFNode) null);
			Resource srcInd = null, tgtInd = null;
			
			while (innerIt.hasNext()) {
				Statement innerSt = innerIt.next();
				
				if (innerSt.getPredicate().equals(modelGT.getProperty("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure"))) {
					if (!innerSt.getObject().toString().contains("1.0"))
						throw new Error("Not 1.0!");
				} else if (innerSt.getPredicate().equals(modelGT.getProperty("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#relation"))) {
					if (!innerSt.getObject().toString().contains("="))
						throw new Error("Not =!");
				} else if (innerSt.getPredicate().equals(modelGT.getProperty("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1")))
					srcInd = innerSt.getObject().asResource();
				  else if (innerSt.getPredicate().equals(modelGT.getProperty("http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2")))
						tgtInd = innerSt.getObject().asResource();
			}
			
			modelNewGT.add(srcInd, OWL.sameAs, tgtInd);
		}
		
		modelNewGT.write(fosGT, "N-TRIPLE");
		fosGT.close();
		fosTgt.close();
		fosSrc.close();
		
		modelNewGT.close();
		modelGT.close();
		modelTgt.close();
		modelSrc.close();
	}

}
