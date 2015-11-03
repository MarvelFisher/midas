package com.cyanspring.adaptor.future.wind.refdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class FileUtil {

    private static final Logger log = LoggerFactory
            .getLogger(FileUtil.class);

    public static <T> List<T> getListFromFile(String listPath) {
        XStream xstream = new XStream(new DomDriver());
        File file = new File(listPath);
        List<T> list = new ArrayList<>();
        try {
            if (file.exists()) {
                list = (List<T>) xstream.fromXML(file);
            } else {
                log.error("Missing file: " + listPath);
            }
        }catch (Exception e) {
            log.error("load file X ", e);
        }
        return list;
    }

    public static <T> void saveListToFile(String path, List<T> list) {
        File file = new File(path);
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            xstream.toXML(list, writer);
            os.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static <K, T> HashMap<K, T> getHashMapFromFile(String hashMapPath){
        XStream xstream = new XStream(new DomDriver());
        File file = new File(hashMapPath);
        HashMap<K, T> hashMap = new HashMap<>();
        try {
            if (file.exists()) {
                hashMap = (HashMap<K, T>) xstream.fromXML(file);
            } else {
                log.error("Missing file: " + hashMapPath);
            }
        }catch (Exception e) {
            log.error("load file X ", e);
        }
        return hashMap;
    }

    public static <K, T> void saveHashMapToFile(String path, HashMap<K, T> hashMap) {
        File file = new File(path);
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        try {
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            xstream.toXML(hashMap, writer);
            os.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
