package algorithms;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.JOptionPane;

public class DefaultTeam {
	
  public final static double BUDGET = 1664;
	
  
  /**
   *  Calcul l’arbre de Steiner sans restriction budgétaire, dans un graphe
   * @param points : une liste de coordonnées de points en 2D
   * @param edgeThreshold : un seuil de tolérance (distance maximale)
   * @param hitPoints : sous liste de points
   * @return steinerTree : l’arbre de Steiner sans restriction budgétaire
   */
  public Tree2D calculSteiner(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) {
	
	// Algorithme de Floyd-Warshall
	int[][] shortestPaths = calculShortestPaths(points, edgeThreshold);
	
	// Filtrer les points d'intérêt pour exclure les points aberrants
	ArrayList<Point> filtrehitPoints = filtrePoints(hitPoints, shortestPaths, points);
	System.out.println("hitpoints "+hitPoints.size()+", filtre "+filtrehitPoints.size());
	
	// Vérifier s'il existe des points aberrants
	if (hitPoints.size()!=filtrehitPoints.size())
        JOptionPane.showMessageDialog(null, "Il existe le(s) point(s) aberrant(s) !", "Avertissement", JOptionPane.WARNING_MESSAGE);
    
	// Appliquer l'algorithme de Kruskal pour trouver un arbre couvrant minimal
    ArrayList<Edge> kruskal_hp = kruskal(filtrehitPoints);
    
    ArrayList<Edge> result = new ArrayList<Edge>();
    
    // Parcourir toutes les arêtes de l'arbre couvrant minimal
    for(Edge e: kruskal_hp) {
    	//chercher tous les point dans la distance min entre e.p et e.q 
    	ArrayList<Point> path = new ArrayList<Point>();
    	path.add(e.p);
    	int k = shortestPaths[points.indexOf(e.p)][points.indexOf(e.q)];
    	while(k!=points.indexOf(e.q)) {
        	path.add(points.get(k));
        	k = shortestPaths[k][points.indexOf(e.q)];
        }
    	path.add(e.q);
    	
    	//remplace par les points
    	for(int i= 0; i<path.size()-1;i++) {
    		result.add(new Edge(path.get(i),path.get(i+1)));
    	}
    }
    
    // Convertir les arêtes résultantes en un arbre 2D
    Tree2D steinerTree= edgesToTree(result,result.get(0).p);    
    
    System.out.println("La distance de l'arbre : "+(int)distanceTrees(steinerTree));
    
    return steinerTree;
 }
  
  
  /**
   *  Calcul l’arbre de Steiner avec restriction budgétaire, dans un graphe
   * @param points : une liste de coordonnées de points en 2D
   * @param edgeThreshold : un seuil de tolérance (distance maximale)
   * @param hitPoints : sous liste de points
   * @return steinerTree : l’arbre de Steiner avec restriction budgétaire
   */
  public Tree2D calculSteinerBudget(ArrayList<Point> points, int edgeThreshold, ArrayList<Point> hitPoints) { 
	// Algorithme de Floyd-Warshall
	int[][] shortestPaths = calculShortestPaths(points, edgeThreshold);
	
	// Filtrer les points d'intérêt pour exclure les points aberrants
	ArrayList<Point> filtrehitPoints = filtrePoints(hitPoints, shortestPaths, points);
	
	// Appliquer l'algorithme de Kruskal pour trouver un arbre couvrant minimal
	ArrayList<Edge> kruskal_hp = kruskal(filtrehitPoints);
    
	ArrayList<Edge> result = new ArrayList<Edge>();
    
	// Parcourir toutes les arêtes de l'arbre couvrant minimal
    for(Edge e: kruskal_hp) {
    	//chercher tous les point dans la distance min entre e.p et e.q 
    	ArrayList<Point> path = new ArrayList<Point>();
    	path.add(e.p);
    	int k = shortestPaths[points.indexOf(e.p)][points.indexOf(e.q)];
    	while(k!=points.indexOf(e.q)) {
        	path.add(points.get(k));
        	k = shortestPaths[k][points.indexOf(e.q)];
        }
    	path.add(e.q);
    	
    	//remplace par les points
    	for(int i= 0; i<path.size()-1;i++) {
    		result.add(new Edge(path.get(i),path.get(i+1)));
    	}
    }
	    
    ArrayList<Edge> prime = meuilleur_solution(result,hitPoints, BUDGET); //kruskal_hp.get(0).p
	//    for(Edge e: prime) {
	//	    	System.out.println("Edge: (" + e.p.x +"," + e.p.y +")" + " - (" + e.q.x +","+ e.q.y +")" + ", Distance: " + e.distance());
	//	    }
	//	    System.out.println("nb points:" + nb_points(prime,hitPoints));
	
    // Recherche de la meilleure solution pour la liste des arêtes de l'arbre couvrant minimal avec un budget
    Tree2D steinerTree= edgesToTree(prime,prime.get(0).p);    
    System.out.println("La distance de l'arbre : "+(int)distanceTrees(steinerTree));
    return steinerTree;
  }
  
 

  /**
   * Il calcule les chemins les plus courts entre tous les points de l'ensemble.
   * dist[i][j] : la distance la plus courte entre i et j dans G
   * paths[i][j] : le sommet k, successeur de i dans un plus court chemin de i à j
   * @param points : une liste de coordonnées de points en 2D
   * @param edgeThreshold : un seuil de tolérance (distance maximale)
   * @return paths: la matrice des chemin 
  **/
  public int[][] calculShortestPaths(ArrayList<Point> points, int edgeThreshold) {
	// Initialisation de la matrice des chemins avec des indices par défaut
	int[][] paths=new int[points.size()][points.size()];
    for (int i=0;i<paths.length;i++) for (int j=0;j<paths.length;j++) { 
    	// Initialisation avec l'indice de départ
    	paths[i][j]=i;
    }

    // Initialisation de la matrice des distances avec des distances par défaut
    double[][] dist=new double[points.size()][points.size()];
    
    // Calcul des distances entre chaque paire de points
    for (int i=0;i<paths.length;i++) {
    	for (int j=0;j<paths.length;j++) { 
    		if (i==j) {
    			dist[i][i]=0; // Distance nulle pour un point avec lui-même
    			continue;}
            // Calcul de la distance euclidienne entre les points et mise à jour des chemins
            if (points.get(i).distance(points.get(j))<= edgeThreshold) { 
            	dist[i][j]=points.get(i).distance(points.get(j)); 
            	paths[i][j]=j; // Mise à jour du prochain point sur le chemin
            }
            else dist[i][j]=Double.POSITIVE_INFINITY;  // Distance infinie si la distance entre les points dépasse le seuil
    	}
    }
    
    // Calcul des chemins les plus courts en utilisant l'algorithme de Floyd-Warshall
    for (int k=0;k<paths.length;k++) {
    	for (int i=0;i<paths.length;i++) {
    		for (int j=0;j<paths.length;j++) {
    			// Mise à jour des distances et des chemins si un chemin plus court est trouvé
    			if (dist[i][j]>dist[i][k] + dist[k][j]){
    				dist[i][j]=dist[i][k] + dist[k][j];
    				paths[i][j]=paths[i][k];  // Mise à jour du prochain point sur le chemin
    			}
    		}
        }
    }
    return paths;
  }
  
	  
  /**
   * Filtre les points aberrants
   * @param hitPoints: sous liste de points
   * @param paths : la matrice des chemin
   * @param points : une liste de coordonnées de points en 2D
   * @return filtre: une liste des points filtrés à partir des hitPoints.
   */
  private ArrayList<Point> filtrePoints(ArrayList<Point> hitPoints, int[][]paths, ArrayList<Point> points){
	ArrayList<Point>filtre = new ArrayList<Point>();
	int Arrive_Threshold = (int) Math.ceil(hitPoints.size()*0.3); // Seuil pour déterminer les points aberrants
	
    // Parcours de tous les points d'intérêt
    for(Point p: hitPoints) {
    	int reach_actuel = 0; 
	    int p_ind= points.indexOf(p);
	    
        // Parcours des autres points d'intérêt pour compter ceux atteints par le point actuel
	    for(Point q: hitPoints) {
	    	// Assure que le point p et le point q sont différents
	    	if(!p.equals(q)) {
	    		int q_ind=points.indexOf(q);
	    		// Vérifie si le point q est atteint à partir de p
	    		if(paths[p_ind][q_ind]!= p_ind) {
	    			reach_actuel++;
	    		}
	    	}
	    }
        // Si le nombre de points atteints est supérieur ou égal au seuil, ajoute le point à la liste filtrée
	    if(reach_actuel>=Arrive_Threshold) {
	    	filtre.add(p);
	    }
    }
    return filtre;
  }
  
  /**
   * Calcul la distance totale une fois que toutes les sous-arbres ont été traitées.
   * @param arbre : l’arbre de Steiner
   * @return d: la distance totale une fois que toutes les sous-arbres ont été traitées.
   */
  private double distanceTrees(Tree2D arbre) {
    double d = 0;
    
    // Si l'arbre ne contient pas de sous-arbres, la distance est nulle
    if(arbre.getSubTrees().isEmpty()) {
    	return 0;
    }
    else{
    	// Si l'arbre ne contient pas de sous-arbres, la distance est nulle
    	for (Tree2D subtree : arbre.getSubTrees()) {
            // Ajout de la distance entre la racine de l'arbre actuel et la racine du sous-arbre
    		d+= arbre.getRoot().distance(subtree.getRoot());
   
    		// Appel récursif pour calculer la distance entre les sous-arbres du sous-arbre actuel
    		d+= distanceTrees(subtree);
   
    	}
    }
    return d;
  }
  

  /**
   * L'algorithme de Prim pour trouver un arbre couvrant de poids minimal avec une contrainte budgétaire
   * @param edges: la liste des arêtes du graphe.
   * @param start: le point de départ pour l'algorithme de Prim.
   * @param budget: le budget maximal autorisé.
   * @return minimumSpanningTree: la liste des arêtes de l'arbre couvrant minimal.
   */
  public ArrayList<Edge> prime_edges(ArrayList<Edge> edges, Point start, double budget) {
	// Ensemble des points visités
	Set<Point> visited = new HashSet<>();
	
	// Arbre couvrant minimal
	ArrayList<Edge> minimumSpanningTree = new ArrayList<>();
	
	// Tas min de priorité pour les arêtes
	PriorityQueue<Edge> minHeap = new PriorityQueue<>(Comparator.comparingDouble(Edge::distance));
	
	// Ajout du point de départ dans les points visités
	visited.add(start);

	// Ajout des arêtes connectées au point de départ dans le tas min
	for (Edge edge : edges) {
		if (edge.p.equals(start) || edge.q.equals(start)) {
			minHeap.add(edge);
		}
	}
	
    double dist = 0;

    // Boucle principale de l'algorithme de Prim
	while (!minHeap.isEmpty()) {
		// Récupération de l'arête de poids minimum dans le tas
		Edge minEdge = minHeap.poll();
		
		// Récupération du prochain point à visiter
		Point nextPoint = visited.contains(minEdge.p) ? minEdge.q : minEdge.p;
	      
		// Si le prochain point n'a pas déjà été visité  
		if (!visited.contains(nextPoint)) {
			
			// Ajout du prochain point dans les points visités
			visited.add(nextPoint);
			if(dist+minEdge.distance()>=budget) {
				// Si ajouter cette arête dépasse le budget, arrêter
				break;
		  	}
			// Ajout de l'arête dans l'arbre couvrant minimal
			minimumSpanningTree.add(minEdge);
			
			// Mise à jour de la distance totale de l'arbre
			dist = dist+minEdge.distance();
			
            // Ajout des arêtes connectées au prochain point dans le tas binaire
			for (Edge edge : edges) {
				if (edge.p.equals(nextPoint) || edge.q.equals(nextPoint)) {
					minHeap.add(edge);
              	}
			}      
		}                       
	}
    return minimumSpanningTree;
  }

  /**
   * Compte le nombre de points parmi les points d'intersection des arêtes qui font partie du graphe et qui sont également présents dans la liste hitPoints.
   * @param edges: la liste des arêtes du graphe.
   * @param hitPoints: la liste des points d'intérêt.
   * @return visited.size(): le nombre de points d'intersection
   */
  private int nb_points(ArrayList<Edge> edges, ArrayList<Point> hitPoints){
	ArrayList<Point> visited = new ArrayList<Point>();
	for(Edge e: edges) {  
		// Ajout des extrémités de l'arête à la liste des points visités
		if(!visited.contains(e.p)) {
			visited.add(e.p);
		}
		if(!visited.contains(e.q)) {
			visited.add(e.q);
		}
	}
    // Utilisation d'un itérateur pour parcourir les points visités et les comparer avec les points de contrainte
    for (java.util.Iterator<Point> iterator = visited.iterator(); iterator.hasNext();) {
    	Point p = iterator.next();
    	if (!hitPoints.contains(p)) {
    		iterator.remove();
    	}
    }
    return visited.size();
  }
  
  /**
   * Recherche la meilleure solution possible pourpour trouver un arbre couvrant de poids minimal avec une contrainte budgétaire, 
   * en essayant différentes combinaisons de points d'origine (maisons-mères) parmi les points d'intérêt de la liste hitPoints. 
   * @param edges: la liste des arêtes du graphe
   * @param hitPoints: la liste des points d'intérêt.
   * @param budget: le budget maximal autorisé.
   * @return bestSolution: ensemble d'arêtes représentant l'arbre de Steiner qui couvre le plus grand nombre de points d'intérêt 
   * 					   avec un budget inférieur à la valeur spécifiée.
   */
  public ArrayList<Edge> meuilleur_solution(ArrayList<Edge> edges,ArrayList<Point> hitPoints,  double budget){
	ArrayList<Edge> bestSolution = null;
    int maxPointsCount = 0;
      
    for(Point p: hitPoints) {
    	ArrayList<Edge> m_list_edges = prime_edges(edges, p, budget);
        int m_nb_points = nb_points(m_list_edges,hitPoints);
          
        if (m_nb_points > maxPointsCount) {
        	bestSolution = m_list_edges;
            maxPointsCount = m_nb_points;
        }
          
        for(Point q: hitPoints) {
        	if (!p.equals(q)) { // assurer que p!=q
        		ArrayList<Edge> list_edges = prime_edges(edges, q, budget);
                int nb_points = nb_points(list_edges,hitPoints);

                if (nb_points > maxPointsCount) {
                	bestSolution = list_edges;
                	maxPointsCount = nb_points;
                }
            }		  
        }
    }
	return bestSolution;
  }
  
  /**
   * Trouver un arbre couvrant de poids minimum dans un graphe non orienté.
   * @param points: la liste des points du graphe.
   * @return kruskal: les arêtes de l'arbre couvrant de poids minimum.
   */
  public ArrayList<Edge> kruskal(ArrayList<Point> points) {
    //KRUSKAL ALGORITHM, NOT OPTIMAL FOR STEINER!
    ArrayList<Edge> edges = new ArrayList<Edge>();
    for (Point p: points) {
      for (Point q: points) {
        if (p.equals(q) || contains(edges,p,q)) continue;
        edges.add(new Edge(p,q));
      }
    }
    edges = sort(edges);

    ArrayList<Edge> kruskal = new ArrayList<Edge>();
    Edge current;
    NameTag forest = new NameTag(points);
    while (edges.size()!=0) {
      current = edges.remove(0);
      if (forest.tag(current.p)!=forest.tag(current.q)) {
        kruskal.add(current);
        forest.reTag(forest.tag(current.p),forest.tag(current.q));
      }
    }

    return kruskal;
  }
	   
  
  /**
   * Trouver un arbre couvrant de poids minimum dans un graphe non orienté.
   * @param points: la liste des points du graphe.
   * @return l'arbre couvrant de poids minimum.
   */
  public Tree2D algo_kruskal(ArrayList<Point> points) {
    //KRUSKAL ALGORITHM, NOT OPTIMAL FOR STEINER!
    ArrayList<Edge> edges = new ArrayList<Edge>();
    for (Point p: points) {
      for (Point q: points) {
        if (p.equals(q) || contains(edges,p,q)) continue;
        edges.add(new Edge(p,q));
      }
    }
    edges = sort(edges);

    ArrayList<Edge> kruskal = new ArrayList<Edge>();
    Edge current;
    NameTag forest = new NameTag(points);
    while (edges.size()!=0) {
      current = edges.remove(0);
      if (forest.tag(current.p)!=forest.tag(current.q)) {
        kruskal.add(current);
        forest.reTag(forest.tag(current.p),forest.tag(current.q));
      }
    }

    return edgesToTree(kruskal,kruskal.get(0).p);
  }
  
  /**
   * Vérifie si une liste d'arêtes contient une arête spécifique définie par deux points p et q
   * @param edges: la liste d'arêtes du graphe
   * @param p: le premier point de l'arête à rechercher
   * @param q: le deuxieme point de l'arête à rechercher.
   * @return : true: indiquant que l'arête spécifiée est présente dans la liste 'edge'
   * 		   false: indiquant que l'arête spécifiée n'est pas présente dans la liste 'edges' 
   */
  private boolean contains(ArrayList<Edge> edges,Point p,Point q){
    for (Edge e:edges){
      if (e.p.equals(p) && e.q.equals(q) ||
          e.p.equals(q) && e.q.equals(p) ) return true;
    }
    return false;
  }
  
  /**
   * Convertir une liste d'arêtes en une structure d'arbre
   * @param edges: la liste d'arêtes du graphe
   * @param root: le point racine de l'arbre
   * @return l'arbre avec le point racine spécifié et la liste de sous-arbres.
   */
  private Tree2D edgesToTree(ArrayList<Edge> edges, Point root) {
    ArrayList<Edge> remainder = new ArrayList<Edge>();
    ArrayList<Point> subTreeRoots = new ArrayList<Point>();
    Edge current;
    while (edges.size()!=0) {
      current = edges.remove(0);
      if (current.p.equals(root)) {
        subTreeRoots.add(current.q);
      } else {
        if (current.q.equals(root)) {
          subTreeRoots.add(current.p);
        } else {
          remainder.add(current);
        }
      }
    }

    ArrayList<Tree2D> subTrees = new ArrayList<Tree2D>();
    for (Point subTreeRoot: subTreeRoots) subTrees.add(edgesToTree((ArrayList<Edge>)remainder.clone(),subTreeRoot));

    return new Tree2D(root, subTrees);
  }
	 
  /**
   * Un algorithme de tri récursif pour trier une liste d'arêtes par ordre croissant de leur distance.
   * @param edges: la liste d'arêtes du graphe
   * @return result: la liste triée 
   */
  private ArrayList<Edge> sort(ArrayList<Edge> edges) {
    if (edges.size()==1) return edges;

    ArrayList<Edge> left = new ArrayList<Edge>();
    ArrayList<Edge> right = new ArrayList<Edge>();
    int n=edges.size();
    for (int i=0;i<n/2;i++) { left.add(edges.remove(0)); }
    while (edges.size()!=0) { right.add(edges.remove(0)); }
    left = sort(left);
    right = sort(right);

    ArrayList<Edge> result = new ArrayList<Edge>();
    while (left.size()!=0 || right.size()!=0) {
      if (left.size()==0) { result.add(right.remove(0)); continue; }
      if (right.size()==0) { result.add(left.remove(0)); continue; }
      if (left.get(0).distance() < right.get(0).distance()) result.add(left.remove(0));
      else result.add(right.remove(0));
    }
    return result;
  }
}

class Edge {
  protected Point p,q;
  protected Edge(Point p,Point q){ this.p=p; this.q=q; }
  protected double distance(){ return p.distance(q); }
}
class NameTag {
  private ArrayList<Point> points;
  private int[] tag;
  protected NameTag(ArrayList<Point> points){
    this.points=(ArrayList<Point>)points.clone();
    tag=new int[points.size()];
    for (int i=0;i<points.size();i++) tag[i]=i;
  }
  protected void reTag(int j, int k){
    for (int i=0;i<tag.length;i++) if (tag[i]==j) tag[i]=k;
  }
  protected int tag(Point p){
    for (int i=0;i<points.size();i++) if (p.equals(points.get(i))) return tag[i];
    return 0xBADC0DE;
 }

}
