package com.company;

import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MultipleObservationParser {
    ArrayList<OWLAxiom> observations;
    ArrayList<OWLAxiom> antiobservations;
    //Set<OWLClass> mentionedClasses;
    Set<OWLNamedIndividual> mentionedIndividuals;
    public MultipleObservationParser(String[] arguments, String baseIriString, OWLDataFactory dfactory){
        mentionedIndividuals=new HashSet<>();
        observations=new ArrayList<>();
        antiobservations=new ArrayList<>();
        for (int i =0; i<arguments.length;i++){
            ObservationParser parser = new ObservationParser(arguments[i],baseIriString,dfactory);
            mentionedIndividuals.addAll(parser.mentionedIndividuals);
            observations.add(parser.observation);
            antiobservations.add(parser.antiobservation);
        }
        System.out.println(mentionedIndividuals);
    }
}