package org.geotools;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.tutorial.quickstart.MBRNode;
import org.geotools.tutorial.quickstart.RTreeLinear;
import org.geotools.tutorial.quickstart.RTreeQuadratic;
import org.junit.Test;
import org.locationtech.jts.geom.Point;

/**
 * Unit test for simple App.
 */
public class AppTest {
    String worldMap = "../tutorial/maps/WB_countries_Admin0_10m.shp";
    String belgiumMap = "../tutorial/maps/sh_statbel_statistical_sectors_31370_20220101.shp";


    // Algorithme Linéaire

    //Carte de la belgique
    @Test
    public void pointDansLaPlaineL() throws IOException {
        String filename = belgiumMap;
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        // Point p = gb.point(-119.0, 56.0);
        Point p = gb.point(152183, 167679);// Plaine
        RTreeLinear rtree = new RTreeLinear(filename, "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found =" + node.label + "end");
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("CAMPUS UNIVERSITAIRE"));
    }


    @Test
    public void pointJusteEnDehorsL() throws IOException {
        String filename = belgiumMap;
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(215113.0, 217404.0);// Plaine
        RTreeLinear rtree = new RTreeLinear(filename, "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node == null);
    }

    // Carte du monde
    @Test
    public void pointInKazakhstanL() throws IOException {
        String filename = worldMap;
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(73.0, 43.0);// Plaine
        RTreeLinear rtree = new RTreeLinear(filename, "NAME_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Kazakhstan"));
    }

    @Test
    public void shouldAnswerWithTrueL() throws IOException {
        String filename = worldMap;
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-119.0, 56.0);
        RTreeLinear rtree = new RTreeLinear(filename, "NAME_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Canada"));
    }

    //Algorithme Quadratique

    // Carte de la belgique
    @Test
    public void pointDansLaPlaineQ() throws IOException {
        String filename = belgiumMap;
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        // Point p = gb.point(-119.0, 56.0);
        Point p = gb.point(152183, 167679);// Plaine
        RTreeQuadratic rtree = new RTreeQuadratic(filename, "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found =" + node.label + "end");
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("CAMPUS UNIVERSITAIRE"));
    }

    @Test
    public void pointJusteEnDehorsQ() throws IOException {
        String filename = belgiumMap;
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(215113.0, 217404.0);// Plaine
        RTreeQuadratic rtree = new RTreeQuadratic(filename, "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node == null);
    }

    // Carte du monde
    @Test
    public void pointInKazakhstanQ() throws IOException {
        String filename = worldMap;
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(73.0, 43.0);// Plaine
        RTreeQuadratic rtree = new RTreeQuadratic(filename, "NAME_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Kazakhstan"));
    }

    @Test
    public void shouldAnswerWithTrueQ() throws IOException {
        String filename = worldMap;
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-119.0, 56.0);
        RTreeQuadratic rtree = new RTreeQuadratic(filename, "NAME_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Canada"));
    }
}
