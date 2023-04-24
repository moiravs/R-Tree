package org.geotools.tutorial.quickstart;

import org.opengis.feature.simple.SimpleFeature;

import jj2000.j2k.util.ArrayUtil;

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

public class RTreeLinear extends RTree {
    private static final int N = 4;
    private static double smallestEnlargementArea = Double.POSITIVE_INFINITY;
    public MBRNode root = new MBRNode("root");
    MBRNode searchNode;

    public RTreeLinear(String filename, String valueProperty) throws IOException {
        int i = 0;
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
    public ArrayList<MBRNode> pickSeeds(MBRNode node) throws Exception {
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
                MBRBestMaxX = seed.MBR;
            }
            if (seed.MBR.getMinX() > bestMinX) {
                bestMinX = seed.MBR.getMinX();
                MBRBestMinX = seed.MBR;
            }

            if (seed.MBR.getMaxY() < bestMaxY) {
                bestMaxY = seed.MBR.getMaxY();
                MBRBestMaxY = seed.MBR;
            }
            if (seed.MBR.getMinY() > bestMinY) {
                bestMinY = seed.MBR.getMinY();
                MBRBestMinY = seed.MBR;
            }
        }

        // normalize
        ArrayList<MBRNode> foundSeeds = new ArrayList<MBRNode>();
        if (MBRBestMinY == MBRBestMaxY && MBRBestMaxX == MBRBestMinX) {
            return null;
        } else if (MBRBestMinY == MBRBestMaxY) {
            foundSeeds.add(new MBRNode(MBRBestMinX));
            foundSeeds.add(new MBRNode(MBRBestMaxX));
        } else if (MBRBestMinX == MBRBestMaxX) {

            foundSeeds.add(new MBRNode(MBRBestMinY));
            foundSeeds.add(new MBRNode(MBRBestMaxY));
        } else {
            double widthInner = MBRBestMinX.getMaxX() - MBRBestMaxX.getMinX();
            double widthOuter = MBRBestMinX.getMinX() - MBRBestMaxX.getMaxX();
            double separationWidth = widthInner / widthOuter;

            double heightInner = MBRBestMinY.getMaxY() - MBRBestMaxY.getMinY();
            double heightOuter = MBRBestMinY.getMinY() - MBRBestMaxY.getMaxY();
            double separationHeight = heightInner / heightOuter;
            if (separationWidth > separationHeight) {

                foundSeeds.add(new MBRNode(MBRBestMinX));
                foundSeeds.add(new MBRNode(MBRBestMaxX));

            } else {

                foundSeeds.add(new MBRNode(MBRBestMinY));
                foundSeeds.add(new MBRNode(MBRBestMaxY));
            }
        }

        return foundSeeds;
    }
}
