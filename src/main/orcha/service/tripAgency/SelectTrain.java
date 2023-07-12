package service.tripAgency;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.5.0.
 */
@SuppressWarnings("rawtypes")
public class SelectTrain extends Contract {
    public static final String BINARY = "6080604052600060015534801561001557600080fd5b5061002a60016012601461004360201b60201c565b61003e60026013601761004360201b60201c565b610131565b6040518060600160405280848152602001838152602001828152506000806001548152602001908152602001600020600082015181600001556020820151816001015560408201518160020155905050600160008154809291906100a6906100e9565b9190505550505050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000819050919050565b60006100f4826100df565b91507f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8203610126576101256100b0565b5b600182019050919050565b6103bc806101406000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c80632deafbc31461003b578063e0f3e62a1461006b575b600080fd5b610055600480360381019061005091906101de565b610087565b6040516100629190610275565b60405180910390f35b610085600480360381019061008091906102bc565b610115565b005b61008f610182565b60005b60015481121561010e578260008083815260200190815260200160002060010154106100fb576000808281526020019081526020016000206040518060600160405290816000820154815260200160018201548152602001600282015481525050915050610110565b80806101069061033e565b915050610092565b505b919050565b6040518060600160405280848152602001838152602001828152506000806001548152602001908152602001600020600082015181600001556020820151816001015560408201518160020155905050600160008154809291906101789061033e565b9190505550505050565b60405180606001604052806000815260200160008152602001600081525090565b600080fd5b6000819050919050565b6101bb816101a8565b81146101c657600080fd5b50565b6000813590506101d8816101b2565b92915050565b6000602082840312156101f4576101f36101a3565b5b6000610202848285016101c9565b91505092915050565b6000819050919050565b61021e8161020b565b82525050565b61022d816101a8565b82525050565b6060820160008201516102496000850182610215565b50602082015161025c6020850182610224565b50604082015161026f6040850182610224565b50505050565b600060608201905061028a6000830184610233565b92915050565b6102998161020b565b81146102a457600080fd5b50565b6000813590506102b681610290565b92915050565b6000806000606084860312156102d5576102d46101a3565b5b60006102e3868287016102a7565b93505060206102f4868287016101c9565b9250506040610305868287016101c9565b9150509250925092565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b60006103498261020b565b91507f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff820361037b5761037a61030f565b5b60018201905091905056fea264697066735822122072d580bfe834004ecf3978e5885ac63259aa99dadcb8d031817c4d3f65eea9e864736f6c63430008120033";

    public static final String FUNC_CREATETRAIN = "CreateTrain";

    public static final String FUNC_GETTRAIN = "getTrain";

    @Deprecated
    protected SelectTrain(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SelectTrain(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SelectTrain(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SelectTrain(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> CreateTrain(BigInteger trainNumber, BigInteger dateDeparture, BigInteger dateArrival) {
        final Function function = new Function(
                FUNC_CREATETRAIN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Int256(trainNumber), 
                new org.web3j.abi.datatypes.generated.Uint256(dateDeparture), 
                new org.web3j.abi.datatypes.generated.Uint256(dateArrival)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<SelectedTrain> getTrain(BigInteger dateP) {
        final Function function = new Function(FUNC_GETTRAIN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(dateP)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<SelectedTrain>() {}));
        return executeRemoteCallSingleValueReturn(function, SelectedTrain.class);
    }

    @Deprecated
    public static SelectTrain load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SelectTrain(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SelectTrain load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SelectTrain(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SelectTrain load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SelectTrain(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SelectTrain load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SelectTrain(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<SelectTrain> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SelectTrain.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<SelectTrain> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SelectTrain.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<SelectTrain> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SelectTrain.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<SelectTrain> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SelectTrain.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class SelectedTrain extends StaticStruct {
        public BigInteger number;

        public BigInteger effectiveDepartureDate;

        public BigInteger effectiveArrivalDate;

        public SelectedTrain(BigInteger number, BigInteger effectiveDepartureDate, BigInteger effectiveArrivalDate) {
            super(new org.web3j.abi.datatypes.generated.Int256(number), 
                    new org.web3j.abi.datatypes.generated.Uint256(effectiveDepartureDate), 
                    new org.web3j.abi.datatypes.generated.Uint256(effectiveArrivalDate));
            this.number = number;
            this.effectiveDepartureDate = effectiveDepartureDate;
            this.effectiveArrivalDate = effectiveArrivalDate;
        }

        public SelectedTrain(Int256 number, Uint256 effectiveDepartureDate, Uint256 effectiveArrivalDate) {
            super(number, effectiveDepartureDate, effectiveArrivalDate);
            this.number = number.getValue();
            this.effectiveDepartureDate = effectiveDepartureDate.getValue();
            this.effectiveArrivalDate = effectiveArrivalDate.getValue();
        }
    }
}
