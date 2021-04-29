package com.webank.solc.plugin.handler;

import com.webank.solc.plugin.compiler.CompileSolToJava;
import com.webank.solc.plugin.config.SolidityCompileExtensions;
import com.webank.solc.plugin.enums.SolcVersionEnum;
import org.apache.commons.lang3.StringUtils;
import org.fisco.solc.compiler.SolidityCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.fisco.solc.compiler.SolidityCompiler.Options.*;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

/**
 * @author aaronchu
 * @Description
 * @data 2021/04/29
 */

public class SolcCompileTest {

    private File helloworld4 = new File("src/test/resources/HelloWorldV4.sol");
    private File helloworld5 = new File("src/test/resources/HelloWorldV5.sol");
    private File helloworld6 = new File("src/test/resources/HelloWorldV6.sol");
    @Test
    public void testV4() throws Exception{
        SolidityCompiler.Result smRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v4).compile(helloworld4, true, true, ABI, BIN, INTERFACE, METADATA);
        SolidityCompiler.Result eccRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v4).compile(helloworld4, false, true, ABI, BIN, INTERFACE, METADATA);
        Assert.assertTrue(!smRes.isFailed());
        Assert.assertTrue(!eccRes.isFailed());
    }

    @Test
    public void testV5() throws Exception{
        SolidityCompiler.Result smRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v5).compile(helloworld5, true, true, ABI, BIN, INTERFACE, METADATA);
        SolidityCompiler.Result eccRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v5).compile(helloworld5, false, true, ABI, BIN, INTERFACE, METADATA);
        Assert.assertTrue(!smRes.isFailed());
        Assert.assertTrue(!eccRes.isFailed());
    }

    @Test
    public void testV6() throws Exception{
        SolidityCompiler.Result smRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v6).compile(helloworld6, true, true, ABI, BIN, INTERFACE, METADATA);
        SolidityCompiler.Result eccRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v6).compile(helloworld6, false, true, ABI, BIN, INTERFACE, METADATA);
        Assert.assertTrue(!smRes.isFailed());
        Assert.assertTrue(!eccRes.isFailed());
    }

    @Test
    public void testSelector() throws Exception{
        File projectDir = new File(System.getProperty("user.dir"));
        SolidityCompileExtensions extensions = new SolidityCompileExtensions();
        extensions.setContracts("src/test/resources");
        extensions.setSelector("*");
        extensions.setOnlyAbiBin(true);
        extensions.setVersion("v4");
        extensions.setOutput("tmp");
        extensions.setSelector("HelloWorldV4");

        CompileHandler compileHandler = new CompileHandler(projectDir, extensions);
        compileHandler.doSolc();
    }
}
