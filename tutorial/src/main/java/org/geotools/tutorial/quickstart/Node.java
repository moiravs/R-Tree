/*
 * Project : 
 * Created Date: Friday, March 24th 2023, 4:42:12 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import java.util.ArrayList;

import org.geotools.geometry.util.XRectangle2D;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class Node {
  ArrayList<Node> subnodes = new ArrayList<Node>();
  public String label;
  Envelope MBR;
  MultiPolygon polygon;
  Node parent;

  public Node(){
    this.label = "root";
    MBR = new Envelope(0, 0, 0, 0);
  }

  public Node(Node node){
    this.label = node.label;
    this.MBR = node.MBR;
    this.polygon = node.polygon;
    this.parent = node.parent;
  }

  public Node(String label, MultiPolygon polygon) {
    this.label = label;
    this.polygon = polygon;
    createMBR();
  }
  public void createMBR(){
    MBR = polygon.getEnvelopeInternal();
  }
}
