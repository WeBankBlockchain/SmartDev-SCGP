package com.webank.solc.plugin;

/**
 * @author aaronchu
 * @Description
 * @data 2020/06/25
 */
public class SolidityCompileExtensions {

    //Required
    private String pkg;

    //Not required
    private String output = "src/main";

    //Not required
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
}
