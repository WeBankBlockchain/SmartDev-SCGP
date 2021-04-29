package com.webank.solc.plugin;

import com.webank.solc.plugin.enums.SolcVersionEnum;
import com.webank.solc.plugin.handler.SolcHandler;
import org.fisco.solc.compiler.Solc;

/**
 * @author aaronchu
 * @Description
 * @data 2021/04/29
 */
public class Main {

    public static void main(String[] args){
        Solc solc =SolcHandler.createSolc(SolcVersionEnum.v4, false);
    }
}
