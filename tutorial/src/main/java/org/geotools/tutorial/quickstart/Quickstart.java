/*
 * Project : 
 * Date: Saturday, March 18th 2023, 5:42:19 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

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

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a
 * map frame.
 *
 * <p>
 * This is the GeoTools Quickstart application used in documentation a and
 * tutorials. *
 */
public class Quickstart {

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and
     * displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        
        String filename = "../tutorial/maps/sh_statbel_statistical_sectors_31370_20220101.shp";

        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection all_features = featureSource.getFeatures();


        ReferencedEnvelope global_bounds = featureSource.getBounds();

        Random r = new Random();

        GeometryBuilder gb = new GeometryBuilder();
        //Point p = gb.point(152183, 167679);// Plaine
        // Point p = gb.point(4.4, 50.8);//
        // Point p = gb.point(58.0, 47.0);
        //Point p = gb.point(10.6,59.9);// Oslo
        //Point p = gb.point(-119.0, 56.0);

        // Point p = gb.point(-70.9,-33.4);// Santiago
        // Point p = gb.point(169.2, -52.5);//NZ

        // Point p = gb.point(172.97365198326708, 1.8869725782923172);

        Point p = gb.point(r.nextInt((int) global_bounds.getMinX(), (int)global_bounds.getMaxX()),r.nextInt((int) global_bounds.getMinY(), (int) global_bounds.getMaxY()));
        System.out.println("point X: "+ p.getX() + "point Y:" + p.getY());
        RTree rtree = new RTree();
        rtree.createTree(filename, "T_SEC_FR");
        rtree.printTree(rtree.root);
        Node node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else 
            System.out.println("Point not in any polygon");

        SimpleFeature target = null;

        System.out.println(all_features.size() + " features");

        try (SimpleFeatureIterator iterator = all_features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();

                MultiPolygon polygon = (MultiPolygon) feature.getDefaultGeometry();

                if (polygon != null && polygon.contains(p)) {
                    target = feature;
                    break;
                }
            }
        }

        if (target == null)
            System.out.println("Point not in any polygon!");

        else {
            for (Property prop : target.getProperties()) {
                if (prop.getName().toString() != "the_geom") {
                    System.out.println(prop.getName() + ": " + prop.getValue());
                }
            }
        }

        MapContent map = new MapContent();
        map.setTitle("Projet INFO-F203");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        ListFeatureCollection collection = new ListFeatureCollection(featureSource.getSchema());
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureSource.getSchema());

        // Add target polygon
        collection.add(target);

        // Add Point
        Polygon c = gb.circle(p.getX(), p.getY(), all_features.getBounds().getWidth() / 200, 10);
        featureBuilder.add(c);
        collection.add(featureBuilder.buildFeature(null));

        // Add MBR
        if (target != null) {
            featureBuilder.add(gb.box(target.getBounds().getMinX(),
                    target.getBounds().getMinY(),
                    target.getBounds().getMaxX(),
                    target.getBounds().getMaxY()));

            // collection.add(featureBuilder.buildFeature(null));

            collection.add(featureBuilder.buildFeature(null));
        }

        Style style2 = SLD.createLineStyle(Color.red, 2.0f);
        Layer layer2 = new FeatureLayer(collection, style2);
        map.addLayer(layer2);

        // Now display the map
        JMapFrame.showMap(map);
    }

    


}