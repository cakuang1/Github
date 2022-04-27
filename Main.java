package gitlet;

import java.io.File;
import java.util.ArrayList;

import static gitlet.Utils.readContentsAsString;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (!args[0].equals("init") && !Repository.isinsidedirectory(Repository.CWD,".gitlet")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);

        }
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (Repository.isinsidedirectory(Repository.CWD,".gitlet")){
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    System.exit(0);
                }
                Repository.initCommand();
                System.exit(0);
            case "merge":
                Repository.merge(args[1]);
                System.exit(0);
            case "add":
                if (!Repository.isinsidedirectory(Repository.CWD,".gitlet")){
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (!Repository.isinsidedirectory(Repository.CWD,args[1])) {
                    System.out.println("File does not exist.");
                    System.exit(0);
                }
                Repository.addCommand(args[1]);
                System.exit(0);
            case "commit":
                if (args[1].length()==0){
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                if (Repository.getstagingdir().list().length==0 && Repository.getRemovaldir().list().length==0){
                    System.out.println("No changes added to the commit.");
                    System.exit(0);
                }

                Repository.commitcommand(args[1]);
                System.exit(0);
            case "log":
                Repository.logcommand();
                System.exit(0);
            case "rm":
                if (!Repository.getheadcommit().getmap().containsKey(args[1]) && !Repository.isinsidedirectory(Repository.getstagingdir(),args[1])){
                    System.out.print("No reason to remove the file.");
                    System.exit(0);}
                Repository.removalcommand(args[1]);
                System.exit(0);
            case "status":
                Repository.statuscommand();
                System.exit(0);
            case "global-log":
                Repository.globallog();
                System.exit(0);
            case "find":
                Repository.findcommand(args[1]);
                System.exit(0);
            case "branch":
                Repository.branch(args[1]);
                System.exit(0);
            case "reset":
                Repository.reset(args[1]);
                System.exit(0);
            case "rm-branch":
                Repository.rmbranch(args[1]);
                System.exit(0);
            case "checkout":
                if (args.length==3) {
                    if (!args[1].equals("--")){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkoutv1(args[2]);
                    System.exit(0);

                }
                else if (args.length==2){
                    if (args[1].equals(readContentsAsString(Repository.getcurrentfile()))) {
                        System.out.println("No need to checkout the current branch.");
                        System.exit(0);}
                    Repository.checkoutv3(args[1]);
                    System.exit(0);
                }
                else{
                    if (!args[2].equals("--")){
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    if (args[1].length()<40){
                        Repository.checkoutv21(args[1],args[3]);
                        System.exit(0);
                    }
                    Repository.checkoutv2(args[1],args[3]);
                    System.exit(0);
                }
            }
            System.out.println("No command with that name exists.");

        }

    }

