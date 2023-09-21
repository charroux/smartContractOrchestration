package service.tripAgency

import configuration.tripAgency.TripInfo
import groovy.util.logging.Slf4j
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthFilter
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.selectrainsmartcontrat.SelecTrainSmartContrat
import org.web3j.tx.gas.DefaultGasProvider

import java.text.SimpleDateFormat

@Slf4j
class TrainSelection {

	String smartContractAddress;

	public TrainSelection() {
		try {
			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);
			SelecTrainSmartContrat selecTrainSmartContract = SelecTrainSmartContrat.deploy(web3j, Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"), new DefaultGasProvider()).send();
			smartContractAddress = selecTrainSmartContract.getContractAddress();
			log.info("deployed at: " + smartContractAddress);
			web3j.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	TripInfo select(TripInfo tripInfo) {

		log.info "Receives : " + tripInfo

		try {
			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);
			SelecTrainSmartContrat selecTrainSmartContract = SelecTrainSmartContrat.load(smartContractAddress, web3j, Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"), new DefaultGasProvider());
			log.info("smart contract address: " + selecTrainSmartContract.getContractAddress());
			TransactionReceipt transactionReceipt = selecTrainSmartContract.getTrain(tripInfo.travelDestination, BigInteger.valueOf(19)).send();
			org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST, selecTrainSmartContract.getContractAddress());
			BigInteger bookingNumber;
			web3j.ethLogFlowable(filter).subscribe(eventLog -> {
				String hexa = eventLog.getData(); 	// 0x00000000...2
				hexa = hexa.substring(2);	// skip the two first characteres
				bookingNumber = new BigInteger(hexa , 16);
				log.info("Event: " + bookingNumber);
			}, error -> {
				log.error(error);
			});
			log.info("getTrain returns booking number: " + bookingNumber);
			BigInteger trainNumber = selecTrainSmartContract.getTrainNumber(bookingNumber).send();
			log.info("trainNumber: " + trainNumber);
			BigInteger departureDate = selecTrainSmartContract.getTrainDepartureDate(bookingNumber).send();
			log.info("departureDate: " + departureDate);
			BigInteger arrivalDate = selecTrainSmartContract.getTrainArrivalDate(bookingNumber).send();
			log.info("arrivalDate: " + arrivalDate);
			String destination = selecTrainSmartContract.getDestination(bookingNumber).send();
			log.info("destination: " + destination);
			BigInteger ticketPrice = selecTrainSmartContract.getTicketPrice(bookingNumber).send();
			log.info("ticketPrice: " + ticketPrice);
			web3j.shutdown();

			int creditBalance = tripInfo.maximumPrice - ticketPrice;
			log.info("Credit Balance: " + creditBalance);

			SelectedTrain selectedTrain = new SelectedTrain()

			selectedTrain.bookingNumber = bookingNumber
			selectedTrain.number = trainNumber
			selectedTrain.destination = destination
			selectedTrain.departure = new Date(departureDate.longValue())
			selectedTrain.arrival = new Date(arrivalDate.longValue())
			selectedTrain.ticketPrice = ticketPrice;
			selectedTrain.selectedTrainContractAddress = selecTrainSmartContract.getContractAddress()

			log.info("Selected train: " + selectedTrain);

			tripInfo.billableAmount = ticketPrice
			log.info("Billable amount: " + tripInfo.billableAmount);

			tripInfo.creditBalance = creditBalance
			tripInfo.selectedTrain = selectedTrain

			log.info("Trip info: " + tripInfo);

			return tripInfo;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}
}
