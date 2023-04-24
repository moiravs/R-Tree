package org.geotools.tutorial.quickstart;

import org.opengis.feature.simple.SimpleFeature;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
public class RTreeQuadratic extends RTree {
    private static final int N = 4;
    private static double smallestEnlargementArea = Double.POSITIVE_INFINITY;
    public MBRNode root = new MBRNode("root");
    MBRNode searchNode;

    public RTreeQuadratic(String filename, String valueProperty) throws IOException {
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
     */
    MBRNode splitQuadratique(MBRNode node) {
        pickSeeds(node);
        return node;
    }

    /**
     * 
     * @param node
     * @return
     * @throws Exception
     */
    public MBRNode split(MBRNode node) throws Exception {
        ArrayList<MBRNode> copiedSubnodes = new ArrayList<MBRNode>(node.subnodes);
        ArrayList<MBRNode> splitSeeds = pickSeeds(node);
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

    /**
     * For each pair of Entries compose a rectangle and pick the one with largest d
     * Choisir les deux seeds les + éloignées possibles
     * pair with de largest d, d = area(J) - area(E1*I) - area(E2*I)
     * 
     * @return
     */
    public ArrayList<MBRNode> pickSeeds(MBRNode node) {
        /*
         * ArrayList<MBRNode> seeds = new ArrayList<MBRNode>();
         * // parcourir tout l'arbre
         * MBRNode seed1 = new MBRNode("seed1");
         * MBRNode seed2 = new MBRNode("seed2");
         * 
         * // créer un rectangle avec les MBR des nodes dedans
         * double rectangleMinX = Double.min(seed1.MBR.getMinX(), seed2.MBR.getMinX());
         * double rectangleMaxX = Double.max(seed1.MBR.getMaxX(), seed2.MBR.getMaxX());
         * double rectangleMinY = Double.min(seed1.MBR.getMinY(), seed2.MBR.getMinY());
         * double rectangleMaxY = Double.max(seed1.MBR.getMaxY(), seed2.MBR.getMaxY());
         * 
         * XRectangle2D rect = XRectangle2D.createFromExtremums(rectangleMinX,
         * rectangleMinY, rectangleMaxX,
         * rectangleMaxY);
         * // avoir l'area du rectangle - l'aire des deux MBR des nodes
         * // double area = rect.getHeight() * rect.getWidth()
         * // - (seed1.MBR.getHeight() * seed1.MBR.getWidth() + seed2.MBR.getHeight()
         * // * seed2.MBR.getWidth());
         * // comparer avec le meilleur rectangle
         * 
         * // return les seeds correspondant au plus grand area
         * return seeds;
         */
        double maxArea = 0;
        int M = node.subnodes.size();
        MBRNode s, t;
        s = new MBRNode(node);
        t = new MBRNode(node);
        ArrayList<MBRNode> seeds = new ArrayList<MBRNode>();
        seeds.add(s);
        seeds.add(t);
        for (int i = 1; i < M; i++) {
            for (int j = i + 1; j < M + 1; j++) {
                Envelope r = new Envelope(node.MBR);
                expandMBR(node.subnodes.get(i), r);
                double area = r.getArea() - node.subnodes.get(i).MBR.getArea() - node.subnodes.get(j).MBR.getArea();
                if (area > maxArea) {
                    maxArea = area;
                    s = node.subnodes.get(i);
                    t = node.subnodes.get(j);
                }
            }
        }
        return seeds;
    }

    void distribute(MBRNode node, MBRNode node1, MBRNode node2) {
        return;
    }

    
}
