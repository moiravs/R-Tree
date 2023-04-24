package org.geotools;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.tutorial.quickstart.FileLoader;
import org.geotools.tutorial.quickstart.MBRNode;
import org.geotools.tutorial.quickstart.RTreeLinear;
import org.geotools.tutorial.quickstart.RTreeQuadratic;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.OrderWith;
import org.locationtech.jts.geom.Point;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
/**
 * Unit test for simple App.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest {
    String worldMap = "../tutorial/maps/WB_countries_Admin0_10m.shp";
    String belgiumMap = "../tutorial/maps/sh_statbel_statistical_sectors_31370_20220101.shp";
    long startTime;
    private static int i = 0;
    long endTime;

    @Before
    public void timer(){
        i+=1;
        System.out.println("Starting " + i + " test");
        startTime = System.currentTimeMillis();
    }

    @After
    public void endTimer() {
        endTime = System.currentTimeMillis();
        System.out.println("Finishing " + i + " test");
        System.out.println("Total execution time: " + (endTime - startTime));
    }
    // 1er test - Carte de la belgique - Algorithme linéaire
    @Test
    public void ApointDansLaPlaineL() throws IOException {
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

    // 2ème test - Carte de la belgique - Algorithme linéaire
    @Test
    public void BpointJusteEnDehorsL() throws IOException {
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

    // 3ème test - Carte du monde - Algorithme linéaire
    @Test
    public void CpointInKazakhstanL() throws IOException {
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

    // 4ème test - Carte du monde - Algorithme linéaire
    @Test
    public void DshouldAnswerWithTrueL() throws IOException {
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

    // 5ème test - Carte de la belgique - Algorithme quadratique
    @Test
    public void EpointDansLaPlaineQ() throws IOException {
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

    // 6ème test - Carte de la belgique - Algorithme quadratique
    @Test
    public void FpointJusteEnDehorsQ() throws IOException {
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

    // 7ème test - Carte du monde - Algorithme quadratique
    @Test
    public void GpointInKazakhstanQ() throws IOException {
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

    // 8ème test - Carte du monde - Algorithme quadratique
    @Test
    public void HshouldAnswerWithTrueQ() throws IOException {
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
