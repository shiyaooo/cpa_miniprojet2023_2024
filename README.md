Ce projet consiste à connecter un ensemble de points dans un plan en minimisant la longueur totale des connexions, en utilisant éventuellement des points intermédiaires (points de Steiner).

- **Sans budget** : On cherche l’arbre de Steiner de coût total minimal, sans contrainte.
![Illustration](Arbre_Steiner_Sans_Budget.png)
- **Avec budget** : L’arbre doit respecter un coût maximal donné. Si ce n’est pas possible, l’algorithme propose une solution partielle ou échoue.
![Illustration](Arbre_Steiner_Avec_Budget.png)
