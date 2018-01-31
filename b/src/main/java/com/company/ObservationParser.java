package com.company;

import org.semanticweb.owlapi.model.*;

public class ObservationParser {
    OWLAxiom observation;
    OWLAxiom antiobservation;

    //Spocita pozorovanie a antipozorovanie, volajuci ho dostane spet cez objekt
    //TODO: multiple observations (asi oddelene bodkociarkov, na kazdy segment pouzit parse, dostat sety
    public void parse(String s, String baseIriString,OWLDataFactory dfactory){
        //Assuming its in format "a,b:R" or "a:C"
        String[] halves=s.split(":");
        //TODO: parsing complex objectPropertyExpressions and ClassExpressions
        if(halves[0].contains(",")){
            //rola
            String[] individuals = halves[0].split(",");
            OWLNamedIndividual subject = dfactory.getOWLNamedIndividual(IRI.create(baseIriString+"#"+individuals[0]));
            OWLNamedIndividual object = dfactory.getOWLNamedIndividual(IRI.create(baseIriString+"#"+individuals[1]));
            OWLObjectProperty role = dfactory.getOWLObjectProperty(IRI.create(baseIriString+"#"+halves[1]));
            observation=dfactory.getOWLObjectPropertyAssertionAxiom(role,subject,object);
            antiobservation=dfactory.getOWLNegativeObjectPropertyAssertionAxiom(role,subject,object);
        }else{
            //classAssertion
            OWLNamedIndividual individual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString+"#"+halves[0]));
            OWLClass owlClass = dfactory.getOWLClass(IRI.create(baseIriString+"#"+halves[1]));
            observation=dfactory.getOWLClassAssertionAxiom(owlClass,individual);
            antiobservation=dfactory.getOWLClassAssertionAxiom(owlClass.getComplementNNF(),individual);
        }


    }
    ObservationParser(String s, String baseIriString, OWLDataFactory dfactory){
        parse(s,baseIriString,dfactory);
    }
}
