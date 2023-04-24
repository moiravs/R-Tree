package org.geotools.tutorial.quickstart;
import java.io.File;

public class FileLoader {
    String filename;
    File file;

    public FileLoader(String filename)
    {
        this.filename = filename;
    }

    public File loadFile()
    {
        file = new File(filename);
        if (!file.exists())
        {
            throw new RuntimeException("Shapefile does not exist.");
        }
        
        return file;
    }
}
