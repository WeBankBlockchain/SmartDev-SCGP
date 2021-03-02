package com.webank.solc.plugin.task;

import com.webank.solc.plugin.Cleaner;
import com.webank.solc.plugin.SolidityCompileExtensions;
import com.webank.solc.plugin.compiler.CompileSolToJava;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.lang.management.ManagementFactory;

/**
 * @author aaronchu
 * @Description
 * @data 2020/06/25
 */
public class CompileTask extends DefaultTask {
    @TaskAction
    public void solc(){
        System.out.println("Entering solc task, pid " + ManagementFactory.getRuntimeMXBean().getName());
        try{
            //Now call solc to generate contracts, and get
            doSolc();
            Cleaner.clean();
        }
        catch (Exception ex){
            System.out.println("Failed" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void doSolc() throws Exception{
        SolidityCompileExtensions extensions = getProject().getExtensions().findByType(SolidityCompileExtensions.class);
        if(extensions == null) extensions = new SolidityCompileExtensions();

        File contractsDir = new File(getProject().getProjectDir(), extensions.getContracts());
        if(!contractsDir.exists()){
            System.out.println("Solidity contracts dir not found "+contractsDir.getAbsolutePath());
            return;
        }


        CompileSolToJava solToJava = new CompileSolToJava();


        File outputBaseDir = new File(getProject().getProjectDir(), extensions.getOutput());//src/main
        String packageName = extensions.getPkg();
        File abiOutputDir = new File(outputBaseDir, "abi");
        File binOutputDir = new File(outputBaseDir, "bin/ecc");
        File smbinOutputDir = new File(outputBaseDir, "bin/sm");
        File javaOutputDir = extensions.isOnlyAbiBin()?null:new File(outputBaseDir, "java");

        solToJava.compileSolToJava("*", packageName, contractsDir, abiOutputDir, binOutputDir, smbinOutputDir, javaOutputDir);

        System.out.println("Solidity contracts compile complete ");
    }
}
