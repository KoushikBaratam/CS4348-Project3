# 2026-05-04 23:50

## Thoughts So Far
I reviewed the B-Tree index file project requirements. The program needs to support command-line operations for creating, inserting, searching, printing, loading, and extracting data from a disk-based B-Tree. I wanted to begin by creating the repository structure and starter Java files.

## Plan for This Session
- Create repository structure
- Create starter Java files
- Prepare files for implementation

## Session Reflection
I created the initial project files and set up the repository structure for the B-Tree assignment.


# 2026-05-04 23:54

## Thoughts So Far
After setting up the project structure, I wanted to keep the repository clean by ignoring generated class files and maintaining a development log throughout implementation.

## Plan for This Session
- Create `.gitignore`
- Ignore compiled `.class` files
- Add `DEVLOG.md`

## Session Reflection
I added the `.gitignore` file and started the development log to track implementation progress.


# 2026-05-05 02:01

## Thoughts So Far
The next step was implementing the command-line interface and the index file creation logic. Before implementing the B-Tree itself, I needed a working file format and header structure.

## Plan for This Session
- Implement command parser
- Add `create` command
- Initialize index file header
- Write magic number and metadata

## Session Reflection
I implemented the command parser and added index file creation logic with header initialization for the B-Tree file format.


# 2026-05-05 11:23

## Thoughts So Far
With the file header working, I moved on to node storage and search functionality. The B-Tree nodes must be stored in fixed 512-byte blocks and recursively traversed during searches.

## Plan for This Session
- Implement node structure
- Add node serialization and deserialization
- Implement recursive search traversal
- Read nodes from disk blocks

## Session Reflection
I implemented node storage handling and recursive B-Tree search traversal using disk-based node blocks.


# 2026-05-05 20:55

## Thoughts So Far
The next major feature was insertion support. Since B-Tree nodes can become full, I also needed to implement node splitting and root splitting behavior.

## Plan for This Session
- Implement insertion logic
- Add non-full node insertion
- Implement child splitting
- Handle root node splitting

## Session Reflection
I implemented B-Tree insertion logic and node splitting behavior for full nodes and root expansion.


# 2026-05-06 14:11

## Thoughts So Far
After insertion and search were working, I focused on the remaining commands. The project requires printing all key/value pairs in sorted order, bulk CSV loading, and extracting the index contents back into a CSV file.

## Plan for This Session
- Implement inorder traversal
- Add `print` command
- Add `load` command
- Add `extract` command
- Support CSV input and output

## Session Reflection
I implemented the `load`, `print`, and `extract` commands using inorder traversal for sorted output generation.


# 2026-05-06 14:17

## Thoughts So Far
After testing the full program workflow, I wanted to include a generated CSV output file in the repository as proof that extraction and traversal were functioning correctly.

## Plan for This Session
- Run complete program test
- Generate sample extracted CSV
- Verify traversal ordering
- Add output file to repository

## Session Reflection
I added the generated `output.csv` file containing extracted key/value pairs from a successful B-Tree traversal.