package service.tripAgency

import configuration.tripAgency.TripInfo
import groovy.util.logging.Slf4j
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.selectrainsmartcontrat.SelecTrainSmartContrat
import org.web3j.tx.gas.DefaultGasProvider

@Slf4j
class TrainSelection {

	/*service.tripAgency.SelectedTrain select(TripInfo tripInfo) {
		log.info "-----------------receives : " + tripInfo
		CompletableFuture<service.tripAgency.SelectedTrain> future = CompletableFuture.supplyAsync(new Supplier<service.tripAgency.SelectedTrain>() {
			@Override
			public service.tripAgency.SelectedTrain get() {
				Thread.sleep(500);
				log.info "-----------------task completed"
				return new service.tripAgency.SelectedTrain(number: 1234, passenger: tripInfo.passenger, departure: tripInfo.departure, arrival: tripInfo.arrival)
			}
		});
		log.info "------------------ wait for response"
		SelectedTrain selectedTrain =  future.get();
		log.info "------------------ returns selected train: " + selectedTrain
		return selectedTrain
	}*/



	/*service.tripAgency.SelectedTrain select(TripInfo tripInfo) {
		log.info "Receives : " + tripInfo
		CompletableFuture<service.tripAgency.SelectedTrain> future = CompletableFuture.supplyAsync(new TrainSupplier(tripInfo))
		log.info "Wait for selected train"
		SelectedTrain selectedTrain =  future.get();
		log.info "Returns selected train: " + selectedTrain
		return selectedTrain
	}*/

	SelectedTrain select(TripInfo tripInfo) {

		log.info "Receives : " + tripInfo

		try {
			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);
			SelecTrainSmartContrat selecTrainSmartContract = SelecTrainSmartContrat.deploy(web3j, Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"), new DefaultGasProvider()).send();
			log.info("deployed at: " + selecTrainSmartContract.getContractAddress());
			selecTrainSmartContract.getTrain(BigInteger.valueOf(19)).send();
			log.info("getTrain");
			BigInteger trainNumber = selecTrainSmartContract.getTrainNumber().send();
			log.info("trainNumber: " + trainNumber);
			BigInteger departureDate = selecTrainSmartContract.getTrainDepartureDate().send();
			log.info("departureDate: " + departureDate);
			BigInteger arrivalDate = selecTrainSmartContract.getTrainArrivalDate().send();
			log.info("arrivalDate: " + arrivalDate);
			SelectedTrain selectedTrain = new SelectedTrain();
			selectedTrain.number = trainNumber;
			selectedTrain.arrival = tripInfo.arrival;
			selectedTrain.departure = tripInfo.departure;
			selectedTrain.passenger = tripInfo.passenger;
			return selectedTrain;


			//SelecTrainSmartContrat.Train train = selecTrainSmartContract.getTrain(BigInteger.valueOf(19)).send();
			//log.info("selectedTrain: + " + train.number + ", " + train.effectiveDepartureDate + ", " + train.effectiveArrivalDate);
			web3j.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}



		return null;

		/*try{

			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);
			org.web3j.selectrain.SelecTrain selecTrainSmartContact = org.web3j.selectrain.SelecTrain.deploy(web3j, Credentials.create("0xc122d38143f971505a0c72ceae1c07c486d69a094500a16c10f4d2b19d1708eb"), new DefaultGasProvider()).send();
			log.info("deployed at: " + selecTrainSmartContact.getContractAddress());
			org.web3j.selectrain.SelecTrain.SelectedTrain train = selecTrainSmartContact.getTrain(BigInteger.valueOf(19)).send();
			log.info("selectedTrain: + " + train.number + ", " + train.effectiveDepartureDate + ", " + train.effectiveArrivalDate);
			service.tripAgency.SelectedTrain selectedTrain1 = new service.tripAgency.SelectedTrain();
			selectedTrain1.number = train.number;
			selectedTrain1.departure = tripInfo.departure;
			selectedTrain1.arrival = tripInfo.arrival;
			selectedTrain1.passenger = tripInfo.passenger;
			return selectedTrain1;
			web3j.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;*/

	}
}
