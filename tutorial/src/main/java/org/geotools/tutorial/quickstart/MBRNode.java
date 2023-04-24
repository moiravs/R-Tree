/*
 * Project : 
 * Created Date: Friday, March 24th 2023, 4:42:12 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import java.util.ArrayList;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;

public class MBRNode {
  ArrayList<MBRNode> subnodes = new ArrayList<MBRNode>();
  public String label;
  Envelope MBR;
  MultiPolygon polygon;
  MBRNode parent;


  public MBRNode(MBRNode node) {
    this.label = node.label;
    this.MBR = node.MBR;
    this.polygon = node.polygon;
    this.parent = node.parent;
  }

  public MBRNode(Envelope MBR) {
    this.MBR = MBR;
    this.label = "SplitSeed";
  }

  public MBRNode(String label, MultiPolygon polygon) {
    this.label = label;
    this.polygon = polygon;
    createMBR();
  }

  public MBRNode(String label) {
    subnodes = new ArrayList<MBRNode>();
    this.label = label;
    MBR = new Envelope(0, 0, 0, 0);
  }

  public void createMBR() {
    MBR = polygon.getEnvelopeInternal();
  }

  public void print(int level) throws Exception {
    for (int i = 1; i < level; i++) {
      System.out.println(this.label);
      System.out.print("\t");
    }
    if (this.subnodes.isEmpty() && this.label== "SplitSeed"){
      throw new Exception("AIEAIEOUILLE");
    }
    for (MBRNode child : this.subnodes) {
      child.print(level + 1);
    }
  }
}
