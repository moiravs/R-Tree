/*
 * Project for the course of INFO-F203 : R-Trees
 * Date: Thursday, March 30th 2023, 12:09:05 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

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
    public MBRNode root = new MBRNode();
    private static int N;

    /**
     * Crée le RTree
     * 
     * @param file          Objet File
     * @param valueProperty Attribut de la propriété à récupérer
     * @param _N            Le nombre d'enfants maximum pour un node
     * @throws IOException File Error in geotools
     */
    RTree(File file, int _N) throws IOException {
        N = _N;
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection all_features = featureSource.getFeatures();

        store.dispose();
        SimpleFeatureIterator iterator = all_features.features();
        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            MultiPolygon multiPolygon = (MultiPolygon) feature.getDefaultGeometry();
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

                if (polygon != null && root != null) {
                    MBRNode nodeToAdd = new MBRNode(polygon, feature);
                    addLeaf(root, nodeToAdd);
                }
            }
        }
        System.out.println(all_features.size() + " features");
    }

    /**
     * Recherche d'un point parmi le R-Tree
     * 
     * @param node
     * @param point
     * @return le node trouvé
     */
    public MBRNode search(MBRNode node, Point point) {
        if (node.subnodes.size() == 0) {
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

    /**
     * Associe chaque noeud à la meilleure des deux seed
     * 
     * @param splitSeed1
     * @param splitSeed2
     * @param subnodes
     */
    public void pickNext(MBRNode splitSeed1, MBRNode splitSeed2, ArrayList<MBRNode> subnodes) {
        for (MBRNode subnode : subnodes) {
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

    /**
     * Augmente le MBR du noeud "node" pour inclure l'enveloppe "MBR"
     * 
     * @param node
     * @param MBR
     */
    public void expandMBR(MBRNode node, Envelope MBR) {
        node.MBR.expandToInclude(MBR);
        if (node != root) {
            expandMBR(node.parent, MBR);
        }
    }

    /**
     * Split un node en choisissant deux seeds parmi ses enfants. Associe le restant
     * des enfants aux meilleures seeds.
     * 
     * @param node le node à split
     */
    public void split(MBRNode node) {
        ArrayList<MBRNode> splitSeeds = pickSeeds(node);
        if (splitSeeds != null) {
            pickNext(splitSeeds.get(0), splitSeeds.get(1), node.subnodes);
            node.subnodes = new ArrayList<MBRNode>();
            splitSeeds.get(0).parent = node;
            splitSeeds.get(1).parent = node;
            node.subnodes.add(splitSeeds.get(0));
            node.subnodes.add(splitSeeds.get(1));
        }
    }

    /**
     * Ajoute une nouvelle feuille dans le R-Tree. Si le nombre d'enfants de "n"
     * dépasse une certaine constante "N", la méthode split est appelée.
     * 
     * @param n
     * @param nodeToAdd
     * @throws Exception
     */
    public void addLeaf(MBRNode n, MBRNode nodeToAdd) {
        if (n.subnodes.size() == 0 || n.subnodes.get(0).subnodes.size() == 0) {
            n.subnodes.add(nodeToAdd);
            nodeToAdd.parent = n;
            expandMBR(n, nodeToAdd.MBR);
        } else {
            n = chooseNode(root, nodeToAdd);
            addLeaf(n, nodeToAdd);
        }
        if (n.subnodes.size() >= N) {
            split(n);
        }
    }

    /**
     * Choisi le meilleur noeud, soit le noeud pour lequel l'expansion de son MBR
     * par le MBR du "nodeToAdd" est la plus petite.
     * 
     * @param bestNode
     * @param nodeToAdd
     * @return
     */
    public MBRNode chooseNode(MBRNode bestNode, MBRNode nodeToAdd) {
        if (bestNode.subnodes.isEmpty() || bestNode.subnodes.get(0).subnodes.isEmpty()) {
            return bestNode;
        } else {
            MBRNode bestChildNode = new MBRNode();
            double smallestEnlargementArea = Double.POSITIVE_INFINITY;

            for (MBRNode subnode : bestNode.subnodes) {
                Envelope copiedEnvelope = new Envelope(subnode.MBR);
                copiedEnvelope.expandToInclude(nodeToAdd.MBR);
                double enlargementArea = (copiedEnvelope.getArea() - nodeToAdd.MBR.getArea());
                if (enlargementArea < smallestEnlargementArea) {
                    smallestEnlargementArea = enlargementArea;
                    bestChildNode = subnode;
                }
            }
            return chooseNode(bestChildNode, nodeToAdd);
        }
    }

    /**
     * Méthode qui choisi les meilleurs seeds
     * 
     * @param node
     * @return Les deux seeds choisies
     */
    abstract ArrayList<MBRNode> pickSeeds(MBRNode node);
}
