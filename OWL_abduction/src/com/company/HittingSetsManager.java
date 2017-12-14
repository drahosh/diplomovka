package com.company;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashSet;
import java.util.Set;

public class HittingSetsManager {
    Set<Set<OWLAxiom>> hittingSets;
    Set<Set<OWLAxiom>> candidatesAlreadyTested;   //pre zrychlenie
    ReasonerAsker asker;
    Set<OWLAxiom> observation;
    public boolean wasAlreadyTested(Set<OWLAxiom> hittingSetCandidate){
        return candidatesAlreadyTested.contains(hittingSetCandidate);
    }
    public boolean testBeforeConsistencyCheck(Set <OWLAxiom> hittingSetCandidate){
        //Returns false if hittingSetCandidate has been already tested or if it and its supersets cannot be valid
        // hitting sets. Should be faster than consistency check.
        if (hittingSetCandidate.containsAll(observation)){
            return false;
            //trivialne
        }
        if(candidatesAlreadyTested.contains(hittingSetCandidate)){
            return false;
        }

        for (Set<OWLAxiom> knownHittingSet : hittingSets){
            if (hittingSetCandidate.containsAll(knownHittingSet)){
                //superset of existing hitting set
                candidatesAlreadyTested.add(hittingSetCandidate); //so that we get it faster next time it gets tested
                return false;
            }
        }
        return true;
    }
    public boolean lastTestAndAddHittingSet(Set<OWLAxiom> hittingSetCandidate){

        //testy menej casovo narocne ako test konzistentnosti su v testBeforeConsistencyCheck
        candidatesAlreadyTested.add(hittingSetCandidate);
        if (!asker.getConsistencyPure(hittingSetCandidate)){
            return false;
            //nieje konzistentne s knowledge base
        }
        hittingSets.add(hittingSetCandidate);
        System.out.println("added hitting set: "+hittingSetCandidate.toString());
        return true;



    }
    HittingSetsManager( ReasonerAsker asker,  Set<OWLAxiom> observation){
        hittingSets = new HashSet<Set<OWLAxiom>>();
        this.asker=asker;
        this.candidatesAlreadyTested=new HashSet<Set<OWLAxiom>>();
        this.observation=observation;
    }
    public Set<Set<OWLAxiom>> getHittingSets(){
        return hittingSets;
    }
}
