package com.webank.solc.plugin;

import com.webank.solc.plugin.config.SolidityCompileExtensions;
import com.webank.solc.plugin.task.CompileTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author aaronchu
 * @Description
 * @data 2020/06/25
 */
public class SolcPlugin implements Plugin<Project> {
    

    @Override
    public void apply(Project project) {
        //Confuration
        project.getExtensions().create("solc", SolidityCompileExtensions.class);
        //Task
        project.getTasks().create("solc", CompileTask.class);
    }

    //call solc.exe
}
