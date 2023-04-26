/*
 * Project for the course of INFO-F203 : R-Trees
 * Created Date: Friday, March 24th 2023, 4:42:12 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */
package org.geotools.tutorial.quickstart;

import java.util.ArrayList;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;

public class MBRNode {
  ArrayList<MBRNode> subnodes = new ArrayList<MBRNode>();
  public String label;
  Envelope MBR;
  Polygon polygon;
  MBRNode parent;

  /**
   * Crée une copie d'un MBRNode
   * 
   * @param node
   */
  public MBRNode(MBRNode node) {
    this.label = node.label;
    this.MBR = node.MBR;
    this.polygon = node.polygon;
    this.parent = node.parent;
  }

  /**
   * Crée un MBRNode à partir d'une enveloppe.
   * 
   * @param MBR
   */
  public MBRNode(Envelope MBR) {
    this.MBR = MBR;
    this.label = "SplitSeed";
  }

  /**
   * Crée un MBRNode à partir d'un label et d'un polygone
   * 
   * @param label
   * @param polygon
   */
  public MBRNode(String label, Polygon polygon) {
    this.label = label;
    this.polygon = polygon;
    MBR = polygon.getEnvelopeInternal(); // crée le MinimumBoundingRectangle associé au polygone
  }

  /**
   * Crée un MBRNode vide avec le label donné en paramètre (utilisé pour le root)
   * 
   * @param label
   */
  public MBRNode(String label) {
    subnodes = new ArrayList<MBRNode>();
    this.label = label;
    MBR = new Envelope(0, 0, 0, 0);
  }

  /**
   * Affiche l'entièreté de l'arbre
   * 
   * @param level
   */
  public void print(int level) {
    for (int i = 1; i < level; i++) {
      System.out.print("\t");
    }
    System.out.println(this.label);
    for (MBRNode child : this.subnodes) {
      child.print(level + 1);
    }
  }
}
