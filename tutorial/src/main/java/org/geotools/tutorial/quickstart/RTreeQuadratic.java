package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Envelope;
import java.io.IOException;
import java.util.ArrayList;
public class RTreeQuadratic extends RTree {
    private static final int N = 4;
    private static double smallestEnlargementArea = Double.POSITIVE_INFINITY;
    public MBRNode root = new MBRNode("root");
    MBRNode searchNode;

    public RTreeQuadratic(String filename, String valueProperty) throws IOException {
        super(filename, valueProperty);
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



    /**
     * For each pair of Entries compose a rectangle and pick the one with largest d
     * Choisir les deux seeds les + éloignées possibles
     * pair with de largest d, d = area(J) - area(E1*I) - area(E2*I)
     * 
     * @return
     */
    public ArrayList<MBRNode> pickSeeds(MBRNode node) {
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
