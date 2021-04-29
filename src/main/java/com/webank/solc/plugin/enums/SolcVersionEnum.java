package com.webank.solc.plugin.enums;

/**
 * @author aaronchu
 * @Description
 * @data 2021/04/29
 */
public enum SolcVersionEnum {

    v4("0.4.25","solcJ-0.4.25.1.jar"),

    v5("0.5.2","solcJ-0.5.2.0.jar"),

    v6("0.6.10","solcJ-0.6.10.0.jar");

    private String version;
    private String jarName;

    SolcVersionEnum(String version, String jarName){
        this.version = version;
        this.jarName = jarName;
    }

    public String getJarName() {
        return jarName;
    }

    public String getVersion() {
        return version;
    }
}
