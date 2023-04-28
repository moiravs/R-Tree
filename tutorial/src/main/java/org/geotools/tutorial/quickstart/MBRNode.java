/*
 * Project for the course of INFO-F203 : R-Trees
 * Created Date: Friday, March 24th 2023, 4:42:12 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */
package org.geotools.tutorial.quickstart;

import java.util.ArrayList;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;

public class MBRNode {
  ArrayList<MBRNode> subnodes = new ArrayList<MBRNode>();
  Envelope MBR;
  Polygon polygon;
  MBRNode parent;
  public SimpleFeature feature;

  /**
   * Crée une copie d'un MBRNode
   * 
   * @param node
   */
  public MBRNode(MBRNode node) {
    this.MBR = node.MBR;
    this.polygon = node.polygon;
    this.parent = node.parent;
    this.feature = node.feature;

  }

  public String getLabel(String name) {
    String label = feature.getProperty(name).getValue().toString();
    return label;
  }

  /**
   * Crée un MBRNode à partir d'une enveloppe.
   * 
   * @param MBR
   */
  public MBRNode(Envelope MBR) {
    this.MBR = MBR;
  }

  /**
   * Crée un MBRNode à partir d'un label et d'un polygone
   * 
   * @param label
   * @param polygon
   */
  public MBRNode(Polygon polygon, SimpleFeature feature) {
    this.polygon = polygon;
    MBR = polygon.getEnvelopeInternal(); // crée le MinimumBoundingRectangle associé au polygone
    this.feature = feature;
  }

  /**
   * Crée un MBRNode vide avec le label donné en paramètre (utilisé pour le root)
   * 
   * @param label
   */
  public MBRNode() {
    subnodes = new ArrayList<MBRNode>();
    MBR = new Envelope(0, 0, 0, 0);
  }

}
