/*
 * Project : 
 * Date: Thursday, March 30th 2023, 12:09:05 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import org.opengis.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.util.XRectangle2D;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;

public class RTree {
    private static final int N = 3;
    Node root;
    Node n;

    Node createTree(SimpleFeatureCollection all_features) {

        try (SimpleFeatureIterator iterator = all_features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();

                MultiPolygon polygon = (MultiPolygon) feature.getDefaultGeometry();
                if (polygon != null && root != null) {
                    String label = feature.getProperty("NAME_FR").getValue().toString();
                    n = root;
                    addLeaf(new Node(label, polygon));
                } else if (polygon != null && root == null) {
                    String label = feature.getProperty("NAME_FR").getValue().toString();
                    root = new Node(label, polygon);
                }
            }
        }
        return null;
    }

    Node addLeaf(Node nodeToAdd) {
        if (n.subnodes.size() == 0 || n.subnodes.get(0).subnodes.size() == 0) { // if bottom level is reached -> create                                                             // leaf
            n.subnodes.add(nodeToAdd); // create leaf
        } else { // still need to go deeper
            n = chooseNode(nodeToAdd, nodeToAdd.polygon);
            Node new_node = addLeaf(nodeToAdd);
            if (new_node != null) { // a split occurred in addLeaf, a new node is added at this level
                n.subnodes.add(new_node);
            }
        }
        expandMBR(n, nodeToAdd.MBR);
        if (n.subnodes.size() >= N) {
            return splitQuadratique(n);
        }
        return null;

    }

    /**
     * @param node
     * @param polygon
     * @return Node
     */
    Node chooseNode(Node node, MultiPolygon polygon) {

        Node bestNode = node; // noeud correspondant au meilleur élargissement
        int smallestEnlargement = 0;
        while (node.label == "0") { // not leaf
            ArrayList<Node> subNodes = node.subnodes;
            // élargissement du MBR

            for (Node elem : subNodes) {
                int newEnlargement = 0; // on calcule jsp comment
                if (newEnlargement < smallestEnlargement) {
                    smallestEnlargement = newEnlargement;
                    bestNode = elem;
                }
            }
        }
        return bestNode;
    }

    /**
     * 
     * @param node
     * @param point
     * @return
     */
    Node search(Node node, Point point) { // appeller cette fonction avec la racine de l'arbre
        if (node.subnodes.size() == 0) { // si c'est une feuille
            if (node.MBR.contains(point.getX(), point.getY())) {
                return node; // Si le point appartient au MBR du nœud et le point appartient au polygone,
                             // retourner "this",
            }
        } else {
            if (node.MBR.contains(point.getX(), point.getY())) {
                for (Node subnode : node.subnodes) {
                    return (search(subnode, point));
                }
            }
        }
        return null;
    }

    void expandMBR(Node node, XRectangle2D MBR) {
        XRectangle2D newMBR = (XRectangle2D) node.MBR.createUnion(MBR);
        node.MBR = newMBR;
    }

    /**
     * 
     * @param node
     * @return
     */
    Node splitQuadratique(Node node) {
        pickSeedsQuadratic(node);
        return node;
    }

    /**
     * 
     * @param node
     * @return
     */
    Node splitLinéaire(Node node) {
        pickSeedsLinear(node);
        return node;
    }

    /**
     * trouver l'entrée dont le rectangle a
     * le côté bas le plus élevé, et celui
     * avec le côté haut le plus bas Enregistrez la séparation.
     * 
     * Normalize the separations
     * by dividing by the width of the entire
     * set
     * 
     * Choisir la pair avec la plus grande normalized séparation
     * 
     * @return
     */
    ArrayList<Node> pickSeedsLinear(Node node) {
        ArrayList<Node> seeds = new ArrayList<Node>();
        double bestMaxX = 0; // + petit possible
        double bestMinX = 100000; // + grand possible
        double bestMaxY = 0; // + petit possible
        double bestMinY = 100000; // + grand possible
        Node bestSeed1 = new Node();
        Node bestSeed2 = new Node();
        Node bestSeed3 = new Node();
        Node bestSeed4 = new Node();
        // getMaxX doit être le + petit possible pr rect1
        // getMinX doit être le + grand possible pr rect2
        for (Node seed : node.subnodes) {

            if (seed.MBR.getMaxX() < bestMaxX) {
                bestMaxX = seed.MBR.getMaxX();
                bestSeed1 = seed;
            }
            if (seed.MBR.getMinX() > bestMinX) {
                bestMaxX = seed.MBR.getMaxX();
                bestSeed2 = seed;
            }

            if (seed.MBR.getMaxY() < bestMaxY) {
                bestMaxY = seed.MBR.getMaxY();
                bestSeed3 = seed;
            }
            if (seed.MBR.getMinY() > bestMinY) {
                bestMaxY = seed.MBR.getMaxY();
                bestSeed4 = seed;
            }
        }

        // normalize
        double widthW = bestSeed2.MBR.getMaxX() - bestSeed1.MBR.getMinX();
        double widthL = bestSeed2.MBR.getMinX() - bestSeed1.MBR.getMaxX();
        double separation = widthL / widthW;

        double widthW2 = bestSeed4.MBR.getMaxX() - bestSeed3.MBR.getMinX();
        double widthL2 = bestSeed4.MBR.getMinX() - bestSeed3.MBR.getMaxX();
        double separation2 = widthL2 / widthW2;

        // choose the greatest separation
        if (separation > separation2) {
            seeds.add(bestSeed1);
            seeds.add(bestSeed2);
        } else {
            seeds.add(bestSeed3);
            seeds.add(bestSeed4);
        }

        return seeds;
    }

    /**
     * For each pair of Entries compose a rectangle and pick the one with largest d
     * Choisir les deux seeds les + éloignées possibles
     * pair with de largest d, d = area(J) - area(E1*I) - area(E2*I)
     * 
     * @return
     */
    ArrayList<Node> pickSeedsQuadratic(Node node) {
        ArrayList<Node> seeds = new ArrayList<Node>();
        // parcourir tout l'arbre
        Node seed1 = new Node();
        Node seed2 = new Node();

        // créer un rectangle avec les MBR des nodes dedans
        double rectangleMinX = Double.min(seed1.MBR.getMinX(), seed2.MBR.getMinX());
        double rectangleMaxX = Double.max(seed1.MBR.getMaxX(), seed2.MBR.getMaxX());
        double rectangleMinY = Double.min(seed1.MBR.getMinY(), seed2.MBR.getMinY());
        double rectangleMaxY = Double.max(seed1.MBR.getMaxY(), seed2.MBR.getMaxY());

        XRectangle2D rect = XRectangle2D.createFromExtremums(rectangleMinX, rectangleMinY, rectangleMaxX,
                rectangleMaxY);
        // avoir l'area du rectangle - l'aire des deux MBR des nodes
        double area = rect.getHeight() * rect.getWidth()
                - (seed1.MBR.getHeight() * seed1.MBR.getWidth() + seed2.MBR.getHeight()
                        * seed2.MBR.getWidth());
        // comparer avec le meilleur rectangle

        // return les seeds correspondant au plus grand area
        return seeds;
    }
}
