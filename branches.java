package gitlet;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

public class branches implements Serializable {
    public String branchedfrom;
    public LinkedList<String> arraycommits;
    public branches() {
        this.arraycommits = new LinkedList<>();
    }
    public branches(String branched){
        this.branchedfrom = branched;
        this.arraycommits = new LinkedList();
    }
}
