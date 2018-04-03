package com.company;

//reasoners
import com.google.common.base.Stopwatch;
import org.semanticweb.HermiT.Reasoner;
import openllet.owlapi.OWL;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
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

    Stopwatch consistencyTimer; //time spent by and number of calls
    int consistencyCalls;    //    to reasoner for consistency
    Stopwatch  antiModelTimer;//time spent and ammount of calls
    int antiModelCalls; //of the getAntiModel function (multiple reasoner calls, time not only spent on reasoner)
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
        this(knowledgeBase,manager);
        this.observation=observation;
        this.antiObservation=antiObservation ;
            this.manager.addAxiom(this.ontology,this.antiObservation);

        }
    public void passMetrics(Stopwatch consistencyTimer, Stopwatch antiModelTimer, int consistencyCalls, int modelCalls){
            //pass namiesto create kvoli viacerym pozorovaniam
            this.consistencyTimer=consistencyTimer;
            this.antiModelTimer=antiModelTimer;
            this.consistencyCalls=consistencyCalls;
            this.antiModelCalls=modelCalls;
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
        if(config.metrics){
            this.consistencyCalls++;
            this.consistencyTimer.start();
        }
        reasoner=refresh_Reasoner(reasoner,ontology);

        boolean toReturn=reasoner.isConsistent();
        if(config.metrics){
            this.consistencyTimer.stop();
        }
        for (OWLAxiom axiom : axioms){
            ontology.removeAxiom(axiom);
        }

        return toReturn;
    }
    public boolean getConsistencyPositiveObservation(Set<OWLAxiom> axioms){
        ontology.removeAxiom(antiObservation); //expecting getConsistency to flush
        ontology.addAxiom(observation);
        boolean toReturn = getConsistency(axioms);
        ontology.removeAxiom(observation);
        ontology.addAxiom(antiObservation);
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
        this.observation=observation;

        //ArrayList<OWLNamedIndividual> individualArray = new ArrayList<OWLNamedIndividual>(ontology.getIndividualsInSignature());
        /*for (OWLIndividual ind : individualArray){
            System.out.println(ind);
            System.out.println(EntitySearcher.getTypes(ind,ontology).collect(toSet()));
        }*/

    }

    public Set<OWLAxiom> getAntiModel(Set<OWLAxiom> axioms){
        //hlada nove typy vzniknute po pridani axiom.
        if(config.metrics){
            antiModelCalls++;
            antiModelTimer.start();
        }
        ontology.addAxioms(axioms);
        reasoner=refresh_Reasoner(reasoner,ontology);
        ArrayList<OWLNamedIndividual> individualArray = new ArrayList<OWLNamedIndividual>(ontology.getIndividualsInSignature());
        Set<OWLAxiom> toReturn = new HashSet<OWLAxiom>();
        //System.out.println("antimodel for: "+axioms);
        for (OWLNamedIndividual ind : individualArray) {
          //  System.out.println("individual : "+ind);
            Set<OWLClassExpression> ontologyTypes = EntitySearcher.getTypes(ind,ontology).collect(toSet());
            Set<OWLClassExpression> knownTypes=new HashSet<>();
            Set<OWLClassExpression> knownNotTypes = new HashSet<>();
          //  System.out.println("Ontology types: "+ontologyTypes);
            for (OWLClassExpression exp : ontologyTypes){
                if (exp.isClassExpressionLiteral()){
                    if(exp.isOWLClass()){
                        knownTypes.add((exp));
                    }else{
                        knownNotTypes.add(exp.getComplementNNF());
                    }
                }
            }
            /*TODO: dat pod knowntypes aj ine class expressions (konjunkcie) - moze zrychlit pre ontologie
              ktore obsahuju konjunkcie*/
            //entitySearcher vracia aj negacie
            //musim zistit ci vracia aj AND alebo OR
           // System.out.println("known types "+knownTypes);
            Set<OWLClassExpression> newNotTypes=classSet2classExpSet(ontology.classesInSignature().collect(toSet()));

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
            List<OWLObjectProperty> propertyStream = ontology.objectPropertiesInSignature().collect(Collectors.toList());
            for (OWLObjectProperty p : propertyStream ){
                Set<OWLIndividual>  knownRelated = EntitySearcher.getObjectPropertyValues(ind,p,ontology).collect(Collectors.toSet());
                Set<OWLIndividual> unknownRelated = reasoner.getObjectPropertyValues(ind,p).entities().collect(toSet());
                Set<OWLIndividual> notRelated = ontology.individualsInSignature().collect(toSet());
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

            ontology.removeAxiom(axiom);
        }
        /*for (OWLAxiom ax : toReturn){
            if(!alreadyFoundDebug.contains(ax)){
                alreadyFoundDebug.add(ax);
                System.out.println(ax);
            }
        }*/
        if(config.metrics){
            antiModelTimer.stop();
        }
        return toReturn;
    }
    public static OWLReasoner initializeReasoner(OWLOntology ontology){
        try {
            //this.reasonerType="Hermit";
            String reasonerType=config.reasonerType;

            if(reasonerType.equals("Hermit")){
                 return new Reasoner.ReasonerFactory().createReasoner(ontology);

            } //openllet
            if(reasonerType.equals("Pellet")){
                return openllet.owlapi.OpenlletReasonerFactory.getInstance().createReasoner(ontology);

            }
            //OWLlink:
            /*
             URL url = new URL("http://localhost:8080");

            OWLlinkReasonerConfiguration reasonerConfiguration =
                    new OWLlinkReasonerConfiguration(url);
            OWLlinkHTTPXMLReasonerFactory factory = new OWLlinkHTTPXMLReasonerFactory();
            this.reasoner =factory.createReasoner(ontology,reasonerConfiguration);
            */

        }catch (Exception e){
            e.printStackTrace();  //kvoli malformedUrlException
        }
        return null; //error, asi je zle reasonertype
    }
    public static OWLReasoner refresh_Reasoner(OWLReasoner reasoner, OWLOntology ontology){
        if(config.reasonerType.equals("Pellet"))
        {
            reasoner=openllet.owlapi.OpenlletReasonerFactory.getInstance().createReasoner(ontology);}
        else{reasoner.flush();}
        return reasoner;
    }
}
