package gitlet;


import java.io.Serializable;
import java.util.LinkedList;

public class Branchess implements Serializable {
    private String branchedfrom;
    private LinkedList<String> arraycommits;
    public Branchess() {
        this.arraycommits = new LinkedList<>();
    }
    public Branchess(String branched) {
        this.branchedfrom = branched;
        this.arraycommits = new LinkedList();
    }
    public LinkedList<String>  getArraycommits() {
        return this.arraycommits;
    }
    public String getBranchedfrom() {
        return this.branchedfrom;
    }
}
