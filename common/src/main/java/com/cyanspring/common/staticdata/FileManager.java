package com.cyanspring.common.staticdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author elviswu
 */
public class FileManager {
    private static final Logger log = LoggerFactory.getLogger(FileManager.class);
    private File file;
    private FileWriter writer;

    public void loadFile(String path) {
        file = new File(System.getProperty("user.dir") + "/" + path);
        try {
            if (!file.exists()) {
            	File directory = new File(file.getParentFile().getAbsolutePath());
            	directory.mkdirs();
				file.createNewFile();
			}
            writer = new FileWriter(file);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void saveToFile(String data) {
        try {
            writer.write(data + "\n");
            writer.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void appendToFile(String data) {
        try {
            writer.append(data + "\n");
            writer.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
