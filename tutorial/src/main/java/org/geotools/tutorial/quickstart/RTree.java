/*
 * Project : 
 * Date: Thursday, March 30th 2023, 12:09:05 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.opengis.feature.simple.SimpleFeature;


import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.MultiPolygon;


abstract class RTree {
    public MBRNode root = new MBRNode("root");
    private static final int N = 4;
    private static double smallestEnlargementArea = Double.POSITIVE_INFINITY;

    RTree(File file, String valueProperty) throws IOException{
        int i = 0;
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection all_features = featureSource.getFeatures();

        store.dispose();
        try (SimpleFeatureIterator iterator = all_features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();

                MultiPolygon polygon = (MultiPolygon) feature.getDefaultGeometry();
                if (polygon != null && root != null && i != 50) {
                    String label = feature.getProperty(valueProperty).getValue().toString();
                    MBRNode nodeToAdd = new MBRNode(label, polygon);
                    try {
                        addLeaf(root, nodeToAdd);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
            }
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


    public void pickNext(MBRNode splitSeed1, MBRNode splitSeed2, ArrayList<MBRNode> copiedSubnodes) {
        for (MBRNode subnode : copiedSubnodes) {
            if (subnode.MBR == splitSeed1.MBR) {
                splitSeed1.subnodes.add(subnode);
                subnode.parent = splitSeed1;
                splitSeed1.MBR.expandToInclude(subnode.MBR);
            } else if (subnode.MBR == splitSeed2.MBR) {
                splitSeed2.subnodes.add(subnode);
                subnode.parent = splitSeed2;
                splitSeed2.MBR.expandToInclude(subnode.MBR);
            } else {
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
                    splitSeed1.MBR.expandToInclude(subnode.MBR);
                }
            }
        }

    }


    public Boolean expandMBR(MBRNode node, Envelope MBR) {
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
     * @throws Exception
     */
    public MBRNode split(MBRNode node) throws Exception {
        System.out.println("before");
        root.print(1);
        ArrayList<MBRNode> copiedSubnodes = new ArrayList<MBRNode>(node.subnodes);
        ArrayList<MBRNode> splitSeeds;
        splitSeeds = pickSeeds(node);
        if (splitSeeds != null) {
            node.subnodes = new ArrayList<MBRNode>();
            splitSeeds.get(0).parent = node;
            splitSeeds.get(1).parent = node;
            node.subnodes.add(splitSeeds.get(0));
            node.subnodes.add(splitSeeds.get(1));
            pickNext(splitSeeds.get(0), splitSeeds.get(1), copiedSubnodes);
        }
        return node;
    }

    public MBRNode addLeaf(MBRNode n, MBRNode nodeToAdd) throws Exception {
        if (n.subnodes.size() == 0 || n.subnodes.get(0).subnodes.size() == 0) { // if bottom level is reached -> create
            n.subnodes.add(nodeToAdd); // create leaf
            nodeToAdd.parent = n;
            expandMBR(n, nodeToAdd.MBR);
            // n.MBR.expandToInclude(nodeToAdd.MBR);
        } else { // still need to go deeper
            n = chooseNode(root, nodeToAdd);
            addLeaf(n, nodeToAdd);
        }
        if (n.subnodes.size() >= N) {
            split(n);
        }
        return null;
    }

    /**
     * @param node
     * @param polygon
     * @return MBRNode
     */
    public MBRNode chooseNode(MBRNode bestNode, MBRNode nodeToAdd) {
        if (bestNode.subnodes.isEmpty() || bestNode.subnodes.get(0).subnodes.isEmpty()) {
            return bestNode;
        } else {
            MBRNode bestChildNode = new MBRNode("SplitNode");
            for (MBRNode subnode : bestNode.subnodes) {
                Envelope copiedEnvelope = new Envelope(subnode.MBR);
                copiedEnvelope.expandToInclude(nodeToAdd.MBR);
                double enlargementArea = (copiedEnvelope.getArea() - nodeToAdd.MBR.getArea());
                if (enlargementArea < smallestEnlargementArea) {
                    smallestEnlargementArea = enlargementArea;
                    bestChildNode = subnode;
                }
            }
            if (bestChildNode.label == "SplitNode") {
                throw new RuntimeException();
            }
            smallestEnlargementArea = Double.POSITIVE_INFINITY;
            return chooseNode(bestChildNode, nodeToAdd);
        }
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
     * @throws Exception
     */
    abstract ArrayList<MBRNode> pickSeeds(MBRNode node) throws Exception;
}
