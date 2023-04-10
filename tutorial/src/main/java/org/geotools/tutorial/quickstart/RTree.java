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
    public MBRNode root = new MBRNode("root");
    MBRNode searchNode;

    public MBRNode createTree(String filename, String valueProperty) throws IOException {
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
                        // System.out.println("Start for: " + nodeToAdd.label);

                        root.print(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                    System.out.println("End of: " + nodeToAdd.label);

                    // i++;
                }
            }
        }

        return null;
    }

    MBRNode addLeaf(MBRNode n, MBRNode nodeToAdd) throws Exception {
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
            System.out.println("ihahahahan" + n.subnodes.get(0).label);
            splitLineaire(n);
        }
        return null;
    }

    /**
     * @param node
     * @param polygon
     * @return MBRNode
     */
    MBRNode chooseNode(MBRNode bestNode, MBRNode nodeToAdd) {
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
     * 
     * @param node
     * @param point
     * @return
     */
    public MBRNode search(MBRNode node, Point point) { // appeller cette fonction avec la racine de l'arbre
        if (node.subnodes.size() == 0) { // si c'est une feuille
            if (node.MBR.contains(point.getX(), point.getY())) {
                // System.out.println(node.label);
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
     * @throws Exception
     */
    MBRNode splitLineaire(MBRNode node) throws Exception {
        ArrayList<MBRNode> copiedSubnodes = new ArrayList<MBRNode>(node.subnodes);
        ArrayList<MBRNode> splitSeeds = pickSeedsLinear(node);
        node.subnodes = new ArrayList<MBRNode>();
        splitSeeds.get(0).parent = node;
        splitSeeds.get(1).parent = node;
        node.subnodes.add(splitSeeds.get(0));
        node.subnodes.add(splitSeeds.get(1));
        pickNext(splitSeeds.get(0), splitSeeds.get(1), copiedSubnodes);
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
    ArrayList<MBRNode> pickSeedsLinear(MBRNode node) throws Exception {
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
            System.out.println(seed.label);
            System.out.println(seed.MBR.getMinX());
            System.out.println(seed.MBR.getMaxX());
            System.out.println(seed.MBR.getMinY());
            System.out.println(seed.MBR.getMaxY());
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
        double widthInner = MBRBestMinX.getMaxX() - MBRBestMaxX.getMinX();
        double widthOuter = MBRBestMinX.getMinX() - MBRBestMaxX.getMaxX();
        double separationWidth = widthInner / widthOuter;

        double heightInner = MBRBestMinY.getMaxY() - MBRBestMaxY.getMinY();
        double heightOuter = MBRBestMinY.getMinY() - MBRBestMaxY.getMaxY();
        double separationHeight = heightInner / heightOuter;
        ArrayList<MBRNode> foundSeeds = new ArrayList<MBRNode>();

        if (MBRBestMinY == MBRBestMaxY && MBRBestMaxX == MBRBestMinX) {
            throw new Exception("All 4 split Nodes are the same");
        } else if (MBRBestMinY == MBRBestMaxY) {
            System.out.println("1 hihi");
            foundSeeds.add(new MBRNode(MBRBestMinX));
            foundSeeds.add(new MBRNode(MBRBestMaxX));
        } else if (MBRBestMinX == MBRBestMaxX && MBRBestMaxY != MBRBestMinY) {
            System.out.println("4 hihi");

            foundSeeds.add(new MBRNode(MBRBestMinY));
            foundSeeds.add(new MBRNode(MBRBestMaxY));
        } else if (separationWidth > separationHeight) {
            System.out.println("2 hihi");

            foundSeeds.add(new MBRNode(MBRBestMinX));
            foundSeeds.add(new MBRNode(MBRBestMaxX));

        } else if (separationWidth <= separationHeight) {
            System.out.println("3 hihi");

            foundSeeds.add(new MBRNode(MBRBestMinY));
            foundSeeds.add(new MBRNode(MBRBestMaxY));
        }

        return foundSeeds;
    }

    void pickNext(MBRNode splitSeed1, MBRNode splitSeed2, ArrayList<MBRNode> copiedSubnodes) {
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
    ArrayList<MBRNode> pickSeedsQuadratic(MBRNode node) {
        /*ArrayList<MBRNode> seeds = new ArrayList<MBRNode>();
        // parcourir tout l'arbre
        MBRNode seed1 = new MBRNode("seed1");
        MBRNode seed2 = new MBRNode("seed2");

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
        return seeds;*/
        double maxArea = 0;
        int M = node.subnodes.size();
        MBRNode s, t;
        s = new MBRNode(node);
        t = new MBRNode(node);
        ArrayList<MBRNode> seeds = new ArrayList<MBRNode>();
        seeds.add(s);
        seeds.add(t);     
        for (int i = 1; i < M; i++)
        {
            for (int j = i + 1; j < M+1; j++)
            {
                Envelope r = new Envelope(node.MBR);
                expandMBR(node.subnodes.get(i), r);
                double area = r.getArea() - node.subnodes.get(i).MBR.getArea() - node.subnodes.get(j).MBR.getArea();
                if (area > maxArea) 
                {
                    maxArea = area;
                    s = node.subnodes.get(i);
                    t = node.subnodes.get(j);
                }
            }
        }
        return seeds;
    }

    void distributeQuadratic(MBRNode node, MBRNode node1, MBRNode node2)
    {
        
    }

}
