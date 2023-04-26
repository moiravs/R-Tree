/*
 * Project for the course of INFO-F203 : R-Trees
 * Date: Saturday, March 18th 2023, 5:42:19 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import java.awt.Color;
import java.util.Random;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

public class Quickstart {

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and
     * displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        String filename = "../tutorial/maps/WB_countries_Admin0_10m.shp";
        FileLoader loader = new FileLoader(filename);

        FileDataStore store = FileDataStoreFinder.getDataStore(loader.loadFile());
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection all_features = featureSource.getFeatures();

        ReferencedEnvelope global_bounds = featureSource.getBounds();

        Random r = new Random();

        GeometryBuilder gb = new GeometryBuilder();

        Point p = gb.point(r.nextInt((int) global_bounds.getMinX(), (int) global_bounds.getMaxX()),
                r.nextInt((int) global_bounds.getMinY(), (int) global_bounds.getMaxY()));

        // Créer le R-Tree
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "NAME_FR", 4);
        long startTimeGlobal = System.currentTimeMillis();

        // Rechercher le point dans le R-Tree
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();

        // Afficher le temps total d'exécution
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal));

        // pour afficher l'arbre créé : rtree.root.print(1);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        System.out.println(all_features.size() + " features");
        System.exit(0);
    }

}