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
  String label;
  XRectangle2D MBR;
  MultiPolygon polygon;

  public Node(){}

  public Node(String label, MultiPolygon polygon) {
    this.label = label;
    this.polygon = polygon;
    createMBR();
  }
  public void createMBR(){
    Envelope mbr = polygon.getEnvelopeInternal();
    MBR = XRectangle2D.createFromExtremums(mbr.getMinX(), mbr.getMinY(), mbr.getMaxX(), mbr.getMaxY());
  }
}
