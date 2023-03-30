package org.geotools.tutorial.quickstart;

import java.util.ArrayList;

import org.locationtech.jts.geom.Polygon;

public class Node {
    ArrayList<Node> subnodes = new ArrayList<Node>();
    String label;
    Polygon polygon;
    
    public Node(String label, Polygon polygon) {
        this.label = label;
        this.polygon = polygon;
  }
}
