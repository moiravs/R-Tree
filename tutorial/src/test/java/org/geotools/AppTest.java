package org.geotools;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.tutorial.quickstart.FileLoader;
import org.geotools.tutorial.quickstart.MBRNode;
import org.geotools.tutorial.quickstart.RTreeLinear;
import org.geotools.tutorial.quickstart.RTreeQuadratic;
import org.junit.After;
import org.junit.Before;
//import org.junit.Test;
import org.locationtech.jts.geom.Point;

/**
 * Unit test for simple App.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) 
public class AppTest {
    String worldMap = "../tutorial/maps/WB_countries_Admin0_10m.shp";
    String belgiumMap = "../tutorial/maps/sh_statbel_statistical_sectors_31370_20220101.shp";
    long startTime;
    long endTime;

    @Before
    public void timer(){
        startTime = System.currentTimeMillis();
    }

    @After
    public void endTimer() {
        endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime));
    }
    // Algorithme Linéaire

    //Carte de la belgique
    @Test
    @Order(1)
    public void pointDansLaPlaineL() throws IOException {
        String filename = belgiumMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        // Point p = gb.point(-119.0, 56.0);
        Point p = gb.point(152183, 167679);// Plaine
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found =" + node.label + "end");
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("CAMPUS UNIVERSITAIRE"));
    }


    @Test
    @Order(2)
    public void pointJusteEnDehorsL() throws IOException {
        String filename = belgiumMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(215113.0, 217404.0);// Plaine
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node == null);
    }

    // Carte du monde
    @Test
    @Order(3)
    public void pointInKazakhstanL() throws IOException {
        String filename = worldMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(73.0, 43.0);// Plaine
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "NAME_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Kazakhstan"));
    }

    @Test
    @Order(4)
    public void shouldAnswerWithTrueL() throws IOException {
        String filename = worldMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-119.0, 56.0);
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "NAME_FR");
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
    @Order(4)
    public void pointDansLaPlaineQ() throws IOException {
        String filename = belgiumMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        // Point p = gb.point(-119.0, 56.0);
        Point p = gb.point(152183, 167679);// Plaine
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found =" + node.label + "end");
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("CAMPUS UNIVERSITAIRE"));
    }

    @Test
    @Order(5)
    public void pointJusteEnDehorsQ() throws IOException {
        String filename = belgiumMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(215113.0, 217404.0);// Plaine
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node == null);
    }

    // Carte du monde
    @Test
    @Order(6)
    public void pointInKazakhstanQ() throws IOException {
        String filename = worldMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(73.0, 43.0);// Plaine
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "NAME_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Kazakhstan"));
    }

    @Test
    @Order(7)
    public void shouldAnswerWithTrueQ() throws IOException {
        String filename = worldMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-119.0, 56.0);
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "NAME_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Canada"));
    }
}
