package service.tripAgency

import configuration.tripAgency.TripInfo
import groovy.util.logging.Slf4j
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.Supplier

@Slf4j
class TrainSupplier implements Supplier<SelectedTrain>{

	TripInfo tripInfo

	TrainSupplier(TripInfo tripInfo) {
		this.tripInfo = tripInfo
	}

	@Override
	SelectedTrain get() {
		Thread.sleep(500);
		SelectedTrain selectedTrain = new SelectedTrain(number: 1234, passenger: tripInfo.passenger, departure: tripInfo.departure, arrival: tripInfo.arrival)
		log.info "Supply selected train: " + selectedTrain
		return selectedTrain
	}

}

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

	service.tripAgency.SelectedTrain select(TripInfo tripInfo) {

		log.info "Receives : " + tripInfo

		try{


			Web3j web3 = Web3j.build(new HttpService("http://127.0.0.1:8545"));

			log.info "web3 : " + web3

			SelectTrain contract = SelectTrain.load(
					"0xD27764C82Cd05d7e405E2d7513c7a5a2e1705595", web3);

			log.info "contract : " + contract

			TransactionReceipt transactionReceipt = contract.getTrain(19).send();

			log.info "transactionReceipt : " + transactionReceipt

			log.info "Latest Ethereum block number: " + transactionReceipt;


			return null;
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
}
