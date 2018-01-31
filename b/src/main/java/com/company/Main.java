package com.company;

import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;

import static java.util.stream.Collectors.toSet;

public class Main {


    public static void algorithm_ClassAssertion(String filepath, String indString, String classString, int maxDepth) {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(filepath));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + indString));
            OWLClassExpression observationClassExp = dfactory.getOWLClass(IRI.create(baseIriString + "#" + classString));
            OWLAxiom observationOne = dfactory.getOWLClassAssertionAxiom(observationClassExp, observationIndividual);
            OWLAxiom observationOneComplement = dfactory.getOWLClassAssertionAxiom(observationClassExp.getComplementNNF(), observationIndividual);
            System.out.println(observationOne);
            System.out.println(observationOneComplement);
            the_algorithm_simple(ontology, manager, observationOne, observationOneComplement, maxDepth);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void the_algorithm_simple(OWLOntology ontology, OWLOntologyManager manager, OWLAxiom observation, OWLAxiom observationComplement, int maxDepth) {

        if (ontology.containsAxiom(observationComplement)) {
            System.out.println("pozorovanie je priamo nekonzistentne s knowledge base");
            return;
        }
        ReasonerAsker asker = new ReasonerAsker(ontology, manager, observation, observationComplement);

        HittingSetsManager hittingSetsManager = new HittingSetsManager(asker, observation);


        Queue<Set<OWLAxiom>> hittingSetCandidates = new LinkedList<Set<OWLAxiom>>();
        Queue<Set<OWLAxiom>> nextHittingSetCandidates = new LinkedList<Set<OWLAxiom>>();
        Set<OWLAxiom> emptySet = new HashSet<OWLAxiom>();

        if (!asker.getConsistencyPositiveObservation(emptySet)) {
            System.out.println("Pozorovanie je nekonzistentne s knowledge base");
            return;
        }
        if (!asker.getConsistency(emptySet)) {
            System.out.println("Pozorovanie vypliva z knowledge base");
            return;
        }
        if (ontology.containsAxiom(observation)) {
            System.out.println("Pozorovanie uz je sucastou knowledge base");
            return;
        }

        hittingSetCandidates.add(emptySet);

        try {
            for (int depth = 0; depth <= maxDepth; depth++) {
                System.out.println("depth is " + depth);
                System.out.println("candidates to test: " + hittingSetCandidates.size());
                if(hittingSetCandidates.size()==0){
                    break;
                }
                while (!hittingSetCandidates.isEmpty()) {
                    if (hittingSetCandidates.size() % 1000 == 0) {
                        System.out.println("candidates left: " + hittingSetCandidates.size());
                    }
                    Set<OWLAxiom> axioms = hittingSetCandidates.remove();
                    if (!hittingSetsManager.minimality_test(axioms)) {
                        continue;
                    }
                    if (!hittingSetsManager.positiveConsistency_test(axioms)) {
                        continue;
                    }


                    if (!hittingSetsManager.addHittingSetIfInconsistent(axioms)) {

                        /*}else{
                            if(!asker.getConsistency(axioms)){
                                continue;
                            }
                        }*/
                        if (depth == maxDepth) {
                            continue;
                        }
                        try {
                            Set<OWLAxiom> antimodel = asker.getAntiModel(axioms);

                            //System.out.println(antimodel.size());
                            for (OWLAxiom axiom : antimodel) {
                                if (!axioms.contains(axiom)) {
                                    // nexceme nahodou pridat co uz vieme, ani pokusit sa testovat to iste dvakrat,
                                    // ani ak vieme ze nadmnoziny nebudu validnym hitting setom

                                    Set<OWLAxiom> newHittingSetCandidate = new HashSet<OWLAxiom>(axioms);
                                    newHittingSetCandidate.add(axiom);
                                    if (hittingSetsManager.firstTest(newHittingSetCandidate)) {
                                        nextHittingSetCandidates.add(newHittingSetCandidate);
                                        //}
                                    }
                                }


                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(axioms);
                        }
                    }

                }
                hittingSetCandidates = nextHittingSetCandidates;
                nextHittingSetCandidates = new LinkedList<Set<OWLAxiom>>();

            }
          //  manager.saveOntology(ontology, new SystemOutDocumentTarget());
            System.out.println("Hitting sets:");
            List<Set<OWLAxiom>> hittingSets= hittingSetsManager.getHittingSets();
            hittingSets=HittingSetsManager.relevancy_test(hittingSets,observationComplement);
            for (Set<OWLAxiom> hittingSet:hittingSetsManager.getHittingSets()){
                printAxiomSet(hittingSet);
            }
            System.out.println("minimal inconsistent candidates: "+hittingSetsManager.minimal_inconsistent_candidates.size());
            System.out.println("number of candidates tested: "+hittingSetsManager.candidatesAlreadyTested.size());
           // System.out.println(hittingSetsManager.candidatesAlreadyTested);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(hittingSetsManager.getHittingSets());

        }
    }



        public static void the_algorithm(OWLOntology ontology, OWLOntologyManager manager, List<OWLAxiom> observations, List<OWLAxiom> observationComplements, int maxDepth) {


            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();

            ReasonerAsker asker = new ReasonerAsker(ontology,manager);
            LinkedList<List<Set<OWLAxiom>>> hittingSets = new LinkedList<List<Set<OWLAxiom>>>();
            List<Set<OWLAxiom>> minimalInconsistentCandidates = new ArrayList<Set<OWLAxiom>>();
            try {
              //  manager.saveOntology(ontology, new SystemOutDocumentTarget());
            }catch(Exception e){
                e.printStackTrace();
            }

            /*vysvetlenie nasledovneho riadku: chceme hladat vysvetlenia ktore vyplivaju nielen z bazy znalosti ale aj z momentalne netestovanych pozorovani.
            Do ontologie teda dame vsetky pozorovania, a ked v nasledujucom for cykle davame askeru pozorovania, tak si sam odstrani sucasne testovane pozorovanie
            a prida k nemu jeho antipozorovanie.

            Preco chceme na zaklade ostatnych pozorovani? Kvoli testu 1, pre pozorovania b:D, b:F, b:F nema vysvetlenie a b:D by malo aj vysvetlenie b:E,b:F, ale to nieje minimalne,
            a s b:F by malo b:E, ale to samo nevysvetluje b:D
             */
            manager.addAxioms(ontology,observations);

            for (int observationNumber = 0; observationNumber < observations.size(); observationNumber++) {
                asker.changeObservation(observations.get(observationNumber),observationComplements.get(observationNumber));
               /* try {
                    manager.saveOntology(ontology, new SystemOutDocumentTarget());
                }catch(Exception e){
                    e.printStackTrace();
                }*/
                System.out.println("--------------------------------------------------------------------------------------------------");

                System.out.println("observation : "+observations.get(observationNumber));

                HittingSetsManager hittingSetsManager = new HittingSetsManager(asker, observations);
                hittingSetsManager.setMinimal_inconsistent_candidates(minimalInconsistentCandidates);
                Queue<Set<OWLAxiom>> hittingSetCandidates = new LinkedList<Set<OWLAxiom>>();
                Queue<Set<OWLAxiom>> nextHittingSetCandidates = new LinkedList<Set<OWLAxiom>>();
                Set<OWLAxiom> emptySet = new HashSet<OWLAxiom>();

                if (!asker.getConsistencyPositiveObservation(emptySet)) {
                    System.out.println("Pozorovanie je nekonzistentne s knowledge base");
                    return;
                }
                if (!asker.getConsistency(emptySet)) {
                    System.out.println("Pozorovanie vypliva z knowledge base a ostatnych pozorovani");
                    continue;
                }
                Set<OWLAxiom> otherObservations = new HashSet<>(observations);
                otherObservations.remove(observations.get(observationNumber));
                System.out.println("Other observations: "+otherObservations);

                if (ontology.containsAxiom(observations.get(observationNumber))) {
                    System.out.println("Pozorovanie uz je sucastou knowledge base");
                    continue;

                }


                hittingSetCandidates.add(emptySet);

                try {
                    for (int depth = 0; depth <= maxDepth; depth++) {
                        System.out.println("depth is " + depth);
                        System.out.println("candidates to test: " + hittingSetCandidates.size());
                        if(hittingSetCandidates.size()==0){
                            break;
                        }
                        while (!hittingSetCandidates.isEmpty()) {
                            if (hittingSetCandidates.size() % 1000 == 0) {
                                System.out.println("candidates left: " + hittingSetCandidates.size());
                            }
                            Set<OWLAxiom> axioms = hittingSetCandidates.remove();
                            if (!hittingSetsManager.minimality_test(axioms)) {
                                continue;
                            }
                            if (!hittingSetsManager.positiveConsistency_test(axioms)) {
                                continue;
                            }


                            if (!hittingSetsManager.addHittingSetIfInconsistent(axioms)) {

                        /*}else{
                            if(!asker.getConsistency(axioms)){
                                continue;
                            }
                        }*/
                                if (depth == maxDepth) {
                                    continue;
                                }
                                try {
                                    Set<OWLAxiom> antimodel = asker.getAntiModel(axioms);

                                    //System.out.println(antimodel.size());
                                    for (OWLAxiom axiom : antimodel) {
                                        if (!axioms.contains(axiom)) {
                                            // nexceme nahodou pridat co uz vieme, ani pokusit sa testovat to iste dvakrat,
                                            // ani ak vieme ze nadmnoziny nebudu validnym hitting setom

                                            Set<OWLAxiom> newHittingSetCandidate = new HashSet<OWLAxiom>(axioms);
                                            newHittingSetCandidate.add(axiom);
                                            if (hittingSetsManager.firstTest(newHittingSetCandidate)) {
                                                nextHittingSetCandidates.add(newHittingSetCandidate);
                                                nextHittingSetCandidates.size();
                                                //}
                                            }
                                        }


                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println(axioms);
                                }
                            }

                        }
                        hittingSetCandidates = nextHittingSetCandidates;
                        nextHittingSetCandidates = new LinkedList<Set<OWLAxiom>>();

                    }
                    List<Set<OWLAxiom>> hittingSetsForObs = hittingSetsManager.getHittingSets();
                    hittingSets.addLast(hittingSetsForObs);
                    minimalInconsistentCandidates=hittingSetsManager.getMinimal_inconsistent_candidates();
                    //manager.saveOntology(ontology, new SystemOutDocumentTarget());
                    System.out.println("number of minimal inconsistent candidates: " + minimalInconsistentCandidates.size());
                    System.out.println("minimal inconsistent candidates: "+minimalInconsistentCandidates);
                    for(Set<OWLAxiom> hittingSet:hittingSetsForObs){
                        printAxiomSet(hittingSet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(hittingSetsManager.getHittingSets());

                }
                if(hittingSets.getLast().size()==0){
                    System.out.println("Pre pozorovanie číslo "+observationNumber +" neexistuje vysvetlenie");
                    return;
                }
            }


            //now we get explanations together
            System.out.println(hittingSets.size());
            Set<Set<OWLAxiom>> finalHittingSets = new HashSet<>(build_final_hitting_sets(hittingSets)); //to remove duplicates
            //TODO: asi bude lepsie robit so setmi aj v build_final_hitting_sets
            System.out.println("final hitting sets: ");

            for(Set<OWLAxiom> hittingSet : finalHittingSets){
                printAxiomSet(hittingSet);
            }
        }
    public static void printAxiomSet(Set<OWLAxiom>axioms){
        String t="";
        Set<OWLAxiom> assertionAxioms = AxiomType.getAxiomsOfTypes(axioms,AxiomType.CLASS_ASSERTION);
        Set<OWLAxiom> propertyAxioms = AxiomType.getAxiomsOfTypes(axioms,AxiomType.OBJECT_PROPERTY_ASSERTION);
        for(OWLAxiom assertionAxiom : assertionAxioms){
           OWLClassAssertionAxiom assertionAxiomf=(OWLClassAssertionAxiom)assertionAxiom;
           OWLNamedIndividual namedin = assertionAxiomf.getIndividual().asOWLNamedIndividual();
            t+=namedin.getIRI().getFragment()+":";
            OWLClassExpression c = assertionAxiomf.getClassExpression(); //assuming we wont modify algorithm to get
            //more complex explanations
            //in other words, assuming c.isLiteral is true
            if (c.isOWLClass()){

                t+=c.asOWLClass().getIRI().getFragment()+"; ";

            }else{
                t+="~"+c.getComplementNNF().asOWLClass().getIRI().getFragment()+": ";
            }

        }
        for (OWLAxiom propertyAxiom : propertyAxioms){
            OWLObjectPropertyAssertionAxiom propertyAxiomf = (OWLObjectPropertyAssertionAxiom) propertyAxiom;
            t+=propertyAxiomf.getSubject().asOWLNamedIndividual().getIRI().getFragment()+",";
            t+=propertyAxiomf.getObject().asOWLNamedIndividual().getIRI().getFragment()+":";
            t+=propertyAxiomf.getProperty().asOWLObjectProperty().getIRI().getFragment()+"; ";

        }
        t=t.substring(0, t.length() - 2); // odstranenie poslednej bodkociarky a medzeri
        System.out.println(t);
    }
    public static List<Set<OWLAxiom>> build_final_hitting_sets(LinkedList<List<Set<OWLAxiom>>> hittingSets){
        System.out.println("f");

        List<Set<OWLAxiom>>forThisObservation=hittingSets.removeFirst();
        if(hittingSets.size()==0){
            System.out.println("aaa");
            return forThisObservation;
        }else{
            System.out.println("ccc");

            LinkedList<Set<OWLAxiom>> fullExplanationBuilder = new LinkedList<>();
            List<Set<OWLAxiom>> forHigherObservations = build_final_hitting_sets(hittingSets);
            System.out.println("eee");
            for (Set<OWLAxiom> thisExp : forThisObservation){
                for(Set<OWLAxiom> higherExp : forHigherObservations){
                    Set<OWLAxiom> explanationUnion = new HashSet<>(thisExp);
                    explanationUnion.addAll(higherExp);
                    fullExplanationBuilder.addLast(explanationUnion);
                }
            }
            return fullExplanationBuilder;

        }
    }

    //TODO: citatelne vypisy
    //Todo : spisovat poznamky ku rozhodnutiam ku kodu
    //Todo: porovnania reasonerov (cas) + s jarkom
    public static void main(String[] args) {
     String inputfile="not loaded";
     String observationString="not loaded";
     int depth = 1000;
     for(int i=0;i<args.length;i++){
        // System.out.println(args[i]);
        if (args[i].equals("-i")){
            inputfile=args[i+1];
            i++;
            continue;
        }
        if(args[i].equals( "-obs" )){
            observationString=args[i+1];
            i++;
            continue;
        }
        if(args[i].equals("-r")){
            if(config.reasonerTypeList.contains(args[i+1])){
                config.reasonerType=args[i+1];
            }else{
                System.out.println("Warning: can't understant reasoner "+args[i+1]+", will use default ("+config.reasonerType+") instead");
            }
            i++;
            continue;
        }
        if(args[i].equals("-l")){
            config.loopsAllowed=true;
            continue;
        }
        if (args[i].equals("-d")){
            depth=Integer.parseInt(args[i+1]);
            i++;
            continue;
        }
        System.out.println("I dont understand argument "+args[i]);
        return;
     }
     try {
         if(inputfile.equals("not loaded")){
             System.out.println("You need to specify input file as argument (-i filepath)");
            return;
         }
         if(observationString.equals("not loaded")){
             System.out.println("You need to provide an observation (-obs individual:Concept  or -obs individual,individual:ObjectProperty)");
         }
         if(depth==1000){
             System.out.println("Warning: depth is set to default (1000), this may take too long, consider setting it lower, like with argument (-d 2)");
         }
         OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
         final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(inputfile));
         String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
         OWLDataFactory dfactory = manager.getOWLDataFactory();

         ObservationParser p = new ObservationParser(observationString,baseIriString,dfactory);
       //  System.out.println(p.observation);
       //  System.out.println(p.antiobservation);
        // ontology.saveOntology(new SystemOutDocumentTarget());
         the_algorithm_simple(ontology,manager,p.observation,p.antiobservation,depth);
     }catch(Exception e){
         e.printStackTrace();
     }
         }
}


/*
porovnanie konjunkcie ku viacerim pozorovaniam
 */