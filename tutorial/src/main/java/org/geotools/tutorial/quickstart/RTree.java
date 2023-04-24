/*
 * Project : 
 * Date: Thursday, March 30th 2023, 12:09:05 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;

abstract class RTree {


    abstract MBRNode addLeaf(MBRNode n, MBRNode nodeToAdd) throws Exception;

    /**
     * @param node
     * @param polygon
     * @return MBRNode
     */
    abstract MBRNode chooseNode(MBRNode bestNode, MBRNode nodeToAdd);

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

    abstract Boolean expandMBR(MBRNode node, Envelope MBR);



    /**
     * 
     * @param node
     * @return
     * @throws Exception
     */
    abstract MBRNode split(MBRNode node) throws Exception ;

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
