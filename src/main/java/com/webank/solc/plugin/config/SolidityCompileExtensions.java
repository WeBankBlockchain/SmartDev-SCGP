package com.webank.solc.plugin.config;

/**
 * @author aaronchu
 * @Description
 * @data 2020/06/25
 */
public class SolidityCompileExtensions {

    private String pkg;

    private String output = "src/main";

    private boolean onlyAbiBin = false;

    private String version = "v4";

    private String contracts = "src/main/contracts";

    public String getContracts() {
        return contracts;
    }

    public void setContracts(String contracts) {
        this.contracts = contracts;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isOnlyAbiBin() {
        return onlyAbiBin;
    }

    public void setOnlyAbiBin(boolean onlyAbiBin) {
        this.onlyAbiBin = onlyAbiBin;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
