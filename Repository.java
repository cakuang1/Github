package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.Utils.*;
import java.util.Arrays;

/**
 * Represents a gitlet repository.
 * does at a high level.
 *
 * @author
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    /**
     * COMMIT TREE
     */
    private static File staging = Utils.join(GITLET_DIR, "stgingdir");
    private static File removal = Utils.join(GITLET_DIR, "removaldir");
    private static File commits = Utils.join(GITLET_DIR, "commits");
    private static File blobs = Utils.join(GITLET_DIR, "blobs");
    private static File head = Utils.join(GITLET_DIR, "head");
    private static File branches = Utils.join(GITLET_DIR, "branches");
    private static File current = Utils.join(GITLET_DIR, "currentbranch");

    public static void initCommand() {

        GITLET_DIR.mkdir();
        staging.mkdir();
        removal.mkdir();
        commits.mkdir();
        blobs.mkdir();
        branches.mkdir();
        Commit initialCommit = new Commit(); /*creates initial commit*/
        String initialhash = getcommithash(initialCommit); /*the commit sha1*/
        File ic = Utils.join(commits, initialhash); /*creates initial commit*/
        File initialbranch = Utils.join(branches, "master");
        Branchess initial = new Branchess();
        initial.getArraycommits().add(initialhash);
        writeObject(initialbranch, initial);
        writeObject(ic, initialCommit);
        writeObject(head, initialhash);
        writeContents(current, "master");
    }

    public static void addCommand(String f) {
        File[] listoffiles = removal.listFiles();
        for (File path : listoffiles) {
            if (path.getName().equals(f)) {
                path.delete();
            }
        }
        Commit heads = getheadcommit();
        File givenfile = Utils.join(CWD, f);
        File referenced = Utils.join(staging, f);
        byte[] referencedbyte = readContents(givenfile);
        String referencedbytesha = sha1(referencedbyte);
        if (heads.getmap().containsKey(f) && heads.getmap().get(f).equals(referencedbytesha)) {
            if (referenced.exists()) {
                referenced.delete();
                return;
            }
            return;
        }
        writeContents(referenced, referencedbyte);
    }

    public static void commitcommand(String message) {
        Commit newcommit = new Commit(message, getheadsha());
        newcommit.setbrancb(readContentsAsString(current));
        Set<String> headkeyset = getheadcommit().getmap().keySet();
        File[] listoffiles = staging.listFiles();
        for (File path : listoffiles) {
            byte[] bytearray = readContents(path);
            String file = sha1(bytearray);
            newcommit.getmap().put(path.getName(), file);
            File newfile = Utils.join(blobs, file);
            writeContents(newfile, bytearray);
            path.delete();
        }
        for (String headcommitkeys : headkeyset) {
            if (!newcommit.getmap().keySet().contains(headcommitkeys)
                && !isinsidedirectory(removal, headcommitkeys)) { newcommit
                .getmap().put(headcommitkeys, getheadcommit().getmap().get(headcommitkeys));
            }
        }
        File[] remv = removal.listFiles();
        for (File path : remv) {
            path.delete();
        }
        newcommit.setparentid1(getheadsha());
        File commit = Utils.join(commits, getcommithash(newcommit));
        writeObject(commit, newcommit);
        writeObject(head, getcommithash(newcommit));
        String curr = readContentsAsString(current);
        Branchess currentbranch = readObject(Utils.join(branches, curr), Branchess.class);
        currentbranch.getArraycommits().add(getcommithash(newcommit));
        writeObject(Utils.join(branches, curr), currentbranch);
    }

    public static void removalcommand(String file) {
        File[] listoffiles = staging.listFiles();
        for (File name : listoffiles) {
            if (name.getName().equals(file)) {
                name.delete();
            }
        }
        if (getheadcommit().getmap().containsKey(file)) {
            File newfile = Utils.join(removal, file);
            File deletecwd = Utils.join(CWD, file);
            writeObject(newfile, deletecwd);
            deletecwd.delete();
        }
    }

    public static void logcommand() {
        Commit currentcommit = getheadcommit();
        while (currentcommit.getParentid() != null) {
            System.out.println("===");
            System.out.println("commit " + getcommithash(currentcommit));
            if (currentcommit.getParentid2() != null) {
                System.out.println("Merge: " + currentcommit.getParentid().substring(0, 7)
                        + " " + currentcommit.getParentid2().substring(0, 7));
            }
            System.out.println("Date: " + currentcommit.getformatedtime());
            System.out.println(currentcommit.getMessage());
            System.out.println();
            File newcommit = Utils.join(commits, currentcommit.getParentid());
            currentcommit = readObject(newcommit, Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + getcommithash(currentcommit));
        System.out.println("Date: " + currentcommit.getformatedtime());
        System.out.println(currentcommit.getMessage());
        System.out.println();
    }

    public static void globallog() {
        File[] commitlist = commits.listFiles();
        for (File commit : commitlist) {
            Commit currentcommit = readObject(commit, Commit.class);
            System.out.println("===");
            System.out.println("commit " + getcommithash(currentcommit));
            System.out.println("Date: " + currentcommit.getformatedtime());
            System.out.println(currentcommit.getMessage());
            System.out.println();
        }
    }

    public static void findcommand(String mess) {
        Boolean flag = false;
        File[] commitlist = commits.listFiles();
        for (File commit : commitlist) {
            Commit currentcommit = readObject(commit, Commit.class);
            if (currentcommit.getMessage().equals(mess)) {
                flag = true;
                System.out.println(commit.getName());
            }
        }
        if (!flag) {
            System.out.println("Found no commit with that message.");
        }

    }

    public static void statuscommand() {
        System.out.println("=== Branches ===");
        String[] listofbranches = branches.list();
        Arrays.sort(listofbranches);
        for (String branch : listofbranches) {
            if (readContentsAsString(current).equals(branch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        String[] stagings = staging.list();
        for (String staged : stagings) {
            System.out.println(staged);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        String[] rem = removal.list();
        for (String staged : rem) {
            System.out.println(staged);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        Set<String> set = getheadcommit().getmap().keySet();
        for (String headommitkeys : set) {
            if (!isinsidedirectory(CWD, headommitkeys)) {
                if (!isinsidedirectory(staging, headommitkeys)
                        && !isinsidedirectory(removal, headommitkeys)) {
                    System.out.println(headommitkeys + " (deleted)");
                }
            } else if (!readContentsAsString(Utils.join(blobs,
                    getheadcommit().getmap().get(headommitkeys))).
                    equals(readContentsAsString(Utils.join(CWD, headommitkeys)))) {
                if (!isinsidedirectory(staging, headommitkeys)
                        && !isinsidedirectory(removal, headommitkeys)) {
                    System.out.println(headommitkeys + " (modified)");
                }
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        List<String> cwd = plainFilenamesIn(CWD);
        for (String file : cwd) {
            if (!getheadcommit().getmap().containsKey(file)) {
                    System.out.println(file);
            }
        }
        System.out.println();
    }
    public static void checkoutv1(String name) {
        checkoutv2(getheadsha(), name);
    }
    public static void checkoutv3(String branchname) {
        if (!isinsidedirectory(branches, branchname)) {
            System.out.println("No such branch exists.");
            return;
        }
        List<String> currentcws = Utils.plainFilenamesIn(CWD);
        File branch = Utils.join(branches, branchname);
        Branchess currentbranch = readObject(branch, Branchess.class);
        File commit = Utils.join(commits, currentbranch.getArraycommits().getLast());
        Commit branchedcommit = readObject(commit, Commit.class);

        for (String files:currentcws) {
            if (!getheadcommit().getmap().containsKey(files)
                    && branchedcommit.getmap().containsKey(files)) {
                File cwdFile = Utils.join(CWD, files);
                File blob = Utils.join(blobs, branchedcommit.getmap().get(files));
                if (!readContentsAsString(cwdFile).equals(readContentsAsString(blob))) {
                    String n = "There is an untracked file in the way; "
                            + "delete it, or add and commit it first.";
                    System.out.println(n);
                    return;
                }
            }
        }
        writeContents(current, branchname);
        Commit currenthead = getheadcommit();
        TreeMap<String, String> treeMap = currenthead.getmap();
        Set<String> keyset = treeMap.keySet();
        for (File file: CWD.listFiles()) {
            file.delete(); }
        for (File file: staging.listFiles()) {
            file.delete();
        }
        for (File file: removal.listFiles()) {
            file.delete();
        }

        for (String s:keyset) {
            String id = treeMap.get(s);
            File newfile = Utils.join(CWD, s);
            File blobss = Utils.join(blobs, id);
            String bytese = readContentsAsString(blobss);
            writeContents(newfile, bytese);
        }
    }
    public static void checkoutv21(String commitid, String filename) {
        int abbcommitid = commitid.length();
        File[] listofcommits = commits.listFiles();
        for (File path:listofcommits) {
            if (path.getName().substring(0, abbcommitid).equals(commitid)) {
                Commit givencommit = readObject(path, Commit.class);
                String fileid = givencommit.getmap().get(filename);
                File commitedfile = Utils.join(blobs, fileid);
                String byteoffile = readContentsAsString(commitedfile);
                File cwdfile = Utils.join(CWD, filename);
                writeContents(cwdfile, byteoffile);
                break;
            }

        }

    }


    public static void checkoutv2(String commitid, String filename) {
        File givenid = Utils.join(commits, commitid);
        if (!isinsidedirectory(commits, commitid)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit givencommit = readObject(givenid, Commit.class);
        if (!givencommit.getmap().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String fileid = givencommit.getmap().get(filename);
        File commitedfile = Utils.join(blobs, fileid);
        String byteoffile = readContentsAsString(commitedfile);
        File cwdfile = Utils.join(CWD, filename);
        writeContents(cwdfile, byteoffile);
    }
    public static void reset(String commit) {
        if (!isinsidedirectory(commits, commit)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File object = Utils.join(commits, commit);
        Commit refere = readObject(object, Commit.class);
        String commitbranch = refere.getBranch();
        File branch = Utils.join(branches, commitbranch);
        Branchess ref = readObject(branch, Branchess.class);
        if (ref.getArraycommits().contains(commit)) {
            for (int i = 0; i < ref.getArraycommits().size(); i++) {
                if (commit.equals(ref.getArraycommits().getLast())) {
                    break;
                }
                ref.getArraycommits().removeLast();
            }
        } else {
            LinkedList<String> newarraylist = new LinkedList<>();
            newarraylist.addFirst(commit);
            while (!refere.getParentid().equals(ref.getArraycommits().getLast())) {
                newarraylist.addFirst(refere.getParentid());
                File nextobject = Utils.join(commits, refere.getParentid());
                refere = readObject(nextobject, Commit.class);
            }
            while (newarraylist.size() != 0) {
                ref.getArraycommits().addLast(newarraylist.removeFirst());
            }
        }
        writeObject(branch, ref);
        checkoutv3(commitbranch);
    }
    public static void branch(String newname) {
        if (isinsidedirectory(branches, newname)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Branchess newbranch = new Branchess(getheadsha());
        newbranch.getArraycommits().add(getheadsha());
        File newfile = Utils.join(branches, newname);
        writeObject(newfile, newbranch);

    }
    public  static void rmbranch(String branchname) {
        if (readContentsAsString(current).equals(branchname)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else if (!isinsidedirectory(branches, branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File referenced = Utils.join(branches, branchname);
        referenced.delete();

    }
    public static void merge(String branchname) {
        /* checks for errors*/
        Boolean yesorno = mergeerrorcheck(branchname);
        if (yesorno) {
            return;
        }
        TreeMap<String, String> split =
                getsplitpointv2(readContentsAsString(current), branchname).getmap();
        TreeMap<String, String> currentc = getheadcommit().getmap();
        TreeMap<String, String> given = getheadcommitofbranch(branchname).getmap();
        Boolean mergeconflic = false;
        Set<String> splitset =
                getsplitpointv2(readContentsAsString(current), branchname).getmap().keySet();
        Set<String> currrentset = getheadcommit().getmap().keySet();
        Set<String> givenset = getheadcommitofbranch(branchname).getmap().keySet();
        TreeMap<String, String> newhash = new TreeMap<>();
        clearstagingremarea();
        for (String cloth : splitset) {
            if (currentc.containsKey(cloth) && given.containsKey(cloth)) {
                if (currentc.get(cloth).equals(split.get(cloth))
                        && !given.get(cloth).equals(split.get(cloth))) {
                    newhash.put(cloth, given.get(cloth)); /* case 1*/
                } else if (!currentc.get(cloth).equals(split.get(cloth))
                        && given.get(cloth).equals(split.get(cloth))) {
                    newhash.put(cloth, currentc.get(cloth)); /* case 2*/
                } else if (currentc.get(cloth).equals(given.get(cloth))) {
                    newhash.put(cloth, currentc.get(cloth)); /* case 3*/
                } else if (!currentc.get(cloth).equals(given.get(cloth))) {
                    mergeconflic = true;
                    mergeconflict1(currentc, given, cloth, newhash);
                }
            }
            if (currentc.containsKey(cloth) && !given.containsKey(cloth)) {
                if (currentc.get(cloth).equals(split.get(cloth))) { /* case 6*/
                    Utils.join(CWD, cloth).delete();
                } else if (!currentc.get(cloth).equals(split.get(cloth))) {
                    mergeconflic = true;
                    mergeconflict2(currentc, cloth, newhash);
                }
            }
            if (!currentc.containsKey(cloth) && given.containsKey(cloth)) {
                if (given.get(cloth).equals(split.get(cloth))) { /* case 7*/
                    continue;
                } else if (!given.get(cloth).equals(split.get(cloth))) {
                    mergeconflic = true;
                    mergeconflict2(given, cloth, newhash);
                }
            }
            if (!currentc.containsKey(cloth) && !given.containsKey(cloth)) {
                continue;
            }
        }
        for (String currentsplit : currrentset) {
            if (!splitset.contains(currentsplit) && !given.containsKey(currentsplit)) {
                newhash.put(currentsplit, currentc.get(currentsplit)); /*case 3*/
            } else if (!splitset.contains(currentsplit) && given.containsKey(currentsplit)) {
                mergeconflic = true;
                mergeconflict1(currentc, given, currentsplit, newhash);
            }
        }
        for (String givenkeys : givenset) {
            if (!splitset.contains(givenkeys) && !currentc.containsKey(givenkeys)) {
                newhash.put(givenkeys, given.get(givenkeys)); /* case 5*/
                writeContents(Utils.join(staging, givenkeys), readContentsAsString
                                (Utils.join(blobs, given.get(givenkeys))));
            } else if (!splitset.contains(givenkeys) && currentc.containsKey(givenkeys)) {
                mergeconflic = true;
                mergeconflict1(currentc, given, givenkeys, newhash);
            }
        }
        if (mergeconflic) {
            System.out.println("Encountered a merge conflict.");
        }
        iterateset(newhash.keySet(), newhash);
        lastparthelper(branchname, newhash);
        clearstagingrema();
    }
    public static void lastparthelper(String givenbranch, TreeMap<String, String> hashset) {
        Commit newcommitmerge = new Commit("Merged " + givenbranch
                + " into " + readContentsAsString(current) + ".", getheadsha());
        newcommitmerge.setMap(hashset);
        newcommitmerge.setparentid2(getheadcommitofbranchsha(givenbranch));
        writeObject(Utils.join(commits, sha1(serialize(newcommitmerge))), newcommitmerge);
        Branchess currentbranche = readObject(Utils.join
                (branches, readContentsAsString(current)), Branchess.class);
        currentbranche.getArraycommits().addLast(sha1(serialize(newcommitmerge)));
        writeObject(Utils.join(branches, readContentsAsString(current)), currentbranche);
    }
    public static void iterateset(Set<String> blah, TreeMap<String, String> sdfksl) {
        for (String name:blah) {
            String contents = readContentsAsString(Utils.join(blobs, sdfksl.get(name)));
            File cwd = Utils.join(CWD, name);
            writeContents(cwd, contents);
        }
    }
    public static void mergeconflict1(TreeMap<String, String> sha, TreeMap<String, String> sha2,
                                      String key, TreeMap<String, String> hash) {
        File blobcurrent = Utils.join(blobs, sha.get(key)); /* case 8*/
        File bloblgiven = Utils.join(blobs, sha2.get(key));
        String uno = readContentsAsString(blobcurrent);
        String dos = readContentsAsString(bloblgiven);
        String contents = "<<<<<<< HEAD\n" + uno + "=======\n" + dos + ">>>>>>>\n";
        File newblob = Utils.join(blobs, sha1(serialize(contents)));
        writeContents(newblob, contents);
        hash.put(key, sha1(serialize(contents)));
    }
    public static void mergeconflict2(TreeMap<String, String> sha,
                                      String key, TreeMap<String, String> hash) {
        File blobcurrent = Utils.join(blobs, sha.get(key));
        String uno = readContentsAsString(blobcurrent);
        String contents = "<<<<<<< HEAD\n" + uno + "=======\n>>>>>>>\n";
        File newblob = Utils.join(blobs, sha1(serialize(contents)));
        writeContents(newblob, contents);
        hash.put(key, sha1(serialize(contents)));
    }
    public static void clearstagingrema() {
        for (File file : staging.listFiles()) {
            file.delete();
        }
        for (File file : removal.listFiles()) {
            file.delete();
        }
    }
    public static void clearstagingremarea() {
        for (File file : CWD.listFiles()) {
            file.delete();
        }
        for (File file : staging.listFiles()) {
            file.delete();
        }
        for (File file : removal.listFiles()) {
            file.delete();
        }
    }

    public static String getheadsha() {
        String currentheadstring = readContentsAsString(current);
        File branch = Utils.join(branches, currentheadstring);
        Branchess heads = readObject(branch, Branchess.class);
        return heads.getArraycommits().getLast();
    }
    public static Commit getheadcommit() {
        File heads = Utils.join(commits, getheadsha());
        return readObject(heads, Commit.class);
    }
    public static Boolean isinsidedirectory(File x, String name) {
        String[] listoffiles = x.list();
        for (String path : listoffiles) {
            if (path.equals(name)) {
                return true;
            }
        }
        return false;
    }
    public static String getcommithash(Commit given) {
        return sha1(serialize(given));
    }
    public static Commit getsplitpointv2(String currentheadname, String givenbranch) {
        Commit currenthead = getheadcommitofbranch(currentheadname);
        Commit branchhead = getheadcommitofbranch(givenbranch);
        ArrayList<String> currentheadcommits = new ArrayList<>();
        ArrayList<String> currentheadcommitsp2 = new ArrayList<>();
        if (currenthead.getParentid2() != null) {
            currentheadcommitsp2.add(currenthead.getParentid2());
            Commit parent2commmit = getcommitfromshaid(currenthead.getParentid2());
            while (parent2commmit.getParentid() != null) {
                currentheadcommits.add(parent2commmit.getParentid());
                parent2commmit = getcommitfromshaid(parent2commmit.getParentid());
            }
        }
        while (currenthead.getParentid() != null) {
            currentheadcommits.add(currenthead.getParentid());
            currenthead = getcommitfromshaid(currenthead.getParentid());
        }
        if (currentheadcommits.contains(getheadcommitofbranchsha(givenbranch))
                || currentheadcommitsp2.contains(getheadcommitofbranchsha(givenbranch))) {
            return getcommitfromshaid(getheadcommitofbranchsha(givenbranch));
        }

        while (branchhead.getParentid() != null) {
            if (currentheadcommits.contains(branchhead.getParentid())
                    || currentheadcommitsp2.contains(branchhead.getParentid())) {
                return getcommitfromshaid(branchhead.getParentid());
            }
            branchhead = getcommitfromshaid(branchhead.getParentid());
        }
        return branchhead;
    }
    public static Commit getcommitfromshaid(String shaid) {
        File fileofcommit = Utils.join(commits, shaid);
        return readObject(fileofcommit, Commit.class);
    }
    public static Commit getheadcommitofbranch(String branchname) {
        File branch = Utils.join(branches, branchname);
        Branchess heads = readObject(branch, Branchess.class);
        String kslfndskl = heads.getArraycommits().getLast();
        File commitobject = Utils.join(commits, kslfndskl);
        return readObject(commitobject, Commit.class);
    }
    public static String getheadcommitofbranchsha(String branchname) {
        File branch = Utils.join(branches, branchname);
        Branchess heads = readObject(branch, Branchess.class);
        return heads.getArraycommits().getLast();
    }
    public static File getstagingdir() {
        return staging;
    }
    public static File getRemovaldir() {
        return removal;
    }
    public static File getcurrentfile() {
        return current;
    }
    public static Boolean mergeerrorcheck(String branchname) {
        if (!isinsidedirectory(branches, branchname)) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        if (staging.list().length != 0 || removal.list().length != 0) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (branchname.equals(readContentsAsString(current))) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        String[] listoffilesincwd = CWD.list();
        for (String list : listoffilesincwd) {
            if (!getheadcommit().getmap().containsKey(list)) {
                if (getheadcommitofbranch(branchname).getmap().containsKey(list)) {
                    if (!getheadcommitofbranch(branchname).getmap().get(list).equals
                            (sha1(serialize(Utils.join(CWD, list))))) {
                        System.out.println("There is an untracked file in the way; "
                                        + "delete it, or add and commit it first.");
                        return true;
                    }
                }
            }
        }
        Commit currenthead = getheadcommit();
        Commit branchhead = getheadcommitofbranch(branchname);
        ArrayList<String> currentheadcommits = new ArrayList<>();
        ArrayList<String> branchcommits = new ArrayList<>();
        while (currenthead.getParentid() != null) {
            currentheadcommits.add(currenthead.getParentid());
            currenthead = getcommitfromshaid(currenthead.getParentid());
        }
        while (branchhead.getParentid() != null) {
            branchcommits.add(branchhead.getParentid());
            branchhead = getcommitfromshaid(branchhead.getParentid());
        }
        if (currentheadcommits.contains(getheadcommitofbranchsha(branchname))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return true;
        }
        if (branchcommits.contains(getheadsha())) {
            System.out.println("Current branch fast-forwarded.");
            checkoutv3(branchname);
            return true;
        }


        return false;
    }
}
