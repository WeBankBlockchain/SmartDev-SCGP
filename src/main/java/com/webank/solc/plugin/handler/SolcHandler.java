package com.webank.solc.plugin.handler;

import com.webank.solc.plugin.enums.SolcVersionEnum;
import org.fisco.solc.compiler.Solc;
import org.fisco.solc.compiler.SolidityCompiler;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author aaronchu
 * @Description
 * @data 2021/04/29
 */
public class SolcHandler {

    public static SolidityCompiler buildSolidityCompiler(SolcVersionEnum solcVersion) {
        try{
            SolidityCompiler solidityCompiler = SolidityCompiler.getInstance();
            Field solcField = SolidityCompiler.class.getDeclaredField("solc");
            Field smSolcField = SolidityCompiler.class.getDeclaredField("sMSolc");
            solcField.setAccessible(true);
            smSolcField.setAccessible(true);
            solcField.set(solidityCompiler, createSolc(solcVersion, false));
            smSolcField.set(solidityCompiler, createSolc(solcVersion, true));
            return solidityCompiler;
        }
        catch (Exception ex){
            onException(ex);
            return null;
        }
    }

    // 问题1：怎么创建——自己扩展，或者用替换大法，得到一个新的，受控制的Solc
    // 问题2：怎么注入——用反射，继承等方式把新的Solc注入到SolidityCompiler即可
    public static Solc createSolc(SolcVersionEnum solcVersion, boolean gm)  throws Exception{


        File solcFile = solcFile(solcVersion, gm);
        solcFile.setExecutable(true);
        Solc solc = new Solc(gm);
        Field field = Solc.class.getDeclaredField("solc");
        field.setAccessible(true);
        field.set(solc, solcFile);
        return solc;
    }

    private static File solcFile(SolcVersionEnum solcVersion, boolean gm) throws Exception{
        ZipFile zipFile = null;
        try{
            //1. Load zip file from resources according to  solidity version
            zipFile = getZipFileFromResources(solcVersion);
            //2. find index file
            ZipEntry indexFile = findIndexFile(zipFile, gm);
            //3. read related entries per index file(is this a bad design?)
            List<ZipEntry> relatedEntries = loadRelatedZipEntry(zipFile, indexFile, gm);
            //4. copy related files into tmp dir
            File solcFile = copyEntriesToLocal(zipFile, relatedEntries, solcVersion, gm);
            return solcFile;
        }
        finally {
            if(zipFile != null) zipFile.close();
        }
    }

    private static ZipFile getZipFileFromResources(SolcVersionEnum solcVersion)  throws Exception{
        File zipFile =
                new File(
                        System.getProperty("user.home"),
                        ".fisco/solc" + "/" + solcVersion.getVersion() + "/" + solcVersion.getJarName());
        if(zipFile.exists()) return new ZipFile(zipFile);//incase multiple processes(cover 80% cases. so no processes lock)
        zipFile.mkdirs();
        zipFile.deleteOnExit();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream("jar/" + solcVersion.getJarName())) {
            Files.copy(in, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return new ZipFile(zipFile);
        }
    }


    private static ZipEntry findIndexFile(ZipFile zipFile, boolean gm) {
        String indexFileName = getSolcDir(gm) + "file.list";
        Enumeration<? extends ZipEntry> entryEnumeration = zipFile.entries();
        while (entryEnumeration.hasMoreElements()) {
            ZipEntry entry = entryEnumeration.nextElement();
            if (entry.getName().equals(indexFileName)) {
                return entry;
            }
        }
        throw new RuntimeException("index file " + indexFileName + " not found");
    }

    private static List<ZipEntry> loadRelatedZipEntry(ZipFile zipFile, ZipEntry indexEntry, boolean gm)  throws Exception{
        //Read entries
        Set<String> indexedEntryNames = new HashSet<>();
        try (Scanner scanner = new Scanner(zipFile.getInputStream(indexEntry))) {
            while (scanner.hasNext()) {
                String s = scanner.next();
                String fullname = getSolcDir(gm) + s;
                indexedEntryNames.add(fullname);
            }
        }
        List<ZipEntry> result = new ArrayList<>(indexedEntryNames.size());
        Enumeration<? extends ZipEntry> entryEnumeration = zipFile.entries();
        while (entryEnumeration.hasMoreElements()) {
            ZipEntry entry = entryEnumeration.nextElement();
            if (indexedEntryNames.contains(entry.getName())) {
                result.add(entry);
            }
        }
        return result;
    }


    private static File copyEntriesToLocal(ZipFile zipFile, List<ZipEntry> relatedEntries, SolcVersionEnum solcVersion, boolean gm)  throws Exception{
        File tmpDir =
                new File(
                        System.getProperty("user.home"),
                        ".fisco/solc" + "/" + solcVersion.getVersion() + "/" + (gm ? "sm3" : "keccak256"));

        tmpDir.mkdirs();
        File solcFile = null;

        for (ZipEntry zipEntry : relatedEntries) {
            String[] components = zipEntry.getName().split("\\/");;
            String simpleName = components[components.length-1];
            File targetFile =  new File(tmpDir, simpleName);
            if(simpleName.contains("solc")){
                solcFile = targetFile;
            }
            try (InputStream in = zipFile.getInputStream(zipEntry)) {
                Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return solcFile;
    }

    private static String getSolcDir(boolean sm) {

        String osName = getOS();
        String resourceDir = "native/" + (sm ? "sm/" : "ecdsa/") + getOS() + "/";
        if (osName.equals("linux")) {
            // Add support for arm
            String archName = getArch();
            if (!archName.isEmpty()) {
                resourceDir += getArch();
                resourceDir += "/";
            }
        }

        resourceDir += "solc/";

        return resourceDir;
    }

    private static String getArch() {
        String archName = System.getProperty("os.arch", "");
        if (archName.contains("aarch64")) {
            return "arm";
        } else {
            return "";
            // throw new RuntimeException("Can't find solc compiler: unrecognized Arch: " +
            // archName);
        }
    }

    private static String getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "win";
        } else if (osName.contains("linux")) {
            return "linux";
        } else if (osName.contains("mac")) {
            return "mac";
        } else {
            throw new RuntimeException("Can't find solc compiler: unrecognized OS: " + osName);
        }
    }

    private static void onException(Exception ex) {
        ex.printStackTrace();
        System.out.println(ex.getMessage());
        System.exit(-1);
    }
}










