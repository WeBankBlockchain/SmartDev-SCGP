package com.webank.solc.plugin.task;

import com.webank.solc.plugin.config.SolidityCompileExtensions;
import com.webank.solc.plugin.handler.CompileHandler;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Set;

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
            File projectDir = getProject().getProjectDir();

            SolidityCompileExtensions extensions = getProject().getExtensions().findByType(SolidityCompileExtensions.class);
            if(extensions == null) extensions = new SolidityCompileExtensions();

            new CompileHandler(projectDir, extensions).doSolc();
        }
        catch (Exception ex){
            System.out.println("Failed" + ex.getMessage());
            System.exit(-1);
        }
    }
}
