import java.io.*;
import java.nio.charset.StandardCharsets;

public class Project3 {

    // constants used for block sizing and file validation
    static final int BLOCK_SIZE = 512;
    static final int T = 10;
    static final int MAX_KEYS = 19;
    static final int MAX_CHILDREN = 20;
    static final String MAGIC = "4348PRJ3";

    public static void main(String[] args) {

        // validates command-line arguments before processing commands
        if (args.length < 2) {
            System.out.println("Usage error");
            return;
        }

        // extracts the command name and index filename
        String command = args[0];
        String file = args[1];

        try {

            // handles supported commands from the command line
            switch (command) {

                case "create":
                    createIndex(file);
                    break;

                // handles insertion of a key/value pair into the index
                case "insert":

                    BTree treeInsert =
                            new BTree(file);

                    long insertKey =
                            Long.parseLong(args[2]);

                    long insertValue =
                            Long.parseLong(args[3]);

                    treeInsert.insert(
                            insertKey,
                            insertValue);

                    System.out.println(
                            "Inserted");

                    break;

                // handles searching for a key inside the index file
                case "search":

                    BTree tree =
                            new BTree(file);

                    long key =
                            Long.parseLong(args[2]);

                    long value =
                            tree.search(key);

                    if (value == -1) {
                        System.out.println("Key not found");
                    }

                    else {
                        System.out.println(
                                key + "," + value);
                    }

                    break;

                // loads key/value pairs from a csv file
                case "load":

                    BTree loadTree =
                            new BTree(file);

                    loadCsv(
                            loadTree,
                            args[2]);

                    System.out.println(
                            "CSV loaded");

                    break;

                // prints all key/value pairs in sorted order
                case "print":

                    BTree printTree =
                            new BTree(file);

                    printTree.printAll();

                    break;

                // extracts all key/value pairs into a csv file
                case "extract":

                    BTree extractTree =
                            new BTree(file);

                    extractCsv(
                            extractTree,
                            args[2]);

                    System.out.println(
                            "CSV extracted");

                    break;

                default:
                    System.out.println("Unsupported command");
            }

        // catches unexpected runtime or file errors
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void createIndex(String filename) throws Exception {

        // checks if the index file already exists
        File f = new File(filename);

        if (f.exists()) {
            throw new Exception("File already exists");
        }

        // opens a new random access file for reading and writing
        RandomAccessFile raf = new RandomAccessFile(f, "rw");

        // creates an empty 512-byte header block
        byte[] block = new byte[BLOCK_SIZE];

        // converts the magic string into ASCII bytes
        byte[] magicBytes =
                MAGIC.getBytes(StandardCharsets.US_ASCII);

        // stores the magic number at the beginning of the file header
        System.arraycopy(magicBytes, 0, block, 0, 8);

        // initializes the root id and next available block id
        writeLong(block, 8, 0);
        writeLong(block, 16, 1);

        // writes the completed header block to disk
        raf.write(block);

        // closes the file after creation is complete
        raf.close();

        System.out.println("Index created");
    }

    static void loadCsv(BTree tree,
                        String csvFile)
            throws Exception {

        // opens the csv input file for reading
        BufferedReader reader =
                new BufferedReader(
                        new FileReader(csvFile));

        String line;

        // reads and inserts each key/value pair
        while ((line = reader.readLine()) != null) {

            String[] parts =
                    line.split(",");

            long key =
                    Long.parseLong(parts[0]);

            long value =
                    Long.parseLong(parts[1]);

            tree.insert(key, value);
        }

        reader.close();
    }

    static void extractCsv(BTree tree,
                           String csvFile)
            throws Exception {

        // creates a writer for csv extraction
        PrintWriter writer =
                new PrintWriter(
                        new FileWriter(csvFile));

        // writes all tree contents into the csv file
        tree.extractAll(writer);

        writer.close();
    }

    static void writeLong(byte[] arr,
                          int offset,
                          long value) {

        // writes a 64-bit integer into the byte array using big-endian order
        for (int i = 7; i >= 0; i--) {

            arr[offset + i] =
                    (byte)(value & 0xff);

            value >>= 8;
        }
    }

    static long readLong(byte[] arr,
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