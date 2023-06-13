package gitlet;

import java.io.Serializable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;



public class Commit implements Serializable {
    /** message.*/
    private String messageP;
    /** timestamp.*/
    private String timestampP;
    /** parent.*/
    private String parentP;
    /** parent2. */
    private String parent2P;
    /** myFiles treeMap.*/
    private TreeMap<String, String> myFiles;

    public Commit(String message, String parent, String parent2) {
        this.messageP = message;
        this.parentP = parent;
        this.parent2P = parent2;
        myFiles = new TreeMap<>();
        SimpleDateFormat timeFormat =
                new SimpleDateFormat("E MMM dd HH:mm:ss y Z");
        Date commitTime = new Date();
        this.timestampP = timeFormat.format(commitTime);
    }
    public String getID() {
        return Utils.sha1(this.messageP, this.parentP, this.timestampP);
    }

    public boolean fileExists(String filename, String id) {
        if (myFiles.containsKey(filename)
                && myFiles.get(filename).equals(id)) {
            return true;
        }
        return false;
    }

    public boolean fileExists2(String filename) {
        return myFiles.containsKey(filename);
    }

    public TreeMap<String, String> getMyFiles() {
        return this.myFiles;
    }

    public void addToMyFiles(TreeMap<String, String> map) {
        for (String name : map.keySet()) {
            myFiles.put(name, map.get(name));
        }
    }

    public void removeFromMyFiles(TreeMap<String, String> map) {
        for (String name : map.keySet()) {
            myFiles.remove(name);
        }
    }


    public String getMessage() {
        return this.messageP;
    }

    public String getTimestamp() {
        return this.timestampP;
    }

    public String getParent() {
        return this.parentP;
    }

    public String getParent2() {
        return this.parent2P;
    }

}
