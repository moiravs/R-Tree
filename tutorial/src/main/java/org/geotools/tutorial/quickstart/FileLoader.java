/*
 * Project for the course of INFO-F203 : R-Trees
 * Date: Thursday, March 30th 2023, 12:09:05 pm
 * Author: Moïra Vanderslagmolen & Andrius Ezerskis
 */
package org.geotools.tutorial.quickstart;

import java.io.File;

public class FileLoader {
    String filename;
    File file;

    /**
     * Constructeur de la classe FileLoader
     * 
     * @param filename
     */
    public FileLoader(String filename) {
        this.filename = filename;
    }

    /**
     * Charge le fichier en mémoire
     * 
     * @return le File chargé en mémoire
     */
    public File loadFile() {
        file = new File(filename);
        if (!file.exists()) {
            throw new RuntimeException("Shapefile does not exist.");
        }

        return file;
    }
}
