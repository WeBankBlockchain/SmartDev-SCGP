package com.webank.solc.plugin.handler;

import org.fisco.solc.compiler.SolidityCompiler;

/**
 * @author aaronchu
 * @Description
 * @data 2021/04/29
 */
public class SolcHandler {

    public static SolidityCompiler buildSolidityCompiler() {
        return SolidityCompiler.getInstance();
    }
}










