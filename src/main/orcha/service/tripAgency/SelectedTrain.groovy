package service.tripAgency

import groovy.transform.ToString

/*
// SPDX-License-Identifier: GPL-3.0

pragma solidity >=0.7.0 <0.9.0;


contract SelecTrain {



	struct SelectedTrain {
		int number;
		uint effectiveDepartureDate;
		uint effectiveArrivalDate;
	}

	constructor()  {

		CreateTrain(1, 18, 20);
		CreateTrain(2, 19, 23);

	}

	mapping (int => SelectedTrain) TrainList;
	int train_Counter = 0;



	function CreateTrain (int trainNumber, uint dateDeparture, uint  dateArrival) public {

		TrainList[train_Counter]= SelectedTrain(trainNumber, dateDeparture, dateArrival);
		train_Counter ++;
	}


	function getTrain( uint dateP) public view returns(SelectedTrain memory train) {



		for(int i =0; i< train_Counter; i++){
			if( TrainList[i].effectiveDepartureDate >=  dateP ) {
				return TrainList[i];

			}

		}



	}


}
		*/
@ToString
class SelectedTrain {
	
	int number
	String destination
	Date departure
	Date arrival
	int ticketPrice
	String selectedTrainContractAddress

}
