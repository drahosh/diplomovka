package com.company;

//reasoners

import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import javax.swing.text.html.parser.Entity;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;




import static java.util.stream.Collectors.toSet;

public class ReasonerAsker {
    OWLOntology ontology;
    OWLReasoner reasoner;
    OWLOntologyManager manager;
    OWLDataFactory dfactory;
    OWLAxiom antiObservation;
    OWLAxiom observation;
    //Set<OWLAxiom> alreadyFoundDebug;
    String reasonerType;
    boolean noLoops;
    ReasonerAsker(OWLOntology knowledgeBase, OWLOntologyManager manager){
        this.noLoops=!config.loopsAllowed;
        //  this.alreadyFoundDebug=new HashSet<>();
        this.ontology=knowledgeBase;
        this.manager=manager;
        this.dfactory = manager.getOWLDataFactory();
        /*try {
            this.manager.saveOntology(ontology, new SystemOutDocumentTarget());
        }catch(Exception e){
            e.printStackTrace();
        }*/
        this.reasoner=initializeReasoner(ontology);

    }



    ReasonerAsker(OWLOntology knowledgeBase, OWLOntologyManager manager, OWLAxiom observation, OWLAxiom antiObservation){
        //Set<OWLAxiom> observations = new HashSet<>();
        this(knowledgeBase,manager);
        this.observation=observation;
        this.antiObservation=antiObservation ;
        this.manager.addAxiom(this.ontology,this.antiObservation);

    }

    public static Set<OWLClassExpression> nodeClassSet2classExpSet(Set<Node<OWLClass>> nodeList){
        Set<OWLClassExpression> toReturn = new HashSet<OWLClassExpression>();
        for (Node<OWLClass> node : nodeList){
            toReturn.addAll(node.getEntitiesMinusTop());
        }
        //System.out.println(toReturn);
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
        //  System.out.println(axioms);

        reasoner=refresh_Reasoner(reasoner,ontology);
        boolean toReturn=reasoner.isConsistent();
        for (OWLAxiom axiom : axioms){
            manager.removeAxiom(ontology,axiom);

        }
        return toReturn;
    }
    public boolean getConsistencyPositiveObservation(Set<OWLAxiom> axioms){
        manager.removeAxiom(ontology,antiObservation);
        manager.addAxiom(ontology,observation);
        boolean toReturn = getConsistency(axioms);
        manager.removeAxiom(ontology,observation);
        manager.addAxiom(ontology,antiObservation);

        /*System.out.println("did positive ruin it?");
        ArrayList<OWLNamedIndividual> individualArray = new ArrayList<OWLNamedIndividual>(ontology.getIndividualsInSignature());
        for (OWLIndividual ind : individualArray){
            System.out.println(ind);
            System.out.println(EntitySearcher.getTypes(ind,ontology).collect(toSet()));
        }*/
        return toReturn;

    }
    public void changeObservation(OWLAxiom observation,OWLAxiom antiObservation){
        if(this.antiObservation!=null) {
            manager.removeAxiom(this.ontology, this.antiObservation);
        }
        this.antiObservation=antiObservation;
        manager.addAxiom(this.ontology,this.antiObservation);

        //chceme pracovat na vysvetleniach zalozenych na vsetkom okrem sucasne testovaneho pozorovania
        if(this.observation!=null){
            manager.addAxiom(ontology,this.observation);
        }
        this.observation=observation;
        manager.removeAxiom(ontology,observation);
        ArrayList<OWLNamedIndividual> individualArray = new ArrayList<OWLNamedIndividual>(ontology.getIndividualsInSignature());
        /*for (OWLIndividual ind : individualArray){
            System.out.println(ind);
            System.out.println(EntitySearcher.getTypes(ind,ontology).collect(toSet()));
        }*/

    }

    public Set<OWLAxiom> getAntiModel(Set<OWLAxiom> axioms){
        //hlada nove typy vzniknute po pridani axiom.
        manager.addAxioms(ontology,axioms);
        reasoner=refresh_Reasoner(reasoner,ontology);
        ArrayList<OWLNamedIndividual> individualArray = new ArrayList<OWLNamedIndividual>(ontology.getIndividualsInSignature());
        Set<OWLAxiom> toReturn = new HashSet<OWLAxiom>();
        //System.out.println("antimodel for: "+axioms);
        for (OWLNamedIndividual ind : individualArray) {
            //  System.out.println("individual : "+ind);
            Set<OWLClassExpression> ontologyTypes = ind.getTypes(ontology);
            Set<OWLClassExpression> knownTypes=new HashSet<>();
            Set<OWLClassExpression> knownNotTypes = new HashSet<>();
            //  System.out.println("Ontology types: "+ontologyTypes);
            for (OWLClassExpression exp : ontologyTypes){
                if (exp.isClassExpressionLiteral()){

                    if(exp.isAnonymous())
                    //znamena ze exp je jednoducha negacia
                        knownNotTypes.add(exp.getComplementNNF());

                    }else{
                    knownTypes.add((exp));

                }
                }

            //entitySearcher vracia aj negacie
            //musim zistit ci vracia aj AND alebo OR
            // System.out.println("known types "+knownTypes);

            Set<OWLClassExpression> newNotTypes=classSet2classExpSet(ontology.getClassesInSignature());

            newNotTypes.remove(dfactory.getOWLThing());
            newNotTypes.removeAll(knownNotTypes);
            Set<OWLClassExpression> foundTypes = nodeClassSet2classExpSet(reasoner.getTypes(ind, false).getNodes());

            newNotTypes.removeAll(foundTypes);
            foundTypes.removeAll(knownTypes);
            // System.out.println("found types :"+foundTypes);
            //  System.out.println("new notTypes :"+newNotTypes);
            if (foundTypes.size()!=0){
                try {
                    // manager.saveOntology(ontology, new SystemOutDocumentTarget());
                    //  reasoner.getRootOntology().saveOntology(new SystemOutDocumentTarget());
                }catch(Exception e){
                    //not gonna happen
                }
            }


            for (OWLClassExpression classExpression : foundTypes) {
                OWLClassExpression negClassExp = classExpression.getComplementNNF();
                OWLAxiom axiom = dfactory.getOWLClassAssertionAxiom(negClassExp, ind);
                toReturn.add(axiom);
            }
            for (OWLClassExpression classExpression : newNotTypes) {
                OWLAxiom axiom = dfactory.getOWLClassAssertionAxiom(classExpression, ind);
                toReturn.add(axiom);
            }

            //vztahy
            List<OWLObjectProperty> propertyStream = new ArrayList<>(ontology.getObjectPropertiesInSignature());
            for (OWLObjectProperty p : propertyStream ){
                Set<OWLIndividual>  knownRelated = ind.getObjectPropertyValues(p,ontology);
                Set<OWLNamedIndividual> unknownRelated = reasoner.getObjectPropertyValues(ind,p).getFlattened();
                Set<OWLNamedIndividual> notRelated = ontology.getIndividualsInSignature();
                notRelated.removeAll(unknownRelated);
                unknownRelated.removeAll(knownRelated);
                if(this.noLoops){
                    unknownRelated.remove(ind);
                    notRelated.remove(ind);
                }
                for (OWLIndividual relatedInd : unknownRelated){
                    //System.out.println("here it comes");
                    toReturn.add(dfactory.getOWLNegativeObjectPropertyAssertionAxiom(p,ind,relatedInd));
                    //System.out.println(dfactory.getOWLNegativeObjectPropertyAssertionAxiom(p,ind,relatedInd));
                }
                for (OWLIndividual unrelatedInd : notRelated){
                    // System.out.println("debug negation:"+dfactory.getOWLObjectPropertyAssertionAxiom(p,ind,unrelatedInd).getNNF());
                    toReturn.add(dfactory.getOWLObjectPropertyAssertionAxiom(p,ind,unrelatedInd));
                }
            }
        }
        //myslim ze by sa hladanie axiom dalo trochu (pravdepodobne zanedbatelne)
        // zefektivnit keby sme odstranovali classexpressions pred vytvorenim axiom.
        for (OWLAxiom axiom : axioms){
            manager.removeAxiom(ontology,axiom);
        }
        /*for (OWLAxiom ax : toReturn){
            if(!alreadyFoundDebug.contains(ax)){
                alreadyFoundDebug.add(ax);
                System.out.println(ax);
            }
        }*/
        return toReturn;
    }
    public static  OWLReasoner initializeReasoner(OWLOntology ontology){
        try {
            //this.reasonerType="Hermit";
            String reasonerType=config.reasonerType;

          /*  if(reasonerType.equals("Hermit")){
                return new Reasoner.ReasonerFactory().createReasoner(ontology);

            }
            if(reasonerType.equals("Pellet")){
                return openllet.owlapi.OpenlletReasonerFactory.getInstance().createReasoner(ontology);

            }*/

            //OWLlink:

             URL url = new URL("http://localhost:8080");

            OWLlinkReasonerConfiguration reasonerConfiguration =
                    new OWLlinkReasonerConfiguration(url);
            OWLlinkHTTPXMLReasonerFactory factory = new OWLlinkHTTPXMLReasonerFactory();
            OWLReasoner r =factory.createReasoner(ontology,reasonerConfiguration);
            return r;

            //Hermit:
            //this.reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

            //Openllet:
            //this.reasoner=openllet.owlapi.OpenlletReasonerFactory.getInstance().createReasoner(ontology);
            //Openllet can be used as OpenlletReasoner extends OWLReasoner (s .getKB().realize())
        }catch (Exception e){
            e.printStackTrace();  //kvoli malformedUrlException
        }
        return null; //error, asi je zle reasonertype
    }
    public static OWLReasoner refresh_Reasoner(OWLReasoner reasoner, OWLOntology ontology){
        if(config.reasonerType=="Pellet")
        {
            //reasoner=openllet.owlapi.OpenlletReasonerFactory.getInstance().createReasoner(ontology);
        }
        else{reasoner.flush();}
        return reasoner;
    }
}
