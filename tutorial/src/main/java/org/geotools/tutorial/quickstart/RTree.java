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
                System.out.println(node.label);
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

    abstract void pickNext(MBRNode splitSeed1, MBRNode splitSeed2, ArrayList<MBRNode> copiedSubnodes);


}
