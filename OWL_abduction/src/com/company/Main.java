package com.company;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.owllink.*;
import org.semanticweb.owlapi.apibinding.OWLManager;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfiguration;

public class Main {




    public static void do_the_algorithm(String filepath, String baseIriString, String indString, String classString, int maxDepth){
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(filepath));

            Set<OWLAxiom> observation=new HashSet<OWLAxiom>();
            Set<OWLAxiom> observationComplement = new HashSet<OWLAxiom>();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual=dfactory.getOWLNamedIndividual(IRI.create(baseIriString+indString));
            OWLClassExpression observationClassExp= dfactory.getOWLClass(IRI.create(baseIriString+classString));
            OWLAxiom observationOne= dfactory.getOWLClassAssertionAxiom(observationClassExp,observationIndividual);
            OWLAxiom observationOneComplement = dfactory.getOWLClassAssertionAxiom(observationClassExp.getComplementNNF(),observationIndividual);
            observation.add(observationOne);
            observationComplement.add(observationOneComplement);

            ReasonerAsker asker=new ReasonerAsker(ontology,manager,observationComplement);

            HittingSetsManager hittingSetsManager = new HittingSetsManager(asker, observation);

            Queue<Set<OWLAxiom>> hittingSetCandidates= new LinkedList<Set<OWLAxiom>>();
            Queue<Set<OWLAxiom>> nextHittingSetCandidates= new LinkedList<Set<OWLAxiom>>();
            Set<OWLAxiom> emptySet=new HashSet<OWLAxiom>();

            if (!asker.getConsistencyPure(observation)){
                System.out.println("Pozorovanie je nekonzistentne s knowledge base");
            }
            if (!asker.getConsistency(emptySet)){
                System.out.println("Pozorovanie vypliva z knowledge base");
            }
            hittingSetCandidates.add(emptySet);

            try {
                for (int depth = 0; depth <= maxDepth; depth++) {
                    System.out.println("depth is "+depth);
                    System.out.println("candidates to test: " + hittingSetCandidates.size());
                    while (!hittingSetCandidates.isEmpty()) {
                        Set<OWLAxiom> axioms = hittingSetCandidates.remove();
                        if (!hittingSetsManager.testBeforeConsistencyCheck(axioms)) {
                            continue;
                        }
                        // nasledujuca zlozena podmienka je na to aby sa testovala konzistentnost len ak je to treba
                        //if (hittingSetsManager.testBeforeConsistencyCheck(axioms)) {
                        if(!asker.getConsistency(axioms)){
                            hittingSetsManager.lastTestAndAddHittingSet(axioms);
                            continue;
                        }
                        /*}else{
                            if(!asker.getConsistency(axioms)){
                                continue;
                            }
                        }*/
                        if(depth==maxDepth){
                            continue;
                        }
                        Set<OWLAxiom> antimodel = asker.getAntiModel(axioms);
                        System.out.println(antimodel.size());
                        for (OWLAxiom axiom : antimodel) {
                            if (!axioms.contains(axiom)) {
                                // nexceme nahodou pridat co uz vieme, ani pokusit sa testovat to iste dvakrat,
                                // ani ak vieme ze nadmnoziny nebudu validnym hitting setom

                                Set<OWLAxiom> newHittingSetCandidate = new HashSet<OWLAxiom>(axioms);
                                newHittingSetCandidate.add(axiom);
                               // if (hittingSetsManager.testBeforeConsistencyCheck(newHittingSetCandidate)) {
                                    nextHittingSetCandidates.add(newHittingSetCandidate);
                                //}
                            }
                        }


                    }
                    hittingSetCandidates = nextHittingSetCandidates;
                    nextHittingSetCandidates = new LinkedList<Set<OWLAxiom>>();

                }
                manager.saveOntology(ontology, new SystemOutDocumentTarget());

                System.out.println(hittingSetsManager.getHittingSets());
            }catch(Exception e){
                e.printStackTrace();
                System.out.println(hittingSetsManager.getHittingSets());

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void test1(){
        do_the_algorithm("C:/Users/drahos/Downloads/pellet-aaa/pellet2/examples/src/main/resources/data/test-2ind.owl.xml",
                "http://www.semanticweb.org/ontologies/2016/0/Ontology1454078869361.owl",
                "#b",
                "#D",
                8);

    }
    public static void test2(){
        do_the_algorithm("C:/Users/drahos/Downloads/pellet-aaa/pellet2/examples/src/main/resources/data/people+pets.owl",
                "http://cohse.semanticweb.org/ontologies/people",
                "a",
                "Pet",
                1);
    }
    public static void test3(){
        do_the_algorithm("C:/Users/drahos/Downloads/pellet-aaa/pellet2/examples/src/main/resources/data/family.owl",
                "http://www.semanticweb.org/julia/ontologies/2017/8/family",
                "aaaaabbbbb",
                "Mother",
                7);
    }
    public static void main(String[] args) {
        test1();
        //test3();
    }
}


// TODO: pseudokod (aj orezavacky)
