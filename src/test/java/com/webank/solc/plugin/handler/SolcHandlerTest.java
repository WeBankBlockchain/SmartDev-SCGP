package com.webank.solc.plugin.handler;

import com.webank.solc.plugin.enums.SolcVersionEnum;
import org.fisco.solc.compiler.SolidityCompiler;
import org.junit.Test;

import java.io.File;

import static org.fisco.solc.compiler.SolidityCompiler.Options.*;
import static org.fisco.solc.compiler.SolidityCompiler.Options.METADATA;

/**
 * @author aaronchu
 * @Description
 * @data 2021/04/29
 */

public class SolcHandlerTest {

    private File helloworld = new File("src/test/resources/HelloWorld.sol");
    @Test
    public void testV4() throws Exception{
        SolidityCompiler.Result smRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v4).compile(helloworld, true, true, ABI, BIN, INTERFACE, METADATA);
        SolidityCompiler.Result eccRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v4).compile(helloworld, false, true, ABI, BIN, INTERFACE, METADATA);

    }

    @Test
    public void testGmV5() throws Exception{
        SolidityCompiler.Result smRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v5).compile(helloworld, true, true, ABI, BIN, INTERFACE, METADATA);
    }

    @Test
    public void testGmV6() throws Exception{
        SolidityCompiler.Result smRes =
                SolcHandler.buildSolidityCompiler(SolcVersionEnum.v5).compile(helloworld, true, true, ABI, BIN, INTERFACE, METADATA);
    }
}
