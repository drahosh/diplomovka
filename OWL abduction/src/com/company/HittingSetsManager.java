package com.company;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class HittingSetsManager {
    List<Set<OWLAxiom>> hittingSets;
    List<Set<OWLAxiom>> minimal_inconsistent_candidates;
    Set<Set<OWLAxiom>> candidatesAlreadyTested;   //faster duplicate removal
    ReasonerAsker asker;
    List<OWLAxiom> observations;

    public boolean firstTest(Set <OWLAxiom> hittingSetCandidate){
        //tests duplicates and triviality
        //hitting set candidate generation process can result in duplicates
        if (candidatesAlreadyTested.contains(hittingSetCandidate)){
            return false;
        }
        candidatesAlreadyTested.add(hittingSetCandidate);
        for (OWLAxiom observation : observations) {
            if (hittingSetCandidate.contains(observation)) {
                // System.out.println(hittingSetCandidate);
                //trivial
                return false;
            }
        }
        //System.out.println(hittingSetCandidate);
        return true;
    }
    public boolean positiveConsistency_test(Set<OWLAxiom> hittingSetCandidate){
        //tests if hitting set candidate is consistent with knowledge base

        //We know its inconsistent if a part of it is inconsistent, no asking reasoner needed
        //Need to check time, this one could possibly take longer than consistency checking
        for (Set<OWLAxiom> minimalCandidate: minimal_inconsistent_candidates){
            if (hittingSetCandidate.containsAll(minimalCandidate)){
                return false;
            }
        }
        boolean consistency = asker.getConsistencyPositiveObservation(hittingSetCandidate);
        if (!consistency){
            minimal_inconsistent_candidates.add(hittingSetCandidate);
            return false;
        }
        return true;   //sets when we check consistency
    }
    public boolean minimality_test(Set <OWLAxiom> hittingSetCandidate){
        //tests if its synt. minimal to known hitting sets
        for (Set<OWLAxiom> knownHittingSet : hittingSets){
            if (hittingSetCandidate.containsAll(knownHittingSet)){
                return false;
            }
        }
        return true;
    }
    public boolean addHittingSetIfInconsistent(Set<OWLAxiom> hittingSetCandidate){
        //adds to hitting sets if inconsistent with knowledge base + observation complement
        //assumes all the other tests have been done (first, consistenncy, minimality)

        //btw still not testing for relevancy

        //System.out.println("Testing :"+hittingSetCandidate);
        if(!asker.getConsistency(hittingSetCandidate)) {
            hittingSets.add(hittingSetCandidate);
            System.out.print("added hitting set: ");
            Main.printAxiomSet(hittingSetCandidate);
            return true;
        }
        return false;

    }

    private HittingSetsManager(ReasonerAsker asker){
        this.asker=asker;
        this.candidatesAlreadyTested=new HashSet<>();
        this.minimal_inconsistent_candidates=new LinkedList<Set<OWLAxiom>>();
        this.hittingSets=new LinkedList<Set<OWLAxiom>>();
    }
    HittingSetsManager(ReasonerAsker asker, OWLAxiom observation){
        this(asker);
        this.observations = new LinkedList<OWLAxiom>();
        this.observations.add(observation);
    }
    HittingSetsManager(ReasonerAsker asker, List<OWLAxiom> observations){
        this(asker);
        this.observations=observations;
    }
    public List<Set<OWLAxiom>> getHittingSets(){
        return hittingSets;
    }
    public void setMinimal_inconsistent_candidates(List<Set<OWLAxiom>>minimal_inconsistent_candidates){
        this.minimal_inconsistent_candidates=minimal_inconsistent_candidates;
    }
    public List<Set<OWLAxiom>> getMinimal_inconsistent_candidates(){
        return this.minimal_inconsistent_candidates;
    }



    public static List<Set<OWLAxiom>>  relevancy_test(List<Set<OWLAxiom>> hittingSets, OWLAxiom antiObservation){
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        try {
            OWLOntology ontology = manager.createOntology();
            manager.addAxiom(ontology,antiObservation);
            OWLReasoner reasoner = ReasonerAsker.initializeReasoner(ontology);
            for(int i=0;i<hittingSets.size();i++){
                Set<OWLAxiom> hittingSet=hittingSets.get(i);
                manager.addAxioms(ontology,hittingSet);
                reasoner=ReasonerAsker.refresh_Reasoner(reasoner,ontology);
                boolean result = reasoner.isConsistent();
                manager.removeAxioms(ontology,hittingSet);
                if (!result){
                    //is inconsistent with antiobservation, we already know its consistent with observation, so
                    //hitting set -> observation , and that means its not relevant
                    System.out.print("failed relevancy test: ");
                    Main.printAxiomSet(hittingSet);
                    hittingSets.remove(i);
                    i--;
                }
                //System.out.print("passed relevancy test: ");
                //Main.printAxiomSet(hittingSet);
            }
            return hittingSets;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
