package com.webank.solc.plugin.handler;

import com.webank.solc.plugin.enums.SolcVersionEnum;
import org.fisco.solc.compiler.Solc;
import org.fisco.solc.compiler.SolidityCompiler;
import org.fisco.solc.compiler.Version;

import java.io.File;
import java.io.InputStream;
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
        return null;
    }

    // 问题1：怎么创建——自己扩展，或者用替换大法，得到一个新的，受控制的Solc
    // 问题2：怎么注入——用反射，继承等方式把新的Solc注入到SolidityCompiler即可
    public static Solc createSolc(SolcVersionEnum solcVersion, boolean isGm) {
        //1. 从资源里加载jar包
        loadJar(solcVersion, isGm);
        //2. 拷贝到指定地点（后续删除）
        //3. 从这个jar包加载
        //解析jar包
        //生成文件地址
        //把文件注入到Solc里
        return null;
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

    private static List<ZipEntry> loadRelatedZipEntry(ZipFile zipFile, ZipEntry indexEntry, boolean gm) {
        //Read entries
        Set<String> indexedEntryNames = new HashSet<>();
        try (Scanner scanner = new Scanner(zipFile.getInputStream(indexEntry))) {
            while (scanner.hasNext()) {
                String s = scanner.next();
                String fullname = getSolcDir(gm) + s;
                indexedEntryNames.add(fullname);
            }
        } catch (Exception ex) {
            onException(ex);
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

    //读取jar，将内置资源都保存在内存中
    //找到索引文件，根据索引文件，选择目标文件
    private static void loadJar(SolcVersionEnum solcVersion, boolean gm) {
        //1. Load zip file from resources according to  solidity version
        ZipFile zipFile = getZipFileFromResources(solcVersion);
        //2. find index file
        ZipEntry indexFile = findIndexFile(zipFile, gm);
        //3. read related entries per index file(is this a bad design?)
        List<ZipEntry> relatedEntries = loadRelatedZipEntry(zipFile, indexFile, gm);
        //4. copy related files into tmp dir
        copyEntriesToLocal(zipFile, relatedEntries, solcVersion, gm);
    }

    private static File copyEntriesToLocal(ZipFile zipFile, List<ZipEntry> relatedEntries, SolcVersionEnum solcVersion, boolean gm) {
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
            } catch (Exception ex) {
                onException(ex);
            }
        }

        return solcFile;
    }

    private static ZipFile getZipFileFromResources(SolcVersionEnum solcVersion) {
        //1. Copy jar to destination
        File zipFile =
                new File(
                        System.getProperty("user.home"),
                        ".fisco/solc" + "/" + solcVersion.getVersion() + "/" + solcVersion.getJarName());
        zipFile.mkdirs();
        zipFile.deleteOnExit();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream("jar/" + solcVersion.getJarName())) {
            Files.copy(in, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return new ZipFile(zipFile);
        } catch (Exception ex) {
            onException(ex);
            return null;
        }
    }

//    private void initBundled(boolean sm) throws IOException {
//
//        File tmpDir =
//                new File(
//                        System.getProperty("user.home"),
//                        ".fisco/solc" + "/" + Version.version + "/" + (sm ? "sm3" : "keccak256"));
//
//        tmpDir.mkdirs();
//        String solcDir = getSolcDir(sm);
//
//        try (InputStream is = getClass().getResourceAsStream(solcDir + "file.list"); ) {
//            try (Scanner scanner = new Scanner(is)) {
//                while (scanner.hasNext()) {
//                    String s = scanner.next();
//                    File targetFile = new File(tmpDir, s);
//
//                    try (InputStream fis = getClass().getResourceAsStream(solcDir + s); ) {
//                        Files.copy(fis, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                        if (solc == null) {
//                            // first file in the list denotes executable
//                            solc = targetFile;
//                            solc.setExecutable(true);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//    }

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










