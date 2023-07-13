package service.tripAgency

import configuration.tripAgency.TripInfo
import groovy.util.logging.Slf4j
import org.web3j.crypto.Credentials
import org.web3j.hotelselectionsmartcontrat.HotelSelectionSmartContrat
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.selectrainsmartcontrat.SelecTrainSmartContrat
import org.web3j.tx.gas.DefaultGasProvider

@Slf4j
class HotelSelection {

	String smartContractAddress;

	public HotelSelection() {
		try {
			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);
			HotelSelectionSmartContrat selecHotelSmartContrat = HotelSelectionSmartContrat.deploy(web3j, Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"), new DefaultGasProvider()).send();
			smartContractAddress = selecHotelSmartContrat.getContractAddress();
			log.info("deployed at: " + smartContractAddress);
			web3j.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	TripInfo select(TripInfo tripInfo){

		log.info "receives : " + tripInfo

		try {
			Web3j web3j = Web3j.build(new HttpService("HTTP://127.0.0.1:7545"));
			log.info("web3j = " + web3j);
			HotelSelectionSmartContrat selecHotelSmartContrat = HotelSelectionSmartContrat.load(smartContractAddress, web3j, Credentials.create("0xba915e64f14ff363abf52193444c30ae0cd2963034dc8a2448f02b95b33702f5"), new DefaultGasProvider());
			log.info("smart contract address: " + selecHotelSmartContrat.getContractAddress());
			BigInteger date = BigInteger.valueOf(tripInfo.getSelectedTrain().getArrival().getTime());
			selecHotelSmartContrat.getRoomInHotel(tripInfo.getSelectedTrain().getDestination(), date).send();
			log.info("getRoomInHotel");
			String address = selecHotelSmartContrat.getAddress().send();
			log.info("hotel address: " + address);
			BigInteger roomNumber = selecHotelSmartContrat.getRoomNumber().send();
			log.info("room number: " + roomNumber);
			BigInteger price = selecHotelSmartContrat.getPrice().send();
			log.info("price: " + price);

			web3j.shutdown();

			SelectedHotel selectedHotel = new SelectedHotel(
					address: address,
					roomNumber: roomNumber,
					arrival: new Date(date.longValue()),
					roomPrice: price,
					selectedHotelContractAddress: selecHotelSmartContrat.getContractAddress())

			log.info("Selected hotel: " + selectedHotel)

			tripInfo.creditBalance = tripInfo.creditBalance - price
			log.info("Credit balance: " + tripInfo.creditBalance);
			tripInfo.billableAmount = tripInfo.billableAmount + price
			log.info("Billable amount: " + tripInfo.billableAmount);

			tripInfo.selectedHotel = selectedHotel
			log.info("Trip info: " + tripInfo)

			return tripInfo;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
