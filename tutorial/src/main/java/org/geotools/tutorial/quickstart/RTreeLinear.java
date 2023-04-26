package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Envelope;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RTreeLinear extends RTree {

    public RTreeLinear(File file, String valueProperty, int N) throws IOException {
        super(file, valueProperty, N);
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
