/*
 * Project for the course of INFO-F203 : R-Trees
 * Date: Thursday, March 30th 2023, 12:09:05 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */
package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Envelope;

import java.io.File;
import java.util.ArrayList;

public class RTreeLinear extends RTree {

    public RTreeLinear(File file, String valueProperty, int N) {
        super(file, valueProperty, N);
    }

    /**
     * Choisi les deux meilleures seeds parmi les enfants du paramètre node.
     * 
     * @param node
     * @return Les deux seeds choisies
     */
    public ArrayList<MBRNode> pickSeeds(MBRNode node) {
        double bestMaxX = Double.POSITIVE_INFINITY;
        double bestMinX = Double.NEGATIVE_INFINITY;
        double bestMaxY = Double.POSITIVE_INFINITY;
        double bestMinY = Double.NEGATIVE_INFINITY;
        Envelope MBRBestMaxX = new Envelope();
        Envelope MBRBestMinX = new Envelope();
        Envelope MBRBestMaxY = new Envelope();
        Envelope MBRBestMinY = new Envelope();

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

        // normalisation
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
            double innerWidth = MBRBestMinX.getMaxX() - MBRBestMaxX.getMinX();
            double outerWidth = MBRBestMinX.getMinX() - MBRBestMaxX.getMaxX();
            double separationWidth = innerWidth / outerWidth;

            double innerHeight = MBRBestMinY.getMaxY() - MBRBestMaxY.getMinY();
            double outerHeight = MBRBestMinY.getMinY() - MBRBestMaxY.getMaxY();
            double separationHeight = innerHeight / outerHeight;
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
