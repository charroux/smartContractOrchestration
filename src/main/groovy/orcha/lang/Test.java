package orcha.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;

/*
async function unregisterUSER()  {

const web3 = new Web3(window.ethereum);
var  contract_user= new web3.eth.Contract(userabi, useraddress);
const accounts= await  ethereum.request({ method: 'eth_requestAccounts' });
	let  id= localStorage.getItem("ID");
	id = Number(id.substring(1,id.length-1));
	console.log(id);
	// add variable ID , take it from ID user in top of the page
    const receipt = await contract_user.methods.unRegisteruser(id, accounts[0]).send( {from: accounts[0], gas: '1000000'},  function(error, resp){
	 if(error){
		bcblock.innerHTML +="<br>" + "UnRegistration Faild";
		bcblock.innerHTML +="<br>"+ JSON.stringify(receipt);
		return
	}
	console.log(resp);

	})
	bcblock.innerHTML +="<br>" + "************************";
	bcblock.innerHTML +="<b…
 */

public class Test {

    private static Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] arg) {
        Web3j web3 = Web3j.build(new HttpService("http://localhost:7545"));
        logger.info("Successfuly connected to Ethereum");

        try {
            // web3_clientVersion returns the current client version.
            Web3ClientVersion clientVersion = web3.web3ClientVersion().send();

            // eth_blockNumber returns the number of most recent block.
            EthBlockNumber blockNumber = web3.ethBlockNumber().send();

            // eth_gasPrice, returns the current price per gas in wei.
            EthGasPrice gasPrice = web3.ethGasPrice().send();

            // Print result
            logger.info("Client version: " + clientVersion.getWeb3ClientVersion());
            logger.info("Block number: " + blockNumber.getBlockNumber());
            logger.info("Gas price: " + gasPrice.getGasPrice());

        } catch (IOException ex) {
            throw new RuntimeException("Error whilst sending json-rpc requests", ex);
        }

    }

}
