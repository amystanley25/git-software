# Gitlet Design Document
Author: Amy Stanley

## 1. Classes and Data Structures

### Commit Class
* A folder that contains a bunch of files, each of which are serialized commit objects. The name of each file would be the SHA1 of the commit & contains the commit object

#### Instance Variables
* Message - contains the message of the commit
* Timestamp - time at which the commit was created. Assigned by the constructor
* Parent - the parent commit of a commit object
* TreeMap(myFiles) - maps the file name to the sha1 of file (blob)
* log method? - gives history of commit from inherited parents
### CommandHandler Class
#### Instance Variables
* TreeMap addStage - file name > Sha1 of blobs(contents of file) to ADD to commits
* TreeMap removeStage - file name >  Sha1 of blobs to remove from commits and commit tree
* TreeMap branches - name of branch > head of branch (master-SHA1,branch2-SHA1)
#### Methods
* init()  - Creates a new Gitlet version-control system in the current directory and sets up initial commit
* add() - Add file to Staging Area (AddStage File) and set up for commiting by creating blob
* commit() - Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time
* checkout() - Handles checkout -- [file name] & checkout [commit id] -- [file name] & checkout [branch name]
* log() - Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit
* addToStage: Put <filename,SHA1> into addStage TreeMap
* removeToStage: Put <filename,SHA1> into removeStage TreeMap
* getCommit() - checks heads file and get branch name > find SHA1 of commit from name of branch
* readCommit() - pass in String SHA1 and deserialize (readObject)
* saveCommit() - write contents of commit into 
* getParent() - checks heads file and get branch name > deserialize branches treeMAP > find SHA1 of commit from name of branch > find matching SHA1 commit in Commits Folder > read contents as String > deserialze the commit object
* updateBranches
### Blob
* A folder than contains a bunch of files, each of which has a name of a SHA id file & contains the contents of the file
### StagingArea
* 2 separate files in the gitlet directory: AddStage File & RemovalStage File.
* AddStage File: Serialized StageToAdd TreeMap - maps filename > SHA1 of file
* RemovalStage File: Serialized StageToRemove TreeMap - maps filename > SHA1 of file
### Branches
* A file in the gitlet directory: Serialized TreeMap (branches)
* Maps branch name > SHA1 of most recent commit
### HEAD
* A file in the gitlet directory that contains a String of the name of the current branch


## 2. Algorithms
* init() - Sets up initial directories and files. Creates initial commit.
* add() - Makes a copy of the file and places in existing staging area with the name of the file and contents as the SHA1.
* commit() - Adding to myFiles TreeMap, update branches TreeMap, clear staging area
* log() - 
* checkout() - 
* branch() - 

## 3. Persistence
After a serialized object has been written into a file, it can be read from the file and deserialized that is,
the type information and bytes that represent the object and its data can be used to recreate the object in memory.
The Utils functions allows to read and write file contents as well as serialize. Specifically, all the data 
in serializable objects like blobs, commits, and TreeMaps will be serialized into a file and is persisted by reading 
from, updating, and writing onto the file each time so that its information will be saved in memory.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

