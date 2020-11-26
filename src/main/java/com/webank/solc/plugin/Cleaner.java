package com.webank.solc.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Cleaner {

    private static List<File> filesToDelete = new ArrayList<>();

    public static void recordDelete(File file){
        filesToDelete.add(file);
    }

    public static void clean(){
        for(File f: filesToDelete){
            boolean success = f.delete();
            if(!success){
                System.out.println("Failed to delete "+f.getAbsolutePath());
            }
            else {
                System.out.println("Success to delete "+f.getAbsolutePath());
            }
        }
        filesToDelete.clear();
    }

}
