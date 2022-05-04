# Fruit Lines en OpenGLES

Pour ce jeu du cinq ou plus, j'ai utilisé des assets de fruits pour changer du thème classique.
L'execution du jeu est simple, il suffit d'ouvrir le projet avec Android Studio puis lancer son émulateur / brancher son propre téléphone Android 
puis lancer le jeu depuis Android Studio. 
En arrivant sur l'activité principale, plusieurs choix s'offrent à vous :
- Jouer sur une grille 9x9 
- Jouer sur une grille 7x7
- Aller dans les paramètres 

Les paramètres proposent différentes options comme changer le nombre de fruits à aligner pour compléter une ligne ou bien encore de changer le nombre de suivants 
pour augmenter / réduire la difficulté. Ces deux options sont disponibles pour les deux modes de jeu 9x9 et 7x7.

Les règles du jeu sont les mêmes que pour le cinq ou plus, un pop-up apparait à l'écran quand une action est impossible.
Il y a un menu Game Over sur lequel on peut rejouer le mode précédent ou revenir à l'accueil.

Concernant le développement du projet, je me suis inspiré de la méthode de Pierre Zachary qui consiste à "recréer" un environnement Unity avec une caméra, une scène
qui contient des GameObject possédant une position dans la scène (Transform), une hitbox (SpriteCollider) et une texture 2D (SpriteRenderer).  
La partie OpenGLES est implémentée dans le draw du SpriteRenderer pour afficher les textures des GameObject.  
Le GameManager est la classe qui gère le déroulement du jeu en appelant des méthodes du modèle qui contient les classes qui définissent une case (Tile), la grille (Grid) 
et les positions.  
Pour la partie Android pure, les menus sont des Activity android avec un layout.xml associé. J'ai utilisé les SharedPreferences pour enregistrer les scores 
et les paramètres choisi par l'utilisateur. J'ai également utiliser l'outil de traduction android pour le français et l'anglais, tous les chaînes de caractères changent
quand la langue du téléphone change.  

Vous pouvez retrouver le projet github [ici](https://github.com/NicolasLoison/KLinesOpenGLES) et me contacter en cas de problème à nicolas.loison@etu.univ-orleans.fr
