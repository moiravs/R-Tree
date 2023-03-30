package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Polygon;

public class RTree {
    private static final int N = 3;

    Node addLeaf(Node node, String label, Polygon polygon) {
        if (node.subnodes.size() == 0) { // if bottom level is reached -> create leaf
            node.subnodes.add(new Node(label, polygon)); // create leaf
        } else { // still need to go deeper
            node = chooseNode(node, polygon);
            Node new_node = addLeaf(node, label, polygon);
            if (new_node != null) {
                // a split occurred in addLeaf ,
                // a new node is added at this level
                node.subnodes.add(new_node);
                // expand node . mbr to include polygon
            }
        }

        if (node.subnodes.size() >= N) {
            return split(node);
        } else {
            return null;
        }

    }

    Node chooseNode(Node node, Polygon polygon) {
        return node;
    }

    void expandMBR(Node node, Polygon polygon) {
        // expand node . mbr to include polygon???
        // wtf am i suppose to do here
    }

    Node split(Node node) {
        return node;
    }
}
