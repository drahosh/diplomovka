package com.company;

import com.google.common.base.Stopwatch;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.apibinding.OWLManager;

import java.io.File;
import java.util.*;

public class Main {



    public static void the_algorithm_simple(OWLOntology ontology, OWLOntologyManager manager, OWLAxiom observation, OWLAxiom observationComplement, int maxDepth) {
        //The algorithm for a single observation


        if (ontology.containsAxiom(observationComplement)) {
            System.out.println("pozorovanie je priamo nekonzistentne s knowledge base");
            return;
        }


        ReasonerAsker asker = new ReasonerAsker(ontology, manager, observation, observationComplement);
        /**************
         * metrics
         *************/
        if (config.metrics) {
            Stopwatch consistencyTimer = Stopwatch.createUnstarted();
            Stopwatch antimodelTimer = Stopwatch.createUnstarted();
            int reasonerConsistencyCalls = 2;
            int reasonerRealisationcalls = 0;
            asker.passMetrics(consistencyTimer, antimodelTimer, reasonerConsistencyCalls, reasonerRealisationcalls);
        }
        /***********************************
         * initializations and trivial cases
         ***********************************/

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


        /*******************
         * Main loop *******
         *******************/
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
            /*******************
             * printing results
             *******************/
          //  manager.saveOntology(ontology, new SystemOutDocumentTarget());
            System.out.println("Hitting sets:");
            hittingSetsManager.relevancy_test(observationComplement);
            for (Set<OWLAxiom> hittingSet: hittingSetsManager.getHittingSets()){
                printAxiomSet(hittingSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(hittingSetsManager.getHittingSets());

        }
        /*******************
         * printing metrics
         *******************/
        if(config.metrics) {
            System.out.println("number of minimal inconsistent candidates: " + hittingSetsManager.minimal_inconsistent_candidates.size());
            System.out.println("number of unique candidates tested: " + hittingSetsManager.candidatesAlreadyTested.size());
            System.out.println("reasoner calls for consistency testing:  " + asker.consistencyCalls);
            System.out.println("time spent by reasoner for consistency calls :" + asker.consistencyTimer);
            System.out.println("function calls for getAntiModel: "+asker.antiModelCalls);
            System.out.println("time spent: " +asker.antiModelTimer);
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
                    System.out.println("Pozorovanie vyplyva z knowledge base");
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
           // System.out.println(hittingSets.size());
            List<Set<OWLAxiom>> finalHittingSets = (build_final_hitting_sets(hittingSets));
            Set<Set<OWLAxiom>> finalHittingSetsTest = new HashSet<>(finalHittingSets);
           /* for( int i =0; i< finalHittingSets.size();i++){
                for (int ii=0; ii<finalHittingSets.size();ii++){
                    if(i==ii) continue;
                    if (finalHittingSets.get(i).containsAll(finalHittingSets.get(ii))){
                        finalHittingSets.remove(i);
                        i--;
                        //removing nonminimal explanations and duplicates
                        //TODO: rethink, maybe too slow when large ammount of explanations, maybe filter during creation somehow
                    }
                }
            }*/
            //TODO: asi bude lepsie robit so setmi aj v build_final_hitting_sets
            System.out.println("final hitting sets: ");

            for(Set<OWLAxiom> hittingSet : finalHittingSetsTest){
                printAxiomSet(hittingSet);
            }
            System.out.println("number of explanations: "+finalHittingSetsTest.size());

        }
    public static void printAxiomSet(Set<OWLAxiom>axioms){
        String t="{";
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
        t+="}";
        System.out.println(t);
    }
    public static List<Set<OWLAxiom>> build_final_hitting_sets(LinkedList<List<Set<OWLAxiom>>> hittingSets){

        List<Set<OWLAxiom>>forThisObservation=hittingSets.removeFirst();
        if(hittingSets.size()==0){
            return forThisObservation;
        }else{

            LinkedList<Set<OWLAxiom>> fullExplanationBuilder = new LinkedList<>();
            List<Set<OWLAxiom>> forHigherObservations = build_final_hitting_sets(hittingSets);
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

    //Todo : spisovat poznamky ku rozhodnutiam ku kodu
    //Todo: porovnania reasonerov (cas) + s jarkom
    public static void normal_main(String[] args){

        String inputfile="not loaded";
        String observationString="not loaded";
        String[] mObservationsStrings=null;
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
            if(args[i].equals("-mobs")){
                int maxIndex=i;
                while (args[maxIndex+1].charAt(0)!='-'){
                    maxIndex++;
                }
                config.multipleObservations=true;
                mObservationsStrings=Arrays.copyOfRange(args,i+1,maxIndex+1);
                i=maxIndex;
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
            if(observationString.equals("not loaded") && config.multipleObservations == false){
                System.out.println("You need to provide an observation (-obs individual:Concept  or -obs individual,individual:ObjectProperty)");
            }
            if(mObservationsStrings == null && config.multipleObservations == true ){
                System.out.println("You need to provide some number of observations after -mobs");
            }
            if(depth==1000){
                System.out.println("Warning: depth is set to default (1000), this may take too long, consider setting it lower, like with argument (-d 2)");
            }
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(inputfile));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            if(config.multipleObservations){
                MultipleObservationParser multiParser = new MultipleObservationParser(mObservationsStrings,baseIriString,dfactory);
                for (OWLNamedIndividual ind : multiParser.mentionedIndividuals){
                    manager.addAxiom(ontology,dfactory.getOWLDeclarationAxiom(ind));
                }
                ontology.saveOntology(new SystemOutDocumentTarget());
                the_algorithm(ontology,manager,multiParser.observations,multiParser.antiobservations,depth);
            }
            else {
                ObservationParser p = new ObservationParser(observationString, baseIriString, dfactory);
                //  System.out.println(p.observation);
                //  System.out.println(p.antiobservation);
                // ontology.saveOntology(new SystemOutDocumentTarget());
                for (OWLNamedIndividual ind :p.mentionedIndividuals){
                    manager.addAxiom(ontology,dfactory.getOWLDeclarationAxiom(ind));
                }
                the_algorithm_simple(ontology, manager, p.observation, p.antiobservation, depth);
            }
            }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //TODO: pri pozorovaniach dat jednotlivce do KB z ostatnych pozorovani
        /*TODO text - zbytocne roztiahnute priklady, najprov bez definicie a potom prilis vela, mozno prilis vela prikladov, priklad bez uvodu co tym chceme ukazat, rozvinut vety aby boli plnovyznamove (
        (namiesto "examples of concepts) dat normlalnu vetu    .   Priklad 2.2.4 - uviest do textu, ohlavickovane priklady len pre vyznamne s textom, - rozpisat, upratat, ujednotit ormat
        Pri tableau zmenit priklad, demonstrovat beh v zatvorkach namiesto textu

        Abduction - chybaju zakladne veci, definovat abduktivny problem, typy problemov, preco len ABOX abduction, dat vsetky definicie. Nejaky sprievodny text - "zamysli sa" - strasne zhurta, daco
        z hystorie, filozofie, po tom rozdeleni nejake typy preco sa venovat.   Citaj abstrakty a uvody literatury, mozno mi to dojde
         

        */

        normal_main(args);
        //tests.test15();
       // tests.test18();
        //
        //
       // config.reasonerType="Pellet";
     //   tests.test19();
      //  tests.test20();
    }
}

/*
porovnanie konjunkcie ku viacerim pozorovaniam
 */