package com.webank.solc.plugin.compiler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
class CompilationResult {

    @JsonProperty("contracts")
    private Map<String, ContractMetadata> contracts;

    @JsonProperty("version")
    public String version;

    @JsonIgnore
    public static CompilationResult parse(String rawJson) throws IOException {
        if (rawJson == null || rawJson.isEmpty()) {
            CompilationResult empty = new CompilationResult();
            empty.contracts = Collections.emptyMap();
            empty.version = "";

            return empty;
        } else {
            return ObjectMapperFactory.getObjectMapper()
                    .readValue(rawJson, CompilationResult.class);
        }
    }

    /** @return the contract's path given this compilation result contains exactly one contract */
    @JsonIgnore
    public Path getContractPath() {
        if (contracts.size() > 1) {
            throw new UnsupportedOperationException(
                    "Source contains more than 1 contact. Please specify the contract name. Available keys ("
                            + getContractKeys()
                            + ").");
        } else {
            String key = contracts.keySet().iterator().next();
            return Paths.get(key.substring(0, key.lastIndexOf(':')));
        }
    }

    /** @return the contract's name given this compilation result contains exactly one contract */
    @JsonIgnore
    public String getContractName() {
        if (contracts.size() > 1) {
            throw new UnsupportedOperationException(
                    "Source contains more than 1 contact. Please specify the contract name. Available keys ("
                            + getContractKeys()
                            + ").");
        } else {
            String key = contracts.keySet().iterator().next();
            return key.substring(key.lastIndexOf(':') + 1);
        }
    }

    /**
     * @param contractName The contract name
     * @return the first contract found for a given contract name; use {@link #getContract(Path,
     *     String)} if this compilation result contains more than one contract with the same name
     */
    @JsonIgnore
    public ContractMetadata getContract(String contractName) {
        if (contractName == null && contracts.size() == 1) {
            return contracts.values().iterator().next();
        } else if (contractName == null || contractName.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Source contains more than 1 contact. Please specify the contract name. Available keys ("
                            + getContractKeys()
                            + ").");
        }
        for (Map.Entry<String, CompilationResult.ContractMetadata> entry : contracts.entrySet()) {
            String key = entry.getKey();
            //key is in format "[filepath:contractname]"
            String name = key.substring(key.lastIndexOf(':') + 1);
            if (contractName.equals(name)) {
                return entry.getValue();
            }
        }
        throw new UnsupportedOperationException(
                "No contract found with name '"
                        + contractName
                        + "'. Please specify a valid contract name. Available keys ("
                        + getContractKeys()
                        + ").");
    }

    /**
     * @param contractPath The contract path
     * @param contractName The contract name
     * @return the contract with key {@code contractPath:contractName} if it exists; {@code null}
     *     otherwise
     */
    @JsonIgnore
    public CompilationResult.ContractMetadata getContract(Path contractPath, String contractName) {
        return contracts.get(contractPath.toAbsolutePath().toString() + ':' + contractName);
    }

    /** @return all contracts from this compilation result */
    @JsonIgnore
    public List<ContractMetadata> getContracts() {
        return new ArrayList<>(contracts.values());
    }

    /** @return all keys from this compilation result */
    @JsonIgnore
    public List<String> getContractKeys() {
        return new ArrayList<>(contracts.keySet());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContractMetadata {
        public String abi;
        public String bin;
        public String solInterface;
        public String metadata;

        public String getInterface() {
            return solInterface;
        }

        public void setInterface(String solInterface) {
            this.solInterface = solInterface;
        }
    }
}
