package service.tripAgency

import configuration.tripAgency.TripInfo
import groovy.util.logging.Slf4j
import org.web3j.crypto.Credentials
import org.web3j.hotelselectionsmartcontrat.HotelSelectionSmartContrat
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.selectrainsmartcontrat.SelecTrainSmartContrat
import org.web3j.taxiselectionsmartcontrat.TaxiSelectionSmartContrat
import org.web3j.tx.gas.DefaultGasProvider

@Slf4j
class PaymentService {

	TripInfo pay(TripInfo tripInfo){

		log.info "Receives: tripInfo " + tripInfo

		try {

			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);

			String selectedTrainContractAddress = tripInfo.selectedTrain.selectedTrainContractAddress

			SelecTrainSmartContrat selecTrainSmartContract = SelecTrainSmartContrat.load(
					selectedTrainContractAddress,
					web3j,
					Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"),
					new DefaultGasProvider());

			log.info("Train smart contract loaded at address: " + selecTrainSmartContract.getContractAddress());

			String selectedHotelContractAddress = tripInfo.selectedHotel.selectedHotelContractAddress

			HotelSelectionSmartContrat hotelSelectionSmartContrat = HotelSelectionSmartContrat.load(
					selectedHotelContractAddress,
					web3j,
					Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"),
					new DefaultGasProvider());

			log.info("Hotel smart contract loaded at address: " + hotelSelectionSmartContrat.getContractAddress());

			String selectedTaxiContractAddress = tripInfo.selectedTaxi.selectedTaxiContractAddress

			TaxiSelectionSmartContrat taxiSelectionSmartContrat = TaxiSelectionSmartContrat.load(
					selectedTaxiContractAddress,
					web3j,
					Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"),
					new DefaultGasProvider());

			log.info("Taxi smart contract loaded at address: " + taxiSelectionSmartContrat.getContractAddress());

			if(tripInfo.creditBalance >= 0) {
				log.info("Credit balance = " + tripInfo.creditBalance + " => payment for train, hotel and taxi.");
				selecTrainSmartContract.pay(tripInfo.selectedTrain.bookingNumber);
				hotelSelectionSmartContrat.pay(tripInfo.selectedHotel.bookingNumber);
				taxiSelectionSmartContrat.pay(tripInfo.selectedTaxi.bookingNumber);
				tripInfo.booking = "BOOKED"
			} else {
				log.info("Credit balance = " + tripInfo.creditBalance + " => cancel booking for train, hotel and taxi.");
				selecTrainSmartContract.cancel(tripInfo.selectedTrain.bookingNumber);
				hotelSelectionSmartContrat.cancel(tripInfo.selectedHotel.bookingNumber);
				taxiSelectionSmartContrat.cancel(tripInfo.selectedTaxi.bookingNumber);
				tripInfo.booking = "CANCELED"
			}

			return tripInfo;

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
