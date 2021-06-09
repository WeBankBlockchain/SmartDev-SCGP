package com.webank.solc.plugin.compiler;

import com.webank.solc.plugin.handler.SolcHandler;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.codegen.SolidityContractGenerator;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fisco.solc.compiler.SolidityCompiler.Options.*;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

/**
 * @author aaronchu
 * @Description
 * @data 2020/06/25
 */
public class CompileSolToJava {

    public void compileSolToJava(
            String solSelector,
            String packageName,
            File solFileList,
            File abiOutputDir,
            File binOutputDir,
            File smbinOutputDir,
            File javaOutputDir
            )
            throws Exception {
        preConditions(abiOutputDir, binOutputDir, smbinOutputDir, javaOutputDir);
        File[] solFiles = solFileList.listFiles();
        if (solFiles.length == 0) {
            System.out.println("The contracts directory is empty.");
            return;
        }
        for (File solFile : solFiles) {
            //Verify
            if(!verifySolfile(solFile, solSelector)){
                continue;
            }
            //Abi and Bin(ecdsa + gm)
            String contractName = solFile.getName().split("\\.")[0];
            AbiAndBin abiAndBin = this.compileSolToBinAndAbi(solFile);
            if(abiAndBin == null){
                continue;
            }
            this.saveAbiAndBin(abiAndBin, contractName, abiOutputDir, binOutputDir, smbinOutputDir);
            //Java
            if(javaOutputDir == null) continue;
            File abiFile = new File(abiOutputDir,contractName + ".abi");
            File binFile = new File(binOutputDir,contractName + ".bin");
            File smbinFile = new File(smbinOutputDir,contractName + ".bin");;
            SolidityContractGenerator scg = new SolidityContractGenerator(binFile, smbinFile, abiFile, javaOutputDir, packageName);
            scg.generateJavaFiles();
        }
    }

    private void preConditions(File abiDir, File binDir, File smbinDir, File javaDir) {
        abiDir.mkdirs();
        binDir.mkdirs();
        smbinDir.mkdirs();
        if(javaDir != null){
            javaDir.mkdirs();
        }
    }

    private void saveAbiAndBin(AbiAndBin abiAndBin, String contractname, File abiDir, File binDir, File smbinDir) throws IOException{
        FileUtils.writeStringToFile(new File(abiDir, contractname + ".abi"), abiAndBin.getAbi());
        FileUtils.writeStringToFile(new File(binDir ,contractname + ".bin"), abiAndBin.getBin());
        FileUtils.writeStringToFile(new File(smbinDir ,contractname + ".bin"), abiAndBin.getSmBin());
    }

    private AbiAndBin compileSolToBinAndAbi(File contractFile) throws
            IOException {
        String contractName = contractFile.getName().split("\\.")[0];

        /** ecdsa compile */
        SolidityCompiler.Result res =
                SolcHandler.buildSolidityCompiler().compile(contractFile, false, true, ABI, BIN, INTERFACE, METADATA);
        if (res.isFailed() || "".equals(res.getOutput())) {
            System.out.println(" Compile error: " + res.getErrors());
            return null;
        }
        /** sm compile */
        SolidityCompiler.Result smRes =
                SolcHandler.buildSolidityCompiler().compile(contractFile, true, true, ABI, BIN, INTERFACE, METADATA);
        if (smRes.isFailed() || "".equals(smRes.getOutput())) {
            System.out.println(" Compile SM error: " + smRes.getErrors());
        }

        CompilationResult result = CompilationResult.parse(res.getOutput());
        CompilationResult smResult = CompilationResult.parse(smRes.getOutput());

        CompilationResult.ContractMetadata meta = result.getContract(contractName);
        CompilationResult.ContractMetadata smMeta = smResult.getContract(contractName);
        return new AbiAndBin(meta.abi, meta.bin, smMeta.bin);
    }

    private boolean verifySolfile(File solFile, String solSelector){
        if (!solFile.getName().endsWith(".sol")) {
            return false;
        }
        if (solFile.getName().startsWith("Lib")) {
            return false;
        }
        if(solSelector == null || solSelector.isEmpty()){
            return true;
        }
        Set<String> solNames = Arrays.stream(solSelector.split(",|;"))
                .map(s-> s.endsWith(".sol")?s:s+".sol")
                .collect(Collectors.toSet());
        return solNames.contains(solFile.getName());
    }
}
