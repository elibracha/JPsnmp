package Module.core;

import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class generates a zip archive containing all the
 * files within constant ZIP_DIR.
 * Minimum Java version: 8
 */

public class SnmpZipMaker {

    private static final String FILE_END = ".snmprec";
    private static final String MESSAGE_TO_CREATE = "Created by Maprinter";
    private static final String MESSAGE_TO_LOG = "Added To Zip ";
    private static final String ERROR_MESSAGE = "\n---- Error : Could'nt Create A Zip File ----\n";

    public static void removeZip(String zipName) {
        try {
            File file = new File(zipName);

            if (!file.delete()) {
                System.out.println("Delete operation failed. Please delete manually");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds an extra file to the zip archive, copying in the created
     * date and a comment.
     *
     * @param enteries  set of streams to be set
     * @param zipStream archive to contain the file.
     */

    public void addToZipFile(Map<String, String> enteries, ZipOutputStream zipStream) {

        try {
            Set<String> set = enteries.keySet();

            for (String IP : set) {
                String fileName = IP + FILE_END;
                ZipEntry entry = new ZipEntry(fileName);
                entry.setComment(MESSAGE_TO_CREATE);
                zipStream.putNextEntry(entry);

                Platform.runLater(() -> System.out.println(MESSAGE_TO_LOG + fileName));

                zipStream.write(enteries.get(IP).getBytes());

                Platform.runLater(() -> System.out.println("Stored " + enteries.get(IP).getBytes().length + " bytes to " + fileName));


            }

        } catch (IOException e) {
            System.err.println(ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}