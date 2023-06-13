package gitlet;


/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Amy Stanley
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     * java gitlet.Main add hello.txt
     */

    public static void main(String... args) {
        CommandHandler handle = new CommandHandler();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        try {
            switch (args[0]) {
            case "init":
                handle.init();
                break;
            case "add":
                handle.add(args[1]);
                break;
            case "commit":
                handle.commit(args[1]);
                break;
            case "log":
                handle.log();
                break;
            case "checkout":
                handle.checkout(args);
                break;
            case "rm":
                handle.rm(args[1]);
                break;
            case "global-log":
                handle.globalLog();
                break;
            case "find":
                handle.find(args[1]);
                break;
            case "status":
                handle.status();
                break;
            case "branch":
                handle.branch(args[1]);
                break;
            case "rm-branch":
                handle.rmBranch(args[1]);
                break;
            case "reset":
                handle.reset(args[1]);
                break;
            case "merge":
                handle.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    static boolean operandCheck(int length, String... args) {
        if (args.length == length) {
            return true;
        }
        System.out.println("Invalid Input");
        return false;
    }
}
