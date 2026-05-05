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

    long search(long key)
            throws Exception {

        // returns failure immediately if the tree is empty
        if (rootId == 0) {
            return -1;
        }

        // begins recursive traversal from the root node
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

        // recursively continues searching in the correct child subtree
        return searchRecursive(
                node.children[i],
                key);
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