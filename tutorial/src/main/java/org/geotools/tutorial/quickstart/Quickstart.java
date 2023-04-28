/*
 * Project for the course of INFO-F203 : R-Trees
 * Date: Saturday, March 18th 2023, 5:42:19 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */

package org.geotools.tutorial.quickstart;

import java.util.Random;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.GeometryBuilder;

import org.locationtech.jts.geom.Point;
import org.opengis.feature.Property;

public class Quickstart {

    public static void main(String[] args) throws Exception {
        String filename = "../tutorial/maps/sh_statbel_statistical_sectors_3812_20220101.shp";
        FileLoader loader = new FileLoader(filename);

        FileDataStore store = FileDataStoreFinder.getDataStore(loader.loadFile());
        SimpleFeatureSource featureSource = store.getFeatureSource();
        ReferencedEnvelope global_bounds = featureSource.getBounds();

        Random r = new Random();
        GeometryBuilder gb = new GeometryBuilder();

        Point p = gb.point(r.nextInt((int) global_bounds.getMinX(), (int) global_bounds.getMaxX()),
                r.nextInt((int) global_bounds.getMinY(), (int) global_bounds.getMaxY()));

        // Créer le R-Tree
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), 4);
        long startTimeGlobal = System.currentTimeMillis();

        // Rechercher le point dans le R-Tree
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();

        // Afficher le temps total d'exécution
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal));

        if (node != null) {
            for (Property prop : node.feature.getProperties()) {
                if (prop.getName().toString() != "the_geom") {
                    System.out.println(prop.getName() + ": " + prop.getValue());
                }
            }
        } else
            System.out.println("Point not in any polygon");
        System.exit(0);
    }

}