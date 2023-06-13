package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

import static gitlet.Utils.error;
import static gitlet.Utils.*;


public class CommandHandler {

    /**
     * Pathway to CWD.
     */
    static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * Pathway to GITLET_FOLDER.
     */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");
    /**
     * Pathway to COMMITS_FOLDER.
     */
    static final File COMMITS_FOLDER = Utils.join(GITLET_FOLDER, "commits");
    /**
     * Pathway to BLOBS_FOLDER.
     */
    static final File BLOBS_FOLDER = Utils.join(GITLET_FOLDER, "blobs");
    /**
     * Pathway to BRANCH_FILE.
     */
    static final File BRANCH_FILE = Utils.join(GITLET_FOLDER, "BRANCH_FILE");
    /**
     * Pathway to HEAD_FILE.
     */
    static final File HEAD_FILE = Utils.join(GITLET_FOLDER, "HEAD_FILE");
    /**
     * Pathway to STAG_ADD.
     */
    static final File STAG_ADD = Utils.join(GITLET_FOLDER, "STAG_ADD");
    /**
     * Pathway to STAG_REM.
     */
    static final File STAG_REM = Utils.join(GITLET_FOLDER, "STAG_REM");

    public void init() {
        if (GITLET_FOLDER.exists()) {
            String msg1 = "A Gitlet version-control system already ";
            String msg2 = "exists in the current directory.";
            throw error(msg1 + msg2);
        }
        GITLET_FOLDER.mkdir();
        COMMITS_FOLDER.mkdir();
        BLOBS_FOLDER.mkdir();
        try {
            STAG_ADD.createNewFile();
            STAG_REM.createNewFile();
            BRANCH_FILE.createNewFile();
            HEAD_FILE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TreeMap<String, String> addStage = new TreeMap<>();
        TreeMap<String, String> removeStage = new TreeMap<>();
        writeObject(STAG_ADD, addStage);
        writeObject(STAG_REM, removeStage);
        Commit initial = new Commit("initial commit", "", "");
        File initialCommit = Utils.join(COMMITS_FOLDER, initial.getID());
        try {
            initialCommit.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(initialCommit, initial);
        String head = "master";
        writeObject(HEAD_FILE, head);
        TreeMap<String, String> branches = new TreeMap<>();
        branches.put("master", initial.getID());
        writeObject(BRANCH_FILE, branches);
    }

    /** commit.
     * @param message the inputted messsage */


    @SuppressWarnings("unchecked")
    public void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message");
            System.exit(0);
        }
        TreeMap<String, String> addStage = readObject(STAG_ADD, TreeMap.class);
        TreeMap<String, String>
                removeStage = readObject(STAG_REM, TreeMap.class);
        if (addStage.isEmpty() && removeStage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        String head = readObject(HEAD_FILE, String.class);
        TreeMap<String, String>
                branches = readObject(BRANCH_FILE, TreeMap.class);
        String recentCommitID = (String) branches.get(head);
        Commit recentCommit = readObject(Utils.join(COMMITS_FOLDER,
                recentCommitID), Commit.class);

        Commit newCommit = new Commit(message, recentCommitID, "");
        newCommit.addToMyFiles(recentCommit.getMyFiles());
        newCommit.addToMyFiles(addStage);
        newCommit.removeFromMyFiles(removeStage);
        String newID = newCommit.getID();
        writeObject(Utils.join(COMMITS_FOLDER, newID), newCommit);

        addStage.clear();
        removeStage.clear();
        writeObject(STAG_ADD, addStage);
        writeObject(STAG_REM, removeStage);

        branches.put(head, newID);
        writeObject(BRANCH_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    public void add(String name) {
        File file = Utils.join(CWD, name);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String head = readObject(HEAD_FILE, String.class);
        TreeMap<String, String>
                branches = readObject(BRANCH_FILE, TreeMap.class);
        String recentCommitID = (String) branches.get(head);
        Commit recentCommit = readObject(Utils.join(COMMITS_FOLDER,
                recentCommitID), Commit.class);

        String currContents = Utils.readContentsAsString(file);
        String currId = Utils.sha1(currContents);
        TreeMap<String, String>
                addStage = readObject(STAG_ADD, TreeMap.class);
        TreeMap<String, String>
                removeStage = readObject(STAG_REM, TreeMap.class);
        if (recentCommit.getMyFiles().containsKey(name)
                && recentCommit.fileExists(name, currId)) {
            addStage.remove(name);
            removeStage.remove(name);
        } else {
            addStage.put(name, currId);
            removeStage.remove(name);
            File blobFile = Utils.join(BLOBS_FOLDER, currId);
            Utils.writeContents(blobFile, currContents);
        }
        Utils.writeObject(STAG_ADD, addStage);
        Utils.writeObject(STAG_REM, removeStage);
    }

    @SuppressWarnings("unchecked")
    public void log() {
        String head = readObject(HEAD_FILE, String.class);
        TreeMap branches = readObject(BRANCH_FILE, TreeMap.class);
        String commitID = (String) branches.get(head);
        while (!commitID.equals("")) {
            Commit commit = readObject(Utils.join(COMMITS_FOLDER,
                    commitID), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitID);
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
            commitID = commit.getParent();
        }
    }

    @SuppressWarnings("unchecked")
    public void checkoutHelper3(String[] args) {
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            String fileName = args[2];
            String head = readObject(HEAD_FILE, String.class);
            TreeMap<String, String>
                    branches = readObject(BRANCH_FILE, TreeMap.class);
            String recentCommitID = branches.get(head);
            Commit recentCommit = readObject(Utils.join(COMMITS_FOLDER,
                    recentCommitID), Commit.class);
            TreeMap<String, String> myFiles = recentCommit.getMyFiles();
            if (!myFiles.containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
            String blobID = myFiles.get(fileName);
            File checkoutBlob = new File(BLOBS_FOLDER, blobID);
            String copyContents = readContentsAsString(checkoutBlob);
            File newFile = new File(CWD, fileName);
            Utils.writeContents(newFile, copyContents);
        }
    }

    @SuppressWarnings("unchecked")
    public void checkoutHelper4(String[] args) {
        if (args.length == 4) {
            final String commitIDarg = args[1];
            String fileName = args[3];
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }

            String commitID = plainFilenamesIn(COMMITS_FOLDER)
                    .stream()
                    .filter(c -> c.substring(0, commitIDarg
                            .length()).equals(commitIDarg)).findFirst()
                    .orElse("");
            if (commitID.equals("")) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }

            File commitFile = Utils.join(COMMITS_FOLDER, commitID);
            if (!commitFile.exists()) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            Commit checkoutCommit = readObject(commitFile, Commit.class);
            TreeMap<String, String> myFiles = checkoutCommit.getMyFiles();
            if (!myFiles.containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }

            String blobID = (String) myFiles.get(fileName);

            File checkoutBlob = new File(BLOBS_FOLDER, blobID);
            String copyContents = readContentsAsString(checkoutBlob);
            File newFile = new File(CWD, fileName);
            Utils.writeContents(newFile, copyContents);
        }
    }

    @SuppressWarnings("unchecked")
    public void checkoutHelper2(String[] args) {
        if (args.length == 2) {
            String branchName = args[1];
            TreeMap<String, String>
                    branches = readObject(BRANCH_FILE, TreeMap.class);
            if (!branches.containsKey(branchName)) {
                System.out.println("No such branch exists.");
                System.exit(0);
            }
            String head = readObject(HEAD_FILE, String.class);
            String currCommitSha = branches.get(head);
            Commit recentCommit = readObject(
                    Utils.join(COMMITS_FOLDER, currCommitSha), Commit.class);

            if (head.equals(branchName)) {
                System.out.println("No need to check out the current branch.");
                System.exit(0);
            }

            Commit checkoutCommit = readObject(Utils.join(COMMITS_FOLDER,
                    branches.get(branchName)), Commit.class);
            for (String file : plainFilenamesIn(CWD)) {
                String contentID = sha1(Utils
                        .readContentsAsString(Utils.join(CWD, file)));
                if (!recentCommit.fileExists(file, contentID)
                        && checkoutCommit.getMyFiles().containsKey(file)) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it, "
                            + "or add and commit it first.");
                    System.exit(0);
                }
            }

            for (String file : plainFilenamesIn(CWD)) {
                String contentID = sha1(Utils.readContentsAsString(Utils
                        .join(CWD, file)));
                if (recentCommit.fileExists(file, contentID)
                        && !checkoutCommit.getMyFiles().containsKey(file)) {
                    Utils.join(CWD, file).delete();
                }
            }
            checkoutStageHelper(args);
            TreeMap<String, String> myFiles = checkoutCommit.getMyFiles();
            ArrayList<String> myFilesList = new ArrayList<>(myFiles.keySet());
            for (String fileName : myFilesList) {
                File blobFile = new File(BLOBS_FOLDER, myFiles.get(fileName));
                String copyContents = readContentsAsString(blobFile);
                File newFile = new File(CWD, fileName);
                Utils.writeContents(newFile, copyContents);
            }

            head = branchName;
            writeObject(HEAD_FILE, head);
        }
    }

    @SuppressWarnings("unchecked")
    public void checkoutStageHelper(String[] args) {
        TreeMap<String, String>
                addStage = readObject(STAG_ADD, TreeMap.class);
        TreeMap<String, String>
                removeStage = readObject(STAG_REM, TreeMap.class);

        String branchName = args[1];
        TreeMap<String, String>
                branches = readObject(BRANCH_FILE, TreeMap.class);
        String head = readObject(HEAD_FILE, String.class);
        if (!head.equals(branchName)) {
            addStage.clear();
            removeStage.clear();
        }
        Utils.writeObject(STAG_ADD, addStage);
        Utils.writeObject(STAG_REM, removeStage);
    }


    @SuppressWarnings("unchecked")
    public void checkout(String[] args) {
        checkoutHelper3(args);
        checkoutHelper4(args);
        checkoutHelper2(args);
    }

    @SuppressWarnings("unchecked")
    public void rm(String fileName) {
        TreeMap<String, String>
                addStage = readObject(STAG_ADD, TreeMap.class);
        TreeMap<String, String>
                removeStage = readObject(STAG_REM, TreeMap.class);
        String head = readObject(HEAD_FILE, String.class);
        TreeMap branches = readObject(BRANCH_FILE, TreeMap.class);
        String commitID = (String) branches.get(head);
        Commit recentCommit = readObject(Utils
                .join(COMMITS_FOLDER, commitID), Commit.class);

        boolean fileTracked = false;
        TreeMap<String, String> trackedFiles = recentCommit.getMyFiles();
        fileTracked = trackedFiles.containsKey(fileName);
        if (addStage.containsKey(fileName)) {
            addStage.remove(fileName);
        } else if (fileTracked) {
            Utils.restrictedDelete(Utils.join(CWD, fileName));
            removeStage.put(fileName, sha1(fileName));
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        Utils.writeObject(STAG_ADD, addStage);
        Utils.writeObject(STAG_REM, removeStage);
    }

    @SuppressWarnings("unchecked")
    public void globalLog() {
        for (String commitID : plainFilenamesIn(COMMITS_FOLDER)) {
            Commit commit = readObject(Utils
                    .join(COMMITS_FOLDER, commitID), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitID);
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    @SuppressWarnings("unchecked")
    public void find(String message) {
        boolean foundCommit = false;
        for (String commitID : plainFilenamesIn(COMMITS_FOLDER)) {
            Commit commit = readObject(Utils
                    .join(COMMITS_FOLDER, commitID), Commit.class);
            if (commit.getMessage().equals(message)) {
                System.out.println(commitID);
                foundCommit = true;
            }
        }
        if (!foundCommit) {
            System.out.println("Found no commit with that message.");
        }
    }

    @SuppressWarnings("unchecked")
    public void status() {
        if (!GITLET_FOLDER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        System.out.println("=== Branches ===");
        TreeMap<String, String>
                branches = readObject(BRANCH_FILE, TreeMap.class);
        String head = readObject(HEAD_FILE, String.class);
        ArrayList<String> branchList = new ArrayList<>(branches.keySet());
        for (String branch : branchList) {
            if (branch.equals(head)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        TreeMap<String, String>
                addStage = readObject(STAG_ADD, TreeMap.class);
        ArrayList<String>
                stageList = new ArrayList<>(addStage.keySet());
        for (String file : stageList) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        TreeMap<String, String>
                removeStage = readObject(STAG_REM, TreeMap.class);
        ArrayList<String> removeList = new ArrayList<>(removeStage.keySet());
        for (String file : removeList) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    @SuppressWarnings("unchecked")
    public void branch(String branchName) {
        TreeMap<String, String>
                branches = readObject(BRANCH_FILE, TreeMap.class);
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        String head = readObject(HEAD_FILE, String.class);
        String currSha = branches.get(head);
        branches.put(branchName, currSha);
        Utils.writeObject(BRANCH_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    public void rmBranch(String branchName) {
        TreeMap<String, String>
                branches = readObject(BRANCH_FILE, TreeMap.class);
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (branchName.equals(readObject(HEAD_FILE, String.class))) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branches.remove(branchName);
        }
        Utils.writeObject(BRANCH_FILE, branches);
    }

    @SuppressWarnings("unchecked")
    public void reset(String commitID) {
        TreeMap<String, String>
                branches = readObject(BRANCH_FILE, TreeMap.class);

        String head = readObject(HEAD_FILE, String.class);
        String currCommitSha = branches.get(head);
        Commit recentCommit = readObject(Utils
                .join(COMMITS_FOLDER, currCommitSha), Commit.class);

        File commitFile = Utils.join(COMMITS_FOLDER, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit checkoutCommit = readObject(Utils
                .join(COMMITS_FOLDER, commitID), Commit.class);
        for (String file : plainFilenamesIn(CWD)) {
            String contentID = sha1(Utils.readContentsAsString(Utils
                    .join(CWD, file)));
            if (!recentCommit.fileExists(file, contentID)
                    && checkoutCommit.getMyFiles().containsKey(file)) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String file : plainFilenamesIn(CWD)) {
            String contentID = sha1(Utils.readContentsAsString(Utils
                    .join(CWD, file)));
            if (recentCommit.fileExists(file, contentID)
                    && !checkoutCommit.getMyFiles().containsKey(file)) {
                Utils.join(CWD, file).delete();
            }
        }

        TreeMap<String, String>
                addStage = readObject(STAG_ADD, TreeMap.class);
        TreeMap<String, String>
                removeStage = readObject(STAG_REM, TreeMap.class);

        addStage.clear();
        removeStage.clear();

        Utils.writeObject(STAG_ADD, addStage);
        Utils.writeObject(STAG_REM, removeStage);
        TreeMap<String, String> myFiles = checkoutCommit.getMyFiles();
        ArrayList<String> myFilesList = new ArrayList<>(myFiles.keySet());
        for (String fileName : myFilesList) {
            File blobFile = new File(BLOBS_FOLDER, myFiles.get(fileName));
            String copyContents = readContentsAsString(blobFile);
            File newFile = new File(CWD, fileName);
            Utils.writeContents(newFile, copyContents);
        }
        branches.put(head, commitID);
        writeObject(BRANCH_FILE, branches);

    }

    @SuppressWarnings("unchecked")
    public void merge(String givenBranch) {
        TreeMap<String, String>
                branches = readObject(BRANCH_FILE, TreeMap.class);
        String head = readObject(HEAD_FILE, String.class);

        TreeMap<String, String>
                addStage = readObject(STAG_ADD, TreeMap.class);
        TreeMap<String, String>
                removeStage = readObject(STAG_REM, TreeMap.class);

        mergeFailureCases(givenBranch, branches, head, addStage, removeStage);

        String headCommitSha = branches.get(head);
        Commit headCommit = readObject(Utils
                .join(COMMITS_FOLDER, headCommitSha), Commit.class);

        String splitCommitSha = splitPoint(branches, head, givenBranch);
        Commit splitCommit = readObject(Utils
                .join(COMMITS_FOLDER, splitCommitSha), Commit.class);

        String givenCommitSha = branches.get(givenBranch);
        Commit givenCommit = readObject(Utils
                .join(COMMITS_FOLDER, givenCommitSha), Commit.class);

        basicBranchCheck(givenBranch, headCommitSha,
                splitCommitSha, givenCommitSha);
        untrackedCheck(headCommit, givenCommit);

        HashSet<String> sets = new HashSet<>();
        sets.addAll(headCommit.getMyFiles()
                .keySet());
        sets.addAll(givenCommit.getMyFiles().keySet());

        boolean mergeConflict = false;
        for (String filename : sets) {
            String headContID = headCommit.getMyFiles().get(filename);
            String givenContID = givenCommit.getMyFiles().get(filename);
            String splitContID = splitCommit.getMyFiles().get(filename);

            mergeCases(addStage, removeStage, filename,
                    headContID, givenContID, splitContID);

            if (splitContID == null) {
                splitContID = "";
            }
            if (headContID == null) {
                headContID = "";
            }
            if (givenContID == null) {
                givenContID = "";
            }
            mergeConflict = isMergeConflict(addStage, mergeConflict,
                    filename, headContID, givenContID, splitContID);
        }
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        mergeCommitHelper(givenBranch, branches, head, addStage,
                removeStage, headCommitSha, headCommit, givenCommitSha);
    }

    private void mergeCases(TreeMap<String, String> addStage,
                            TreeMap<String, String> removeStage,
                            String filename,
                            String headContID, String givenContID,
                            String splitContID) {
        if (headContID != null && givenContID != null
                && splitContID != null) {
            if (!givenContID.equals(splitContID)
                    && headContID.equals(splitContID)) {
                addStage.put(filename, givenContID);
                String contents = Utils.readContentsAsString(Utils
                        .join(BLOBS_FOLDER, givenContID));
                Utils.writeContents(Utils.join(CWD, filename), contents);
            }
        }

        if (splitContID == null && headContID == null
                && givenContID != null) {
            addStage.put(filename, givenContID);
            String contents = Utils.readContentsAsString(Utils
                    .join(BLOBS_FOLDER, givenContID));
            Utils.writeContents(Utils.join(CWD, filename), contents);
        }

        if (splitContID != null && headContID != null
                && givenContID == null
                && splitContID.equals(headContID)) {
            removeStage.put(filename, headContID);
            Utils.restrictedDelete(filename);
        }
    }

    private void mergeFailureCases(String givenBranch, TreeMap<String,
            String> branches, String head, TreeMap<String, String> addStage,
                                   TreeMap<String, String> removeStage) {
        if (!branches.containsKey(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (givenBranch.equals(head)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        if (!addStage.isEmpty() || !removeStage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    private void basicBranchCheck(String givenBranch, String headCommitSha,
                                  String splitCommitSha,
                                  String givenCommitSha) {
        if (givenCommitSha.equals(splitCommitSha)) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            System.exit(0);
        }
        if (headCommitSha.equals(splitCommitSha)) {
            String[] args = {"checkout", givenBranch};
            checkout(args);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    private void untrackedCheck(Commit headCommit, Commit givenCommit) {
        for (String file : plainFilenamesIn(CWD)) {
            if (!headCommit.getMyFiles().containsKey(file)
                    && givenCommit.getMyFiles()
                    .containsKey(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    private boolean isMergeConflict(TreeMap<String, String> addStage,
                                    boolean mergeConflict, String filename,
                                    String headContID, String givenContID,
                                    String splitContID) {
        if (!splitContID.equals(headContID)
                && !splitContID.equals(givenContID)
                && !headContID.equals(givenContID)) {
            mergeConflict = true;
            String headCont = "";
            String givenCont = "";
            String mergeCont = "";
            if (!headContID.equals("")) {
                headCont = readContentsAsString(Utils
                        .join(BLOBS_FOLDER, headContID));
            }
            if (!givenContID.equals("")) {
                givenCont = readContentsAsString(Utils
                        .join(BLOBS_FOLDER, givenContID));
            }
            mergeCont = "<<<<<<< HEAD\n"
                    + headCont
                    + "=======\n"
                    + givenCont
                    + ">>>>>>>\n";
            Utils.writeContents(Utils.join(CWD, filename), mergeCont);
            String mergeContID = sha1(mergeCont);
            addStage.put(filename, mergeContID);
            Utils.writeContents(Utils
                    .join(BLOBS_FOLDER, mergeContID), mergeCont);
        }
        return mergeConflict;
    }

    private void mergeCommitHelper(String givenBranch, TreeMap<String,
            String> branches, String head, TreeMap<String, String> addStage,
                                   TreeMap<String, String> removeStage,
                                   String headCommitSha, Commit headCommit,
                                   String givenCommitSha) {
        Commit mergeCommit = new Commit("Merged " + givenBranch
                + " into " + head + ".", headCommitSha, givenCommitSha);
        mergeCommit.addToMyFiles(headCommit.getMyFiles());
        mergeCommit.addToMyFiles(addStage);
        mergeCommit.removeFromMyFiles(removeStage);
        String mergeCommitID = mergeCommit.getID();
        Utils.writeObject(Utils
                .join(COMMITS_FOLDER, mergeCommitID), mergeCommit);
        addStage.clear();
        removeStage.clear();
        branches.put(head, mergeCommitID);

        writeObject(BRANCH_FILE, branches);
        writeObject(STAG_ADD, addStage);
        writeObject(STAG_REM, removeStage);
    }

    @SuppressWarnings("unchecked")
    public HashSet<String> updateParents(HashSet<String> tracker) {
        HashSet<String> newTracker = new HashSet<>();
        for (String c : tracker) {
            if (c.equals("")) {
                continue;
            }
            Commit commit = readObject(Utils
                    .join(COMMITS_FOLDER, c), Commit.class);
            newTracker.add(commit.getParent());
            newTracker.add(commit.getParent2());
        }
        return newTracker;
    }

    @SuppressWarnings("unchecked")
    public String splitPoint(TreeMap<String, String> branches,
                             String curr, String given) {
        HashMap<String, Integer> currHistory = new HashMap<>();
        HashSet<String> givenHistory = new HashSet<>();
        HashSet<String> currTracker = new HashSet<>();
        currTracker.add(branches.get(curr));
        HashSet<String> givenTracker = new HashSet<>();
        int dist = 0;
        givenTracker.add(branches.get(given));
        HashMap<String, Integer> splits = new HashMap<>();
        while (!currTracker.isEmpty() && !givenTracker.isEmpty()) {
            for (String c : currTracker) {
                currHistory.put(c, dist);
            }
            for (String c : givenTracker) {
                givenHistory.add(c);
            }
            for (String c : givenTracker) {
                if (!c.isEmpty() && currHistory.containsKey(c)) {
                    splits.put(c, currHistory.get(c));
                }
            }
            for (String c : currTracker) {
                if (!c.isEmpty() && givenHistory.contains(c)) {
                    splits.put(c, dist);
                }
            }
            currTracker = updateParents(currTracker);
            givenTracker = updateParents(givenTracker);
            dist += 1;
        }

        return Collections.min(splits.entrySet(), Map.Entry
                .comparingByValue()).getKey();
    }


}
