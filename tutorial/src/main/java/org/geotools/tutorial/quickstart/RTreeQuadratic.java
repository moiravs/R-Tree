package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Envelope;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RTreeQuadratic extends RTree {

    public RTreeQuadratic(File file, String valueProperty) throws IOException {
        super(file, valueProperty);
    }

    /**
     * For each pair of Entries compose a rectangle and pick the one with largest d
     * Choisir les deux seeds les + éloignées possibles
     * pair with de largest d, d = area(J) - area(E1*I) - area(E2*I)
     * 
     * @return
     * @throws Exception
     */
    public ArrayList<MBRNode> pickSeeds(MBRNode node) throws Exception {
        double maxArea = 0;
        int M = node.subnodes.size();
        MBRNode seed1 = new MBRNode("test");
        MBRNode seed2 = new MBRNode("test");
        Envelope copiedEnvelope;
        ArrayList<MBRNode> seeds = new ArrayList<MBRNode>();

        for (int i = 0; i < M; i++) {
            for (int j = i + 1; j < M; j++) { // expliquer dans le rapport pq on prend j +1
                copiedEnvelope = new Envelope(node.subnodes.get(i).MBR);
                copiedEnvelope.expandToInclude(node.subnodes.get(j).MBR);
                double area = copiedEnvelope.getArea() - node.subnodes.get(i).MBR.getArea() - node.subnodes.get(j).MBR.getArea();
                if (area > maxArea) {
                    maxArea = area;
                    seed1 = new MBRNode(new MBRNode(node.subnodes.get(i).MBR));
                    seed2 = new MBRNode(new MBRNode(node.subnodes.get(j).MBR));
                }
            }
        }

        seeds.add(seed1);
        seeds.add(seed2);
        return seeds;
    }

}
