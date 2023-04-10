package org.geotools;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.tutorial.quickstart.MBRNode;
import org.geotools.tutorial.quickstart.RTree;
import org.junit.Test;
import org.locationtech.jts.geom.Point;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void shouldAnswerWithTrue() throws IOException {
        String filename = "../tutorial/maps/WB_countries_Admin0_10m.shp";
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-119.0, 56.0);
        RTree rtree = new RTree();
        rtree.createTree(filename, "NAME_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label + "end");
        else
            System.out.println("Point not in any polygon");
        // assertTrue(node.label == "Canada");
    }

    @Test
    public void pointDansLaPlaine() throws IOException {
        String filename = "../tutorial/maps/sh_statbel_statistical_sectors_31370_20220101.shp";
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        // Point p = gb.point(-119.0, 56.0);
        Point p = gb.point(152183, 167679);// Plaine
        RTree rtree = new RTree();
        rtree.createTree(filename, "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found =" + node.label + "end");
        else
            System.out.println("Point not in any polygon");
        // assertTrue(node.label == "CAMPUS UNIVERSITAIRE");
    }

    @Test
    public void pointJusteEnDehors() throws IOException {
        String filename = "../tutorial/maps/sh_statbel_statistical_sectors_31370_20220101.shp";
        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");
        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(215113.0, 217404.0);// Plaine
        RTree rtree = new RTree();
        rtree.createTree(filename, "T_SEC_FR");
        MBRNode node = rtree.search(rtree.root, p);
        if (node != null)
            System.out.println(" node found = " + node.label);
        else
            System.out.println("Point not in any polygon");
        // assertTrue(node == null);
    }

}
