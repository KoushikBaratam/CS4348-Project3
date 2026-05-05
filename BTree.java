import java.io.*;
import java.nio.charset.StandardCharsets;

public class BTree {

    // constants used for validating and reading index blocks
    static final int BLOCK_SIZE = 512;
    static final String MAGIC = "4348PRJ3";

    // file handle and header metadata for the b-tree
    RandomAccessFile file;

    long rootId;
    long nextBlockId;

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