# diplomovka
<<<<<<< HEAD
Jarko treba spustit z konzole, pozaduje spustenie reasoneru kompatibilneho s OWL API v3 na localhoste, port 8080.
=======
Jarko je konzolová apka,
Su dve verzie, jedna je OWL_abduction, je zalozena na OWLAPI 3,
 potrebuje spustený OWL API reasoner na localhost:8080. 
>>>>>>> f2a223e7aee975081f338a63dff38be978cf1de3
druha je b, robi cez OWLAPI 5 HermiT alebo Pellet.

Pouzitie: Cez input argumenty, napriklad "-i   C:/Users/drahos/Downloads/aaa-pellet/pellet2/examples/src/main/resources/data/family.owl
-d 20 -obs mary:Mother" 
-i : input
-d : maximum depth (default 1000)
-l : allow loops (reflexive relations in explanations, not allowed by default)
-obs : observation, format "a:C" or "a,b:R"
-r "Pellet" or -r "HermiT" - choose reasoner (for b, OWL_abduction has only owllink) - only those two (for b) for now, HermiT is default
The algorithm can do multiple observations or complex class/objectProperty expressions in observations, but so far i haven't added it as console input.
