package service.tripAgency

import configuration.tripAgency.TripInfo
import groovy.util.logging.Slf4j
import org.web3j.crypto.Credentials
import org.web3j.hotelselectionsmartcontrat.HotelSelectionSmartContrat
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.taxiselectionsmartcontrat.TaxiSelectionSmartContrat
import org.web3j.tx.gas.DefaultGasProvider

@Slf4j
class TaxiSelection {

	String smartContractAddress;

	public TaxiSelection() {
		try {
			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);
			TaxiSelectionSmartContrat taxiSelectionSmartContrat = TaxiSelectionSmartContrat.deploy(web3j, Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"), new DefaultGasProvider()).send();
			smartContractAddress = taxiSelectionSmartContrat.getContractAddress();
			log.info("deployed at: " + smartContractAddress);
			web3j.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	TripInfo select(def travelInfo){

		log.info "receives : train " + travelInfo[0] + ", hotel " + travelInfo[1]
		TripInfo tripInfoFromTrain = null
		if(travelInfo[0] instanceof TripInfo) {
			tripInfoFromTrain = travelInfo[0]
		}
		TripInfo tripInfoFromHotel = null
		if(travelInfo[1] instanceof TripInfo) {
			tripInfoFromHotel = travelInfo[1]
		}


		try {
			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);
			TaxiSelectionSmartContrat taxiSelectionSmartContrat = TaxiSelectionSmartContrat.load(smartContractAddress, web3j, Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"), new DefaultGasProvider());
			log.info("smart contract address: " + taxiSelectionSmartContrat.getContractAddress());
			BigInteger date = BigInteger.valueOf(tripInfoFromHotel.selectedTrain.arrival.getTime());
			taxiSelectionSmartContrat.getTaxi(
					tripInfoFromHotel.selectedTrain.destination, 	// departure of the taxi = destination if the train
					date, 											// departure date of the taxi = arrival date of the train
					tripInfoFromHotel.selectedHotel.address 		// destination of the taxi = address of the hotel
			).send();
			log.info("getTaxi");
			String taxiFrom = taxiSelectionSmartContrat.getTaxiFrom().send();
			log.info("taxi from: " + taxiFrom);
			BigInteger departureDate = taxiSelectionSmartContrat.getDepartureDate().send();
			log.info("taxi departure date: " + departureDate);
			String taxiTo = taxiSelectionSmartContrat.getTaxiTo().send();
			log.info("taxi to: " + taxiTo);
			int number = taxiSelectionSmartContrat.getNumber().send();
			log.info("taxi number: " + number);
			int price = taxiSelectionSmartContrat.getPrice().send();
			log.info("price: " + price);

			web3j.shutdown();

			SelectedTaxi selectedTaxi = new SelectedTaxi(
				number: number,
				departureLocation: taxiFrom,
				departureDate: new Date(departureDate.longValue()),
				arrivalLocation: taxiTo,
				price: price
			)

			log.info("Selected taxi: " + selectedTaxi)

			tripInfoFromHotel.creditBalance = tripInfoFromHotel.creditBalance - price
			log.info("Credit balance: " + tripInfoFromHotel.creditBalance);
			tripInfoFromHotel.billableAmount = tripInfoFromHotel.billableAmount + price
			log.info("Billable amount: " + tripInfoFromHotel.billableAmount);

			tripInfoFromHotel.selectedTaxi = selectedTaxi
			log.info("Trip info: " + tripInfoFromHotel)

			return tripInfoFromHotel;

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
