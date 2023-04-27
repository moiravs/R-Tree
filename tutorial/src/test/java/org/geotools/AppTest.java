/*
 * Project for the course of INFO-F203 : R-Trees
 * Date: Thursday, March 30th 2023, 12:09:05 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */
package org.geotools;

import static org.junit.Assert.assertTrue;

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
import org.junit.runners.MethodSorters;

import org.locationtech.jts.geom.Point;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest {
    String worldMap = "../tutorial/maps/WB_countries_Admin0_10m.shp";
    String belgiumMap = "../tutorial/maps/sh_statbel_statistical_sectors_31370_20220101.shp";
    String franceMap = "../tutorial/maps/regions-20180101.shp";
    String japanMap = "../tutorial/maps/jpn_admbnda_adm2_2019.shp";
    long startTimeLocal;
    private static int i = 0;
    long endTimeLocal;
    private static final int N = 10000;

    @Before
    public void timer() {
        i += 1;
        System.out.println("Starting " + i + " test");
        startTimeLocal = System.currentTimeMillis();
    }

    @After
    public void endTimer() {
        endTimeLocal = System.currentTimeMillis();
        System.out.println("Finishing " + i + " test");
        System.out.println("Total execution time: " + (endTimeLocal - startTimeLocal) + " ms");
    }

    // 1er test - Carte de la Belgique - Algorithme linéaire
    @Test
    public void TestA_assertPointIsInCampus() throws IOException {
        String filename = belgiumMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(152183, 167679);
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "T_SEC_FR", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found =" + node.label + "end");
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("CAMPUS UNIVERSITAIRE"));
    }

    // 2ème test - Carte de la Belgique - Algorithme linéaire
    @Test
    public void TestB_assertPointIsNull() throws IOException {
        String filename = belgiumMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(215113.0, 217404.0);
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "T_SEC_FR", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node == null);
    }

    // 3ème test - Carte du monde - Algorithme linéaire
    @Test
    public void TestC_assertPointIsInKazakhstan() throws IOException {
        String filename = worldMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(73.0, 43.0);
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "NAME_FR", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Kazakhstan"));
    }

    // 4ème test - Carte du monde - Algorithme linéaire
    @Test
    public void TestD_assertPointIsInCanada() throws Exception {
        String filename = worldMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-119.0, 56.0);
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "NAME_FR", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        // rtree.root.print(1);
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Canada"));
    }

    // 5ème test - Carte de la France - Algorithme linéaire
    @Test
    public void TestE_assertPointIsInAuvergne() throws IOException {
        String filename = franceMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(4.44, 44.85);
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "wikipedia", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("fr:Auvergne-Rhône-Alpes"));
    }

    // 6ème test - Carte de la France - Algorithme linéaire
    @Test
    public void TestF_assertPointIsInGuyane() throws IOException {
        String filename = franceMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-53.14, 4.14);
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "wikipedia", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("fr:Guyane"));
    }

    // 7ème test - Carte de la Belgique - Algorithme quadratique
    @Test
    public void TestG_assertPointIsInCampus() throws IOException {
        String filename = belgiumMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        // Point p = gb.point(-119.0, 56.0);
        Point p = gb.point(152183, 167679);
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "T_SEC_FR", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found =" + node.label + "end");
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("CAMPUS UNIVERSITAIRE"));
    }

    // 8ème test - Carte de la Belgique - Algorithme quadratique
    @Test
    public void TestH_assertPointIsNotInAnyPolygon() throws IOException {
        String filename = belgiumMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(215113.0, 217404.0);
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "T_SEC_FR", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node == null);
    }

    // 9ème test - Carte du monde - Algorithme quadratique
    @Test
    public void TestI_assertPointIsInKazakhstan() throws IOException {
        String filename = worldMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(73.0, 43.0);
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "NAME_FR", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Kazakhstan"));
    }

    // 10ème test - Carte du monde - Algorithme quadratique
    @Test
    public void TestJ_assertPointIsInCanada() throws IOException {
        String filename = worldMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-119.0, 56.0);
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "NAME_FR", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("Canada"));
    }

    // 11ème test - Carte de la France - Algorithme quadratique
    @Test
    public void TestK_assertPointIsInAuvergne() throws IOException {
        String filename = franceMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(4.44, 44.85);
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "wikipedia", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("fr:Auvergne-Rhône-Alpes"));

    }

    // 12ème test - Carte de la France - Algorithme quadratique
    @Test
    public void TestL_assertPointIsInGuyane() throws IOException {
        String filename = franceMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-53.14, 4.14);
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "wikipedia", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("fr:Guyane"));
    }

    // 13ème test - Carte du Japon - Algorithme linéaire
    @Test
    public void TestM_assertPointIsInGifu() throws IOException {
        String filename = japanMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(137.36, 36.12);
        RTreeLinear rtree = new RTreeLinear(loader.loadFile(), "ADM2_PCODE", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found =" + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("JP21042")); // Gifu
    }

    // 14ème test - Carte du Japon - Algorithme quadratique
    @Test
    public void TestN_assertPointIsInGifu() throws IOException {
        String filename = japanMap;
        FileLoader loader = new FileLoader(filename);
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(137.36, 36.12);
        RTreeQuadratic rtree = new RTreeQuadratic(loader.loadFile(), "ADM2_PCODE", N);
        long startTimeGlobal = System.currentTimeMillis();
        MBRNode node = rtree.search(rtree.root, p);
        long endTimeGlobal = System.currentTimeMillis();
        System.out.println("Total search function execution time: " + (endTimeGlobal - startTimeGlobal) + " ms");
        if (node != null)
            System.out.println(" node found =" + node.label);
        else
            System.out.println("Point not in any polygon");
        assertTrue(node.label.equals("JP21042")); // Gifu
    }
}
