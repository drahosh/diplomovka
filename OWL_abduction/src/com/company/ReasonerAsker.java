package com.company;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.Node;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ReasonerAsker {
    OWLOntology ontology;
    OWLReasoner reasoner;
    OWLOntologyManager manager;
    OWLDataFactory dfactory;
    Set<OWLAxiom> antiObservation;
    Set<OWLAxiom> defaultAntimodel;
    ReasonerAsker(OWLOntology knowledgeBase, OWLOntologyManager manager, Set<OWLAxiom> antiObservation){
        this.ontology=knowledgeBase;
        this.manager=manager;
        this.dfactory = manager.getOWLDataFactory();
        this.antiObservation=antiObservation;
        this.manager.addAxioms(this.ontology,this.antiObservation);
        try {
            URL url = new URL("http://localhost:8080");

            OWLlinkReasonerConfiguration reasonerConfiguration =
                    new OWLlinkReasonerConfiguration(url);
            OWLlinkHTTPXMLReasonerFactory factory = new OWLlinkHTTPXMLReasonerFactory();
            this.reasoner =
                    factory.createNonBufferingReasoner(ontology, reasonerConfiguration);
        }catch (Exception e){
            e.printStackTrace();  //kvoli malformedUrlException
        }
    }

    public static Set<OWLClassExpression> nodeClassSet2classExpSet(Set<Node<OWLClass>> nodeList){
        Set<OWLClassExpression> toReturn = new HashSet<OWLClassExpression>();
        for (Node<OWLClass> node : nodeList){
            toReturn.add(node.getRepresentativeElement());
        }
        return toReturn;
    }
    public static Set<OWLClassExpression> classSet2classExpSet(Set<OWLClass> classSet){
        Set<OWLClassExpression> toReturn = new HashSet<OWLClassExpression>();
        for (OWLClassExpression classExp: classSet){
            toReturn.add(classExp);
        }
        return toReturn;
    }
    public boolean getConsistency(Set<OWLAxiom> axioms){
        for (OWLAxiom axiom : axioms){
            manager.addAxiom(ontology,axiom);
        }
        boolean toReturn=reasoner.isConsistent();
        for (OWLAxiom axiom : axioms){
            manager.removeAxiom(ontology,axiom);
        }
        return toReturn;
    }
    public boolean getConsistencyPure(Set<OWLAxiom> axioms){
        manager.removeAxioms(ontology,antiObservation);
        boolean toReturn = getConsistency(axioms);
        manager.addAxioms(ontology,antiObservation);
        return toReturn;

    }
    public Set<OWLAxiom> getAntiModel(Set<OWLAxiom> axioms){
        //hlada nove typy vzniknute po pridani axiom.
        for (OWLAxiom axiom : axioms){
            manager.addAxiom(ontology,axiom);
        }
        ArrayList<OWLNamedIndividual> individualArray = new ArrayList<OWLNamedIndividual>(ontology.getIndividualsInSignature());
        Set<OWLAxiom> toReturn = new HashSet<OWLAxiom>();
        for (OWLNamedIndividual ind : individualArray) {
            Set<OWLClassExpression> knownTypes = ind.getTypes(ontology);
            Set<OWLClassExpression> notTypes =  classSet2classExpSet(ontology.getClassesInSignature());
            Set<OWLClassExpression> foundTypes=nodeClassSet2classExpSet(reasoner.getTypes(ind,false).getNodes());

            notTypes.removeAll(foundTypes);
            foundTypes.removeAll(knownTypes);
            // Hladame nove typy ktore su, a vsetky typy ktore niesu
            //TODO : hladaj len nove ktore niesu
            for(OWLClassExpression classExpression :foundTypes){
               OWLClassExpression negClassExp = classExpression.getComplementNNF();
               OWLAxiom axiom = dfactory.getOWLClassAssertionAxiom(negClassExp,ind);
               toReturn.add(axiom);
            }
            for(OWLClassExpression classExpression: notTypes){
                OWLAxiom axiom = dfactory.getOWLClassAssertionAxiom(classExpression,ind);
                toReturn.add(axiom);
            }
        }
        //myslim ze by sa hladanie axiom dalo trochu (pravdepodobne zanedbatelne)
        // zefektivnit keby sme odstranovali classexpressions pred vytvorenim axiom.
        for (OWLAxiom axiom : axioms){
            manager.removeAxiom(ontology,axiom);
        }
        return toReturn;
    }
}
