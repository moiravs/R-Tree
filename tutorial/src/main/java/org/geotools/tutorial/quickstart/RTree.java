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
import org.geotools.geometry.util.XRectangle2D;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RTree {
    private static final int N = 4;
    private static double smallestEnlargementArea = Double.POSITIVE_INFINITY;
    MBRNode bestNode; // noeud correspondant au meilleur élargissement
    public MBRNode root;
    MBRNode n;
    MBRNode searchNode;
    MBRNode chosenNode;

    public MBRNode createTree(String filename, String valueProperty) throws IOException {
        int i = 0;
        root = new MBRNode();
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
                if (polygon != null && root != null && i != 6) {
                    String label = feature.getProperty(valueProperty).getValue().toString();
                    MBRNode nodeToAdd = new MBRNode(label, polygon);
                    System.out.println("start");
                    root.print(1);
                    System.out.println("end");
                    addLeaf(root, nodeToAdd);
                    i++;
                }
            }
        }

        return null;
    }

    MBRNode addLeaf(MBRNode n, MBRNode nodeToAdd) {
        if (n.subnodes.size() == 0 || n.subnodes.get(0).subnodes.size() == 0) { // if bottom level is reached -> create
            n.subnodes.add(nodeToAdd); // create leaf
            nodeToAdd.parent = n;
        } else { // still need to go deeper
            n = chooseNode(root, nodeToAdd, nodeToAdd.polygon);
            MBRNode new_node = addLeaf(n, nodeToAdd);
            if (new_node != null) { // a split occurred in addLeaf, a new node is added at this level
                new_node.label = "splitNode";
                n.subnodes.add(new_node);
                new_node.parent = n;
                nodeToAdd.parent = new_node;
                // expandMBR(new_node, nodeToAdd.MBR);
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
     * @return MBRNode
     */
    MBRNode chooseNode(MBRNode bestNode, MBRNode nodeToAdd, MultiPolygon polygon) {

        if (bestNode.subnodes.size() == 0) {
            return bestNode;
        }
        // calculate expansion
        else {
            MBRNode bestChildNode = new MBRNode("SplitNode");
            for (MBRNode subnode : bestNode.subnodes) {

                MBRNode copiedNode = new MBRNode(subnode);
                copiedNode.MBR.expandToInclude(nodeToAdd.MBR);
                double enlargementArea = (copiedNode.MBR.getArea() - nodeToAdd.MBR.getArea());
                if (enlargementArea < smallestEnlargementArea) {
                    smallestEnlargementArea = enlargementArea;
                    bestChildNode = subnode;
                }
            }
            return chooseNode(bestChildNode, nodeToAdd, polygon);
        }
    }

    /**
     * 
     * @param node
     * @param point
     * @return
     */
    public MBRNode search(MBRNode node, Point point) { // appeller cette fonction avec la racine de l'arbre
        if (node.subnodes.size() == 0) { // si c'est une feuille
            if (node.MBR.contains(point.getX(), point.getY())) {
                if (node.polygon.contains(point)) {
                    return node;
                }
            }
        } else {
            if (node.MBR.contains(point.getX(), point.getY())) {
                for (MBRNode subnode : node.subnodes) {
                    MBRNode nodeFound = search(subnode, point);
                    if (nodeFound != null)
                        return nodeFound;
                }

            }
        }
        return null;
    }

    Boolean expandMBR(MBRNode node, Envelope MBR) {
        node.MBR.expandToInclude(MBR);
        while (node.label != "root") {
            return expandMBR(node.parent, MBR);
        }
        return true;
    }

    /**
     * 
     * @param node
     * @return
     */
    MBRNode splitQuadratique(MBRNode node) {
        pickSeedsQuadratic(node);
        return node;
    }

    /**
     * 
     * @param node
     * @return
     */
    MBRNode splitLineaire(MBRNode node) {
        System.out.println("label that must be root" + node.label);
        System.out.println("before");
        root.print(1);
        ArrayList<MBRNode> copiedSubnodes = new ArrayList<MBRNode>(node.subnodes);
        ArrayList<MBRNode> splitSeeds = pickSeedsLinear(node);
        node.subnodes = new ArrayList<MBRNode>();
        splitSeeds.get(0).parent = node;
        splitSeeds.get(1).parent = node;
        node.subnodes.add(splitSeeds.get(0));
        node.subnodes.add(splitSeeds.get(1));
        System.out.println("pendant");
        root.print(1);
        pickNext(splitSeeds.get(0), splitSeeds.get(1), copiedSubnodes);

        System.out.println("after");

        root.print(1);

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
    ArrayList<MBRNode> pickSeedsLinear(MBRNode node) {
        double bestMaxX = Double.POSITIVE_INFINITY; // + petit possible
        double bestMinX = Double.NEGATIVE_INFINITY; // + grand possible
        double bestMaxY = Double.POSITIVE_INFINITY; // + petit possible
        double bestMinY = Double.NEGATIVE_INFINITY; // + grand possible
        Envelope MBRBestMaxX = new Envelope();
        Envelope MBRBestMinX = new Envelope();
        Envelope MBRBestMaxY = new Envelope();
        Envelope MBRBestMinY = new Envelope();

        // getMaxX doit être le + petit possible pr rect1
        // getMinX doit être le + grand possible pr rect2
        for (MBRNode seed : node.subnodes) {
            if (seed.MBR.getMaxX() < bestMaxX) {
                bestMaxX = seed.MBR.getMaxX();
                System.out.println("best1" + seed.label);
                MBRBestMaxX = seed.MBR;

            }
            if (seed.MBR.getMinX() > bestMinX) {
                System.out.println("best2" + seed.label);

                bestMinX = seed.MBR.getMinX();
                MBRBestMinX = seed.MBR;

            }

            if (seed.MBR.getMaxY() < bestMaxY) {
                bestMaxY = seed.MBR.getMaxY();
                System.out.println("best3" + seed.label);

                MBRBestMaxY = seed.MBR;

            }
            if (seed.MBR.getMinY() > bestMinY) {
                System.out.println("best4" + seed.label);

                bestMinY = seed.MBR.getMinY();
                MBRBestMinY = seed.MBR;

            }
        }

        // normalize
        double widthInner = MBRBestMinX.getMaxX() - MBRBestMaxX.getMinX();
        double widthOuter = MBRBestMinX.getMinX() - MBRBestMaxX.getMaxX();
        double separationWidth = widthInner / widthOuter;

        double heightInner = MBRBestMinY.getMaxY() - MBRBestMaxY.getMinY();
        double heightOuter = MBRBestMinY.getMinY() - MBRBestMaxY.getMaxY();
        double separationHeight = heightInner / heightOuter;
        ArrayList<MBRNode> foundSeeds = new ArrayList<MBRNode>();

        if (separationWidth > separationHeight) {
            foundSeeds.add(new MBRNode(MBRBestMinX));
            foundSeeds.add(new MBRNode(MBRBestMaxX));

            System.out.println("seed best max x:" + MBRBestMaxX);

            System.out.println("seed best min x:" + MBRBestMinX);

        } else {
            foundSeeds.add(new MBRNode(MBRBestMinY));
            foundSeeds.add(new MBRNode(MBRBestMaxY));

            System.out.println("seed best max y:" + MBRBestMaxY);

            System.out.println("seed best min y:" + MBRBestMinY);
        }

        return foundSeeds;
    }

    void pickNext(MBRNode splitSeed1, MBRNode splitSeed2, ArrayList<MBRNode> copiedSubnodes) {
        for (MBRNode subnode : copiedSubnodes) {

            Envelope expandedMBR1 = new Envelope(splitSeed1.MBR);
            Envelope expandedMBR2 = new Envelope(splitSeed2.MBR);

            expandedMBR1.expandToInclude(subnode.MBR);
            expandedMBR2.expandToInclude(subnode.MBR);
            if (expandedMBR1.getArea() - splitSeed1.MBR.getArea() > expandedMBR2.getArea()
                    - splitSeed2.MBR.getArea()) {
                splitSeed2.subnodes.add(subnode);
                subnode.parent = splitSeed2;
                splitSeed2.MBR.expandToInclude(subnode.MBR);
            } else {
                splitSeed1.subnodes.add(subnode);
                subnode.parent = splitSeed1;
                Double blu = splitSeed1.MBR.getArea();
                System.out.println(subnode.label + " " + blu);
                // expandMBR(seed1, subnode.MBR);
                // splitSeed1.MBR.expandToInclude(subnode.MBR);
                Double blu2 = splitSeed1.MBR.getArea();
                System.out.println(subnode.label + " 2 " + blu2);
                splitSeed1.MBR.expandToInclude(subnode.MBR);
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
    ArrayList<MBRNode> pickSeedsQuadratic(MBRNode node) {
        ArrayList<MBRNode> seeds = new ArrayList<MBRNode>();
        // parcourir tout l'arbre
        MBRNode seed1 = new MBRNode();
        MBRNode seed2 = new MBRNode();

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

}
