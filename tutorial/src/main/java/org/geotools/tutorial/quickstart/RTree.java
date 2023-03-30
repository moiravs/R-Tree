package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Polygon;

public class RTree {
    void addLeaf(Node node, String label, Polygon polygon) {
        if (node.subnodes.size()==0) { //if bottom level is reached -> create leaf
            node.subnodes.add(new Node(label, polygon));
        }
        /*
         * if size ( n . subnodes )==0 or (n . subnodes [0]).child == nullptr :
         * # bottom level is reached -> create leaf
         * n.subnodes.add(new Leaf(name,polygon))
         * else : # still need to go deeper
         * n = chooseNode ( node , polygon )
         * new_node = addLeaf (n , label , polygon )
         * if new_node != null :
         * # a split occurred in addLeaf ,
         * # a new node is added at this level
         * subnodes . add ( new_node )
         * expand node . mbr to include polygon
         * 
         * if size ( node . subnodes ) >= N :
         * return split(node)
         * else :
         * return null
         */

    }
}
