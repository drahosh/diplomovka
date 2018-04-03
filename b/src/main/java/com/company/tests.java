package com.company;

import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toSet;

public class tests {
    //TODO: automatic testing
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
            Main.the_algorithm_simple(ontology, manager, observationOne, observationOneComplement, maxDepth);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    public static void test1() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-2ind.owl",
                "b",
                "E",
                8);
        //a:C, a:E, a:F, (b: E ,b: F)
    }

    public static void test1_2() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-2ind.owl.xml",
                "g",
                "D",
                4);
    }
    public static void test1_3(){
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-2ind.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual b = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "b"));
            OWLClassExpression D = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
            OWLClassExpression E = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "E"));
            manager.addAxiom(ontology,dfactory.getOWLClassAssertionAxiom(E,b));
            OWLAxiom observationOne = dfactory.getOWLClassAssertionAxiom(D, b);
            OWLAxiom observationOneComplement = dfactory.getOWLClassAssertionAxiom(D.getComplementNNF(), b);

            Main.the_algorithm_simple(ontology, manager, observationOne, observationOneComplement, 3);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void test2() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/people+pets.owl",
                "a",
                "Pet",
                1);
    }

    public static void test3() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/family.owl",
                "mary",
                "Mother",
                5);
    }

    public static void test4() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/simpletestFamily.owl",
                "mary",
                "Mother",
                5);
    }


    public static void test5() {
        //mother with one "anonymous" individual
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/simpletestFamily.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "mary"));
            OWLClassExpression observationClassExp = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Mother"));
            OWLAxiom observationOne = dfactory.getOWLClassAssertionAxiom(observationClassExp, observationIndividual);
            OWLAxiom observationOneComplement = dfactory.getOWLClassAssertionAxiom(observationClassExp.getComplementNNF(), observationIndividual);
            System.out.println(observationOne);
            System.out.println(observationOneComplement);
            manager.addAxiom(ontology, dfactory.getOWLDeclarationAxiom(dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymous1"))));
            Main.the_algorithm_simple(ontology, manager, observationOne, observationOneComplement, 3);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void test6() {
        //grandmother with several "anonymous" individuals
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/simpletestFamily.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "mary"));
            OWLClassExpression observationClassExp = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Grandmother"));
            OWLAxiom observationOne = dfactory.getOWLClassAssertionAxiom(observationClassExp, observationIndividual);
            OWLAxiom observationOneComplement = dfactory.getOWLClassAssertionAxiom(observationClassExp.getComplementNNF(), observationIndividual);
            System.out.println(observationOne);
            System.out.println(observationOneComplement);
            manager.addAxiom(ontology, dfactory.getOWLDeclarationAxiom(dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymous1"))));
            manager.addAxiom(ontology, dfactory.getOWLDeclarationAxiom(dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymous2"))));
            manager.addAxiom(ontology, dfactory.getOWLDeclarationAxiom(dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymous3"))));


            Main.the_algorithm_simple(ontology, manager, observationOne, observationOneComplement, 3);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void test7() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/simpletest_vegetarian3.owl",
                "gary",
                "Vegetarian",
                3);
    }

    public static void test8() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/simpletest_vegetarian.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "gary"));
            OWLClassExpression observationClassExp = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Vegetarian"));
            OWLAxiom observationOne = dfactory.getOWLClassAssertionAxiom(observationClassExp, observationIndividual);
            OWLAxiom observationOneComplement = dfactory.getOWLClassAssertionAxiom(observationClassExp.getComplementNNF(), observationIndividual);
            System.out.println(observationOne);
            System.out.println(observationOneComplement);
            manager.addAxiom(ontology, dfactory.getOWLDeclarationAxiom(dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymous1"))));
            manager.addAxiom(ontology, dfactory.getOWLDeclarationAxiom(dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymous2"))));
            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Plant")), dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymouus1"))));
            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Meat")), dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymouus2"))));

            Main.the_algorithm_simple(ontology, manager, observationOne, observationOneComplement, 5);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void test9() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/role-ass.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
            OWLNamedIndividual observationIndividual2 = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "b"));
            OWLObjectProperty r2 = dfactory.getOWLObjectProperty(IRI.create(baseIriString + "#" + "R2"));
            OWLAxiom observationOne = dfactory.getOWLObjectPropertyAssertionAxiom(r2, observationIndividual, observationIndividual2);
            OWLAxiom observationOneComplement = dfactory.getOWLNegativeObjectPropertyAssertionAxiom(r2, observationIndividual, observationIndividual2);
            System.out.println(observationOne);
            System.out.println(observationOneComplement);
            Main.the_algorithm_simple(ontology, manager, observationOne, observationOneComplement, 5);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void test10() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/ten.owl",
                "a",
                "D",
                15);
        // {a: A, a: B, a: C, a: E, a: F, a: G, a: H, a: I, a: J, a: K}
        //nefunguje v ani jednom z troch reasonerov
    }
    /*
        public static void test10_2() {
            //nefunguje v ani jednom z troch reasonerov -- nvm, zle som pochopil
            try {
                OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/ten.owl"));
                String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
                OWLDataFactory dfactory = manager.getOWLDataFactory();
                OWLNamedIndividual a = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
                OWLClassExpression c = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "C"));
                OWLClassExpression d = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
                manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(c, a));
                manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(d.getComplementNNF(), a));
                //manager.addAxiom(ontology,dfactory.getOWLClassAssertionAxiom(d,a));
                //Hermit:
                //this.reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

                //Openllet:
                OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
                reasoner.flush();
                //Openllet can be used as OpenlletReasoner extends OWLReasoner (s .getKB().realize())

                manager.saveOntology(ontology, new SystemOutDocumentTarget());

                System.out.println(reasoner.isConsistent());
                System.out.println(reasoner.getTypes(a, true).entities().collect(toSet()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    */
    public static void test10_3() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/ten2.owl",
                "a",
                "D",
                1);
    }

    public static void test11() {
        //"observation:WTF???"
        //z nejakeho dovodu ignoruje a:~C z ontologie, ani v protege to nieje vidiet
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
            OWLClass c = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "C"));
            OWLAxiom observation = dfactory.getOWLClassAssertionAxiom(c, observationIndividual);
            OWLAxiom observationComplement = dfactory.getOWLClassAssertionAxiom(c.getComplementNNF(), observationIndividual);

            Main.the_algorithm_simple(ontology, manager, observation, observationComplement, 5);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void test11_2() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test_fixed.xml"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
            OWLClass c = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
            OWLAxiom observation = dfactory.getOWLClassAssertionAxiom(c, observationIndividual);
            OWLAxiom observationComplement = dfactory.getOWLClassAssertionAxiom(c.getComplementNNF(), observationIndividual);

            Main.the_algorithm_simple(ontology, manager, observation, observationComplement, 5);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void test12() {
        //a \in C or D
        //obs: a \in D
        //explanation: a \in not C
        try {
            try {
                OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-2models.owl"));
                String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
                OWLDataFactory dfactory = manager.getOWLDataFactory();
                OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
                OWLClass d = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
                OWLAxiom observation = dfactory.getOWLClassAssertionAxiom(d, observationIndividual);
                OWLAxiom observationComplement = dfactory.getOWLClassAssertionAxiom(d.getComplementNNF(), observationIndividual);

                Main.the_algorithm_simple(ontology, manager, observation, observationComplement, 5);
            } catch (Exception e) {
                e.printStackTrace();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void test13() {
        //funguje v hermit
        //pellet pre {a:~D, a:C) vrati model v ktorom (a:A)
        //Toto je sposobene, ako ukazuje to ze to funguje ak flushovanie nahradime vytvorenim noveho reasoneru,
        // tym ze openllet spravne neflushuje, ale dajake veci si pameta
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/triple.owl",
                "a",
                "D",
                3);
    }
    public static void test13_2(){

        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/triple.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual a = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
            OWLClassExpression A = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "A"));
            OWLClassExpression B = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "B"));

            OWLClassExpression C = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "C"));
            OWLClassExpression D = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(A, a));
            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(B, a));
            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(C, a));

            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(D.getComplementNNF(), a));
            //manager.addAxiom(ontology,dfactory.getOWLClassAssertionAxiom(d,a));
            //Hermit:
            //this.reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

            //Openllet:
            OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
            reasoner.flush();
            //Openllet can be used as OpenlletReasoner extends OWLReasoner (s .getKB().realize())

            // manager.saveOntology(ontology, new SystemOutDocumentTarget());

            System.out.println(reasoner.isConsistent());
            System.out.println(reasoner.getTypes(a, true).entities().collect(toSet()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void test13_3(){
        //dokazuje ze openllet nereaguje korektne na flush(), nechava si aj stare ontologie
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/triple.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual a = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
            OWLClassExpression A = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "A"));
            OWLClassExpression B = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "B"));

            OWLClassExpression D = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(A, a));
            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(B, a));

            manager.addAxiom(ontology, dfactory.getOWLClassAssertionAxiom(D.getComplementNNF(), a));
            //manager.addAxiom(ontology,dfactory.getOWLClassAssertionAxiom(d,a));
            //Hermit:
            //this.reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

            //Openllet:
            OWLReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
            reasoner.flush();
            //Openllet can be used as OpenlletReasoner extends OWLReasoner (s .getKB().realize())

            // manager.saveOntology(ontology, new SystemOutDocumentTarget());

            System.out.println(reasoner.isConsistent());
            System.out.println(reasoner.getTypes(a, true).entities().collect(toSet()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void test14(){
        try{
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-2ind.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual b = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "b"));
            OWLClassExpression D = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
            OWLClassExpression E = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "E"));
            // OWLClassExpression DandE = dfactory.
            OWLClassExpression DandE = dfactory.getOWLObjectIntersectionOf(D,E);
            OWLAxiom observation = dfactory.getOWLClassAssertionAxiom(DandE,b);
            OWLAxiom antiObservation = dfactory.getOWLClassAssertionAxiom(DandE.getComplementNNF(),b);
            Main.the_algorithm_simple(ontology,manager,observation,antiObservation,10);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void test14_2(){
        //ako 14, namiesto intersection je union
        try{
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-2ind.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual b = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "b"));
            OWLClassExpression D = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
            OWLClassExpression E = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "E"));
            // OWLClassExpression DandE = dfactory.
            OWLClassExpression DorE = dfactory.getOWLObjectUnionOf(D,E);
            OWLAxiom observation = dfactory.getOWLClassAssertionAxiom(DorE,b);
            OWLAxiom antiObservation = dfactory.getOWLClassAssertionAxiom(DorE.getComplementNNF(),b);
            Main.the_algorithm_simple(ontology,manager,observation,antiObservation,10);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void test15(){
        try{
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-2ind.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual b = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "b"));
            OWLClassExpression D = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
            OWLClassExpression E = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "E"));
            List<OWLAxiom> observations = new ArrayList<>();
            List<OWLAxiom> antiObservations = new ArrayList<>();
            observations.add(dfactory.getOWLClassAssertionAxiom(D,b));
            observations.add(dfactory.getOWLClassAssertionAxiom(E,b));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(D.getComplementNNF(),b));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(E.getComplementNNF(),b));
            Main.the_algorithm(ontology,manager,observations,antiObservations,3);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void test16(){
        try{
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-2ind.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual b = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "b"));
            OWLClassExpression D = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "D"));
            OWLClassExpression C = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "C"));
            List<OWLAxiom> observations = new ArrayList<>();
            List<OWLAxiom> antiObservations = new ArrayList<>();
            observations.add(dfactory.getOWLClassAssertionAxiom(D,b));
            observations.add(dfactory.getOWLClassAssertionAxiom(C,b));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(D.getComplementNNF(),b));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(C.getComplementNNF(),b));
            Main.the_algorithm(ontology,manager,observations,antiObservations,3);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void test17() {
        algorithm_ClassAssertion("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/test-inf.owl",
                "b",
                "D",
                5);
    }
    public static void test18(){

        try{
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/coffee-ontology.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual a = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
            OWLNamedIndividual b = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "b"));
            OWLNamedIndividual c = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "c"));

            OWLClassExpression Milk = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Milk"));
            OWLClassExpression Coffee = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Coffee"));
            OWLClassExpression Pure = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Pure"));


            List<OWLAxiom> observations = new ArrayList<>();
            List<OWLAxiom> antiObservations = new ArrayList<>();
            observations.add(dfactory.getOWLClassAssertionAxiom(Milk,a));
            observations.add(dfactory.getOWLClassAssertionAxiom(Coffee,b));
            observations.add(dfactory.getOWLClassAssertionAxiom(Pure,c));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(Milk.getComplementNNF(),a));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(Coffee.getComplementNNF(),b));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(Pure.getComplementNNF(),c));

            Main.the_algorithm(ontology,manager,observations,antiObservations,3);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void test19(){
        try{
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/LUBM.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual a = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "a"));
            OWLNamedIndividual jack = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "jack"));

            OWLClassExpression Person = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Person"));
            OWLClassExpression Employee = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Employee"));
            OWLClassExpression Publication = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Publication"));


            List<OWLAxiom> observations = new ArrayList<>();
            List<OWLAxiom> antiObservations = new ArrayList<>();
            observations.add(dfactory.getOWLClassAssertionAxiom(Publication,a));
            observations.add(dfactory.getOWLClassAssertionAxiom(Person,jack));
            observations.add(dfactory.getOWLClassAssertionAxiom(Employee,jack));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(Publication.getComplementNNF(),a));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(Person.getComplementNNF(),jack));
            antiObservations.add(dfactory.getOWLClassAssertionAxiom(Employee.getComplementNNF(),jack));

            Main.the_algorithm(ontology,manager,observations,antiObservations,3);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void test20(){
        //sanity test
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/family.owl"));
            String baseIriString = ontology.getOntologyID().getOntologyIRI().get().toString();
            OWLDataFactory dfactory = manager.getOWLDataFactory();
            OWLNamedIndividual observationIndividual = dfactory.getOWLNamedIndividual(IRI.create(baseIriString + "#" + "mary"));
            OWLClassExpression observationClassExp = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Mother"));
            OWLClassExpression observationClassExp2 = dfactory.getOWLClass(IRI.create(baseIriString + "#" + "Grandmother"));
            OWLAxiom observation = dfactory.getOWLClassAssertionAxiom(dfactory.getOWLObjectUnionOf(observationClassExp,observationClassExp2),observationIndividual);
            ontology.addAxiom(observation);
            OWLReasoner reasoner = openllet.owlapi.OpenlletReasonerFactory.getInstance().createReasoner(ontology);
            System.out.println(reasoner.isConsistent());
        //    manager.addAxiom(ontology, dfactory.getOWLDeclarationAxiom(dfactory.getOWLNamedIndividual(IRI.create("pseudoanonymous1"))));
          //  Main.the_algorithm_simple(ontology, manager, observationOne, observationOneComplement, 3);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    //  test1();
    //test1_2();
    //  test1_3();
    //test2();
    //test3();
    //test4();
    //  test5();
    //test6();
    //test7();
    //test8();
    //test9();
    //test10();
    //test10_2();
    //test10_3();
    //   test11();
    //test11_2();
    //test12();
    // test13();
    //test13_2();
    //test13_3();
    //test14();
    //test14();
    //test14_2();
    // test15();
    // test16();
//test17();
}
