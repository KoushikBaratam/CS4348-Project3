import java.io.*;
import java.nio.charset.StandardCharsets;

public class BTree {

    // constants used for validating and reading index blocks
    static final int BLOCK_SIZE = 512;
    static final String MAGIC = "4348PRJ3";

    // constants defining the b-tree degree and node capacities
    static final int T = 10;
    static final int MAX_KEYS = 19;
    static final int MAX_CHILDREN = 20;

    // file handle and header metadata for the b-tree
    RandomAccessFile file;

    long rootId;
    long nextBlockId;

    class Node {

        // metadata identifying the node location and parent
        long blockId;
        long parentId;

        int numKeys;

        // arrays storing node keys, values, and child pointers
        long[] keys =
                new long[MAX_KEYS];

        long[] values =
                new long[MAX_KEYS];

        long[] children =
                new long[MAX_CHILDREN];
    }

    public BTree(String filename) throws Exception {

        // checks if the requested index file exists
        File f = new File(filename);

        if (!f.exists()) {
            throw new Exception("Index file missing");
        }

        // opens the index file for random-access operations
        file = new RandomAccessFile(f, "rw");

        // loads and validates the file header
        readHeader();
    }

    void readHeader() throws Exception {

        // reads the first 512-byte block containing the file header
        byte[] block = new byte[BLOCK_SIZE];

        file.seek(0);
        file.readFully(block);

        // extracts and validates the magic number from the header
        String magic =
                new String(block, 0, 8,
                StandardCharsets.US_ASCII);

        if (!magic.equals(MAGIC)) {
            throw new Exception("Invalid index");
        }

        // loads the root node id and next free block id
        rootId = readLong(block, 8);
        nextBlockId = readLong(block, 16);
    }

    Node readNode(long blockId)
            throws Exception {

        // loads a full node block from the index file
        byte[] block =
                new byte[BLOCK_SIZE];

        file.seek(blockId * BLOCK_SIZE);

        file.readFully(block);

        // creates a node object and loads header information
        Node node = new Node();

        node.blockId =
                readLong(block, 0);

        node.parentId =
                readLong(block, 8);

        node.numKeys =
                (int)readLong(block, 16);

        int offset = 24;

        // reads all stored keys from the node block
        for (int i = 0;
             i < MAX_KEYS;
             i++) {

            node.keys[i] =
                    readLong(block, offset);

            offset += 8;
        }

        // reads all stored values from the node block
        for (int i = 0;
             i < MAX_KEYS;
             i++) {

            node.values[i] =
                    readLong(block, offset);

            offset += 8;
        }

        // reads all child block pointers from the node block
        for (int i = 0;
             i < MAX_CHILDREN;
             i++) {

            node.children[i] =
                    readLong(block, offset);

            offset += 8;
        }

        return node;
    }

    void writeNode(Node node)
            throws Exception {

        // creates a block buffer for node serialization
        byte[] block =
                new byte[BLOCK_SIZE];

        writeLong(block, 0, node.blockId);
        writeLong(block, 8, node.parentId);
        writeLong(block, 16, node.numKeys);

        int offset = 24;

        // writes all node keys into the block
        for (int i = 0;
             i < MAX_KEYS;
             i++) {

            writeLong(block,
                    offset,
                    node.keys[i]);

            offset += 8;
        }

        // writes all node values into the block
        for (int i = 0;
             i < MAX_KEYS;
             i++) {

            writeLong(block,
                    offset,
                    node.values[i]);

            offset += 8;
        }

        // writes all child pointers into the block
        for (int i = 0;
             i < MAX_CHILDREN;
             i++) {

            writeLong(block,
                    offset,
                    node.children[i]);

            offset += 8;
        }

        // saves the node block to disk
        file.seek(node.blockId * BLOCK_SIZE);
        file.write(block);
    }

    Node allocateNode(long parentId)
            throws Exception {

        // creates a new empty node
        Node node = new Node();

        node.blockId =
                nextBlockId;

        nextBlockId++;

        node.parentId =
                parentId;

        node.numKeys = 0;

        writeNode(node);

        return node;
    }

    long search(long key)
            throws Exception {

        // returns failure immediately if the tree is empty
        if (rootId == 0) {
            return -1;
        }

        return searchRecursive(
                rootId,
                key);
    }

    long searchRecursive(long nodeId,
                         long key)
            throws Exception {

        // loads the current node being searched
        Node node =
                readNode(nodeId);

        int i = 0;

        // scans keys until the correct position is found
        while (i < node.numKeys
                && key > node.keys[i]) {

            i++;
        }

        // returns the matching value if the key exists
        if (i < node.numKeys
                && key == node.keys[i]) {

            return node.values[i];
        }

        // returns failure if the search reaches a leaf node
        if (node.children[i] == 0) {
            return -1;
        }

        return searchRecursive(
                node.children[i],
                key);
    }

    void insert(long key,
                long value)
            throws Exception {

        // creates the root node if the tree is empty
        if (rootId == 0) {

            Node root =
                    allocateNode(0);

            root.keys[0] = key;
            root.values[0] = value;
            root.numKeys = 1;

            rootId =
                    root.blockId;

            writeNode(root);

            return;
        }

        Node root =
                readNode(rootId);

        // splits the root if it is full
        if (root.numKeys == MAX_KEYS) {

            Node newRoot =
                    allocateNode(0);

            root.parentId =
                    newRoot.blockId;

            newRoot.children[0] =
                    root.blockId;

            rootId =
                    newRoot.blockId;

            writeNode(root);

            splitChild(
                    newRoot,
                    0,
                    root);

            insertNonFull(
                    newRoot,
                    key,
                    value);
        }

        else {

            insertNonFull(
                    root,
                    key,
                    value);
        }
    }

    void insertNonFull(Node node,
                       long key,
                       long value)
            throws Exception {

        int i =
                node.numKeys - 1;

        // inserts directly into a leaf node
        if (node.children[0] == 0) {

            while (i >= 0
                    && key < node.keys[i]) {

                node.keys[i + 1] =
                        node.keys[i];

                node.values[i + 1] =
                        node.values[i];

                i--;
            }

            node.keys[i + 1] = key;
            node.values[i + 1] = value;

            node.numKeys++;

            writeNode(node);
        }

        // recursively inserts into the correct child
        else {

            while (i >= 0
                    && key < node.keys[i]) {

                i--;
            }

            i++;

            Node child =
                    readNode(
                            node.children[i]);

            // splits the child if it is full
            if (child.numKeys == MAX_KEYS) {

                splitChild(
                        node,
                        i,
                        child);

                if (key > node.keys[i]) {
                    i++;
                }
            }

            child =
                    readNode(
                            node.children[i]);

            insertNonFull(
                    child,
                    key,
                    value);
        }
    }

    void splitChild(Node parent,
                    int index,
                    Node child)
            throws Exception {

        // creates a new node for the split
        Node newNode =
                allocateNode(
                        parent.blockId);

        newNode.numKeys =
                T - 1;

        // copies upper half keys and values
        for (int j = 0;
             j < T - 1;
             j++) {

            newNode.keys[j] =
                    child.keys[j + T];

            newNode.values[j] =
                    child.values[j + T];
        }

        // copies child pointers if the node is internal
        for (int j = 0;
             j < T;
             j++) {

            newNode.children[j] =
                    child.children[j + T];
        }

        child.numKeys =
                T - 1;

        // shifts parent child pointers
        for (int j = parent.numKeys;
             j >= index + 1;
             j--) {

            parent.children[j + 1] =
                    parent.children[j];
        }

        parent.children[index + 1] =
                newNode.blockId;

        // shifts parent keys and values
        for (int j = parent.numKeys - 1;
             j >= index;
             j--) {

            parent.keys[j + 1] =
                    parent.keys[j];

            parent.values[j + 1] =
                    parent.values[j];
        }

        // promotes the median key into the parent
        parent.keys[index] =
                child.keys[T - 1];

        parent.values[index] =
                child.values[T - 1];

        parent.numKeys++;

        writeNode(child);
        writeNode(newNode);
        writeNode(parent);
    }

    void printAll()
            throws Exception {

        // prints all entries in sorted order
        if (rootId != 0) {
            printRecursive(rootId);
        }
    }

    void printRecursive(long nodeId)
            throws Exception {

        // performs inorder traversal of the b-tree
        Node node =
                readNode(nodeId);

        for (int i = 0;
             i < node.numKeys;
             i++) {

            if (node.children[i] != 0) {

                printRecursive(
                        node.children[i]);
            }

            System.out.println(
                    node.keys[i]
                    + ","
                    + node.values[i]);
        }

        if (node.children[node.numKeys] != 0) {

            printRecursive(
                    node.children[node.numKeys]);
        }
    }

    void extractAll(PrintWriter writer)
            throws Exception {

        // extracts all entries into a csv file
        if (rootId != 0) {
            extractRecursive(
                    rootId,
                    writer);
        }
    }

    void extractRecursive(long nodeId,
                          PrintWriter writer)
            throws Exception {

        // performs inorder traversal for csv extraction
        Node node =
                readNode(nodeId);

        for (int i = 0;
             i < node.numKeys;
             i++) {

            if (node.children[i] != 0) {

                extractRecursive(
                        node.children[i],
                        writer);
            }

            writer.println(
                    node.keys[i]
                    + ","
                    + node.values[i]);
        }

        if (node.children[node.numKeys] != 0) {

            extractRecursive(
                    node.children[node.numKeys],
                    writer);
        }
    }

    void writeLong(byte[] arr,
                   int offset,
                   long value) {

        // writes a 64-bit integer into the byte array using big-endian order
        for (int i = 7; i >= 0; i--) {

            arr[offset + i] =
                    (byte)(value & 0xff);

            value >>= 8;
        }
    }

    long readLong(byte[] arr,
                  int offset) {

        // reconstructs a 64-bit integer from big-endian bytes
        long value = 0;

        for (int i = 0; i < 8; i++) {

            value =
                    (value << 8)
                    | (arr[offset + i] & 0xffL);
        }

        return value;
    }
}