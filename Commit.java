package gitlet;



import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /**The timestamp for this Commit */
    private Date timestamp;
    /** Makes the initial commit */
    private String parentid;
    private String parentid2;
    private TreeMap<String, String> map;
    private String branch;
    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.map = new TreeMap<>();
        this.parentid = null;
        this.branch = "master";
    }
    public Commit(String mess, String parentid) {
        this.message = mess;
        this.timestamp = new Date();
        this.map = new TreeMap<>();
        this.parentid = parentid;
        this.parentid2 = null;
        this.branch = null;
    }
    public String getformatedtime() {
        String format = "E LLL d kk:mm:ss y Z";
        DateFormat date = new SimpleDateFormat(format);
        return date.format(timestamp);
    }
    public String getMessage() {
        return this.message;
    }
    public Date gettimestamp() {
        return this.timestamp;
    }
    public String getParentid() {
        return this.parentid;
    }
    public String getParentid2() {
        return this.parentid2;
    }
    public TreeMap<String, String> getmap() {
        return this.map;
    }
    public String getBranch() {
        return this.branch;
    }
    public void setparentid1(String given) {
        this.parentid = given;
    }
    public void setbrancb(String given) {
        this.branch = given;
    }
    public void setparentid2(String given) {
        this.parentid2 = given;
    }

    public void setMap(TreeMap<String, String> map) {
        this.map = map;
    }
}
