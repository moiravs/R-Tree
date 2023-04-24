package org;
import java.io.File;

public class FileLoader {
    String filename;
    File file;

    FileLoader(String filename)
    {
        this.filename = filename;
    }

    public loadFile()
    {
        file = new File(filename);
        if (!file.exists())
        {
            
        }
    }
}
