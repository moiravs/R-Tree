package org.geotools.tutorial.quickstart;

import org.locationtech.jts.geom.Polygon;
import java.awt.Color;
import java.io.File;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;

import org.locationtech.jts.geom.MultiPolygon;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.geotools.geometry.jts.GeometryBuilder;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Random;

import org.geotools.swing.JMapFrame;

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

    Node search(Node node, Point point) { // appeller cette fonction avec la racine de l'arbre
        if (node.subnodes.size() == 0) { // si c'est une feuille
            if (node.MBR.contains(point)) {
                return node; // Si le point appartient au MBR du nœud et le point appartient au polygone, retourner "this",
            } else {
                return null;
            }
        } else {
            if (node.MBR.contains(point)){
                for (Node subnode : node.subnodes) {
                    if (search(subnode, point) != null){
                        return node;
                    }
                }
            }
        }
        return null;
        /*
         * — Pour une feuille :
         * — Si le point appartient au MBR du nœud et le point appartient au polygone,
         * retourner "this",
         * — Sinon, retourner "null" ;
         * — Pour un nœud :
         * — Si le point appartient au MBR, tester récursivement l’appartenance pour
         * chacun
         * des sous-nœuds. Dès qu’un appel récursif renvoie autre chose que null,
         * retourner
         * ce résultat,
         * — Sinon, retourner null ;
         * — Pour un arbre : appeler la fonction de recherche sur la racine.
         */
    }

    void expandMBR(Node node, Polygon polygon) {
        // expand node . mbr to include polygon???
        // wtf am i suppose to do here
    }

    Node split(Node node) {
        return node;
    }
}
