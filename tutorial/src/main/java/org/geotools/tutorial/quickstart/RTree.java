/*
 * Project : 
 * Date: Thursday, March 30th 2023, 12:09:05 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import org.opengis.feature.simple.SimpleFeature;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.util.XRectangle2D;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RTree {
    private static final int N = 3;
    private static double smallestEnlargementArea = 100000;
    Node bestNode; // noeud correspondant au meilleur élargissement
    public Node root;
    Node n;
    Node searchNode;
    Node chosenNode;

    public Node createTree(String filename, String valueProperty) throws IOException {
        int i = 0;
        root = new Node();
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection all_features = featureSource.getFeatures();

        store.dispose();
        try (SimpleFeatureIterator iterator = all_features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();

                MultiPolygon polygon = (MultiPolygon) feature.getDefaultGeometry();
                if (polygon != null && root != null) {
                    String label = feature.getProperty(valueProperty).getValue().toString();
                    n = root;
                    Node nodeToAdd = new Node(label, polygon);
                    addLeaf(nodeToAdd);
                }
            }
        }
        return null;
    }

    Node addLeaf(Node nodeToAdd) {
        if (n.subnodes.size() == 0 || n.subnodes.get(0).subnodes.size() == 0) { // if bottom level is reached -> create
            n.subnodes.add(nodeToAdd); // create leaf
            nodeToAdd.parent = n;
        } else { // still need to go deeper
            System.out.println("hereAHHH");
            chosenNode = root;
            chooseNode(nodeToAdd, nodeToAdd.polygon);
            n = bestNode;
            Node new_node = addLeaf(nodeToAdd);
            if (new_node != null) { // a split occurred in addLeaf, a new node is added at this level
                n.subnodes.add(new_node);
                nodeToAdd.parent = n;
            }
        }
        expandMBR(n, nodeToAdd.MBR);
        if (n.subnodes.size() >= N) {
            return splitLineaire(n);

        }
        return null;
    }

    /**
     * @param node
     * @param polygon
     * @return Node
     */
    void chooseNode(Node nodeToAdd, MultiPolygon polygon) {
        while (!(chosenNode.subnodes.isEmpty())) // not leaf
        {
            for (Node elem : chosenNode.subnodes) {
                chosenNode = elem;
                chooseNode(nodeToAdd, polygon);
            }
        }
        // calculate expansion
        Node copiedNode = new Node(chosenNode);
        copiedNode.MBR.expandToInclude(nodeToAdd.MBR);
        double enlargementArea = (copiedNode.MBR.getWidth() * copiedNode.MBR.getHeight())
                - (nodeToAdd.MBR.getWidth() * nodeToAdd.MBR.getHeight());
        if (enlargementArea < smallestEnlargementArea) {
            smallestEnlargementArea = enlargementArea;
            bestNode = chosenNode;
        }
    }

    /**
     * 
     * @param node
     * @param point
     * @return
     */
    public Node search(Node node, Point point) { // appeller cette fonction avec la racine de l'arbre
        if (node.subnodes.size() == 0) { // si c'est une feuille
            if (node.MBR.contains(point.getX(), point.getY())) {
                if (node.polygon.contains(point)) {
                    System.out.println("jpasse par ici haha");
                    System.out.println(node.label);
                    return node;
                }
            }
        } else {
            System.out.println("Surujefjo");
            if (node.MBR.contains(point.getX(), point.getY())) {
                System.out.println("rrrrrr");
                for (Node subnode : node.subnodes) {
                    Node nodeFound = search(subnode, point);
                    if (nodeFound != null)
                        return nodeFound;
                }

            }
        }
        return null;
    }

    void expandMBR(Node node, Envelope MBR) {
        node.MBR.expandToInclude(MBR);
        while (node != root) {
            expandMBR(node.parent, MBR);
        }
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
    Node splitLineaire(Node node) {
        System.out.println("coincé ici");
        ArrayList<Node> seeds = pickSeedsLinear(node);
        //pickNext(seeds.get(0), seeds.get(1), node);
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

    void pickNext(Node seed1, Node seed2, Node nodeToSplit) {
        for (Node subnode : nodeToSplit.subnodes) {
            //puts("eaccc");
            if (seed1 != subnode && seed2 != subnode) {
                Node copiedNode = new Node(seed1);
                Node copiedNode2 = new Node(seed2);
                copiedNode.MBR.expandToInclude(subnode.MBR);
                copiedNode2.MBR.expandToInclude(subnode.MBR);
                if (copiedNode.MBR.getArea() > copiedNode2.MBR.getArea()) {
                    seed2.subnodes.add(subnode);
                    expandMBR(seed2, subnode.MBR);
                    subnode.parent = seed2;
                } else {
                    seed1.subnodes.add(subnode);
                    expandMBR(seed1, subnode.MBR);
                    subnode.parent = seed1;
                }
                nodeToSplit.subnodes.remove(subnode);
            }
        }

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
        // double area = rect.getHeight() * rect.getWidth()
        // - (seed1.MBR.getHeight() * seed1.MBR.getWidth() + seed2.MBR.getHeight()
        // * seed2.MBR.getWidth());
        // comparer avec le meilleur rectangle

        // return les seeds correspondant au plus grand area
        return seeds;
    }

    public void printTree(Node node) {
        System.out.println("kids");
        for (int i = 0; i < node.subnodes.size(); i++) {
            // System.out.println("which node " + node.subnodes.get(i).label);
            if (node.subnodes.get(i).subnodes.size() != 0) {
                System.out.println("wtf it has kids");
                printTree(node.subnodes.get(i));
            }
        }
    }
}
