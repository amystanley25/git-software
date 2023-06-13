package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    /** contents of file. */
    private String contents;
    public Blob(File file) {
        contents = readContentsAsString(file);
    }

}
