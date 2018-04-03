package com.company;

import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ObservationParser {
    OWLAxiom observation;
    OWLAxiom antiobservation;
    //ArrayList<Set<OWLClass>> simpleObservationLists; /* as observation is in disjunctive normal form, every simpleObservationList
    //contains a set of simple observations for one posibility - simple observation is a,b:R or a:C, hitting set candidate
    //is relevant if it contains no simpleObservationList */
    //ArrayList<Set<OWLClass>> simpleClassLists; //the same as simpleObservationLists but just classes

    Set<OWLNamedIndividual> mentionedIndividuals;
    //Spocita pozorovanie a antipozorovanie, volajuci ho dostane spet cez objekt
    //TODO: multiple observations (asi oddelene bodkociarkov, na kazdy segment pouzit parse, dostat sety
    private OWLClassExpression parseClassExpression(String s, String baseIriString, OWLDataFactory dfactory ){  //,int listnumber)
        int i = s.indexOf('|');
        if (i == -1){
            i=s.indexOf('&');
            if (i ==-1) {
                //just a concept name
                OWLClass c =  dfactory.getOWLClass(IRI.create(baseIriString + "#" + s));
      //          simpleObservationLists.add(dfactory.getOWLClassAssertionAxiom())
                return c;
            }else{
                //concept intersection
                String firstexp = s.substring(0,i);
                String secondexp = s.substring(i+1);

                return dfactory.getOWLObjectIntersectionOf(parseClassExpression(firstexp,baseIriString,dfactory),parseClassExpression(secondexp,baseIriString,dfactory));

            }
        }else{//concept union
            String firstexp = s.substring(0,i);
            String secondexp = s.substring(i+1);
        //    int newListNumber = simpleObservationLists.size();
          //  simpleObservationLists.add(new HashSet<OWLClass>());
            return dfactory.getOWLObjectUnionOf(parseClassExpression(firstexp,baseIriString,dfactory),parseClassExpression(secondexp,baseIriString,dfactory));
        }
    }
    private void parse(String s, String baseIriString,OWLDataFactory dfactory){
        //Assuming its in format "a,b:R" or "a:C"
        String[] halves=s.split(":");
        //TODO: parsing complex objectPropertyExpressions and ClassExpressions
        if(halves[0].contains(",")){
            //rola
            String[] individuals = halves[0].split(",");
            OWLNamedIndividual subject = dfactory.getOWLNamedIndividual(IRI.create(baseIriString+"#"+individuals[0]));
            OWLNamedIndividual object = dfactory.getOWLNamedIndividual(IRI.create(baseIriString+"#"+individuals[1]));
            mentionedIndividuals.add(subject);
            mentionedIndividuals.add(object);
            OWLObjectProperty role = dfactory.getOWLObjectProperty(IRI.create(baseIriString+"#"+halves[1]));
            observation=dfactory.getOWLObjectPropertyAssertionAxiom(role,subject,object);
            antiobservation=dfactory.getOWLNegativeObjectPropertyAssertionAxiom(role,subject,object);
        }else{
            System.out.println(halves[0]);
            //classAssertion
            OWLNamedIndividual individual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString+"#"+halves[0]));
            mentionedIndividuals.add(individual);
            OWLClassExpression owlClassExp = parseClassExpression(halves[1],baseIriString,dfactory);
            observation=dfactory.getOWLClassAssertionAxiom(owlClassExp,individual);
            antiobservation=dfactory.getOWLClassAssertionAxiom(owlClassExp.getComplementNNF(),individual);
        }


    }
    ObservationParser(String s, String baseIriString, OWLDataFactory dfactory){
      //  simpleObservationLists = new ArrayList<>();
        //simpleObservationLists.add(new HashSet<>());
        mentionedIndividuals = new HashSet<>();
        parse(s,baseIriString,dfactory);
        //System.out.println(simpleObservationLists);
        System.out.println(observation);
        System.out.println(antiobservation);
    }
}
