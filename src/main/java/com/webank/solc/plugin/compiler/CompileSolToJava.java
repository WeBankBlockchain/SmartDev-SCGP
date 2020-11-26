package com.webank.solc.plugin.compiler;

import com.webank.solc.plugin.cl.SolcjClassLoader;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.codegen.SolidityContractGenerator;

import java.io.File;
import java.io.IOException;

/**
 * @author aaronchu
 * @Description
 * @data 2020/06/25
 */
public class CompileSolToJava {

    private SolidityCompiler solidityCompiler;
    private SolidityCompiler gmSolidityCompiler;
    public CompileSolToJava(){
        this.solidityCompiler = new SolidityCompiler(new Solc(new SolcjClassLoader(Constants.ECC_JAR)));
        this.gmSolidityCompiler=  new SolidityCompiler(new Solc(new SolcjClassLoader(Constants.GM_JAR)));
    }

    public void compileSolToJava(
            String solName,
            String packageName,
            File solFileList,
            File abiOutputDir,
            File binOutputDir,
            File smbinOutputDir,
            File javaOutputDir)
            throws Exception {
        preConditions(abiOutputDir, binOutputDir, smbinOutputDir, javaOutputDir);
        File[] solFiles = solFileList.listFiles();
        if (solFiles.length == 0) {
            System.out.println("The contracts directory is empty.");
            return;
        }
        for (File solFile : solFiles) {
            //Verify
            if(!verifySolfile(solFile, solName)){
                continue;
            }
            //ECC compile to bin
            compileToBinAndBinary(this.solidityCompiler, solFile, abiOutputDir, binOutputDir);
            //GM compile to bin
            compileToBinAndBinary(this.gmSolidityCompiler, solFile, abiOutputDir, smbinOutputDir);
            //Generate java files
            String contractname = solFile.getName().split("\\.")[0];
            File abiFile = new File(abiOutputDir,contractname + ".abi");
            File binFile = new File(binOutputDir,contractname + ".bin");
            File smbinFile = new File(smbinOutputDir,contractname + ".bin");;
            SolidityContractGenerator scg = new SolidityContractGenerator(binFile, smbinFile, abiFile, javaOutputDir, packageName);
            scg.generateJavaFiles();
        }
    }

    private void preConditions(File abiDir, File binDir, File smbinDir, File javaDir) {
        abiDir.mkdirs();
        binDir.mkdirs();
        smbinDir.mkdirs();
        javaDir.mkdirs();
    }

    private void compileToBinAndBinary(SolidityCompiler solCompiler,File solFile, File abiDir, File binDir) throws IOException{
        SolidityCompiler.Result res =
                solCompiler.compileSrc(solFile, false, true, SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN, SolidityCompiler.Options.INTERFACE, SolidityCompiler.Options.METADATA);
        if ("".equals(res.output)) {
            throw new RuntimeException("Compile error: " + res.errors);
        }
        CompilationResult result = CompilationResult.parse(res.output);
        String contractname = solFile.getName().split("\\.")[0];
        CompilationResult.ContractMetadata a =
                result.getContract(solFile.getName().split("\\.")[0]);
        FileUtils.writeStringToFile(new File(abiDir, contractname + ".abi"), a.abi);
        FileUtils.writeStringToFile(new File(binDir ,contractname + ".bin"), a.bin);
    }

    private boolean verifySolfile(File solFile, String solName){
        if (!solFile.getName().endsWith(".sol")) {
            return false;
        }
        if (solFile.getName().startsWith("Lib")) {
            return false;
        }
        return "*".equals(solName) || solFile.getName().equals(solName);
    }
}
