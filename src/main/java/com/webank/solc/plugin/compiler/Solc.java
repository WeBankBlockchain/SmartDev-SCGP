package com.webank.solc.plugin.compiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

class Solc {


    private ClassLoader classLoader;

    private File solc;

    public Solc(ClassLoader classLoader){
        this.classLoader = classLoader;

        try{
            initBundled();
        }
        catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    public Solc(){
        this(Thread.currentThread().getContextClassLoader());
    }


    private void initBundled() throws IOException {
        //Ensure file exists
        File tmpDir = new File(System.getProperty("user.home"), "solc");
        tmpDir.mkdirs();

        //Load resource from jar
        InputStream is = this.classLoader.getResourceAsStream("native/" + getOS() + "/solc/file.list");
        try(Scanner scanner = new Scanner(is)) {
            while(scanner.hasNext()){
                String s = scanner.next();
                File targetFile = new File(tmpDir, s);
                InputStream fis = this.classLoader.getResourceAsStream("native/" + getOS() + "/solc/" + s);
                Files.copy(fis, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if(solc == null){
                    // first file in the list denotes executable
                    solc = targetFile;
                    boolean executable = solc.setExecutable(true);
                    if(!executable) System.out.println("Failed to set executable");
                }
                fis.close();;
            }
        }
        is.close();
    }


    private String getOS(){
        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("win")){
            return "win";
        }else if(osName.contains("linux")){
            return "linux";
        }else if(osName.contains("mac")){
            return "mac";
        }else{
            throw new RuntimeException("Can't find solc compiler: unrecognized OS: " + osName);
        }
    }

    public File getExecutable() {
        return solc;
    }
}
