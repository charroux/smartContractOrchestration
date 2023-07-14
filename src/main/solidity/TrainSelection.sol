// SPDX-License-Identifier: MIT
pragma solidity ^0.6.5;
pragma experimental ABIEncoderV2;


contract SelecTrainSmartContrat {

    struct Train {
        uint number;
        string destination;
        uint effectiveDepartureDate;
        uint effectiveArrivalDate;
        uint ticketPrice;
    }

    event bookingNumber(uint number);

    Train[] trains;

    Train selectedTrain;

    constructor() public {
        trains.push(Train(1, "Paris", 1693087200000, 1693346400000, 100));              // 27-08-2023, 30-08-2023
        trains.push(Train(2, "Paris", 1693173600000, 1693346400000, 150));             // 28-08-2023, 30-08-2023
        trains.push(Train(2, "Nice", 1693173600000, 1693346400000, 150));              // 28-08-2023, 30-08-2023
    }

   //function getTrain(string calldata destination, uint dateDeparture) public returns(uint) {
    function getTrain(string calldata destination, uint dateDeparture) public {
        uint length = trains.length;
        for(uint256 i=0; i< length; i++){
            if(keccak256(abi.encodePacked(trains[i].destination)) == keccak256(abi.encodePacked(destination))) {
                if( trains[i].effectiveDepartureDate >=  dateDeparture ) {
                    selectedTrain = trains[i];
                    emit bookingNumber(1);
                    return;
                }
            }
        }
        revert("No train found");
    }

    function getTrainNumber(uint bookingNumber) public view returns(uint) {
        return selectedTrain.number;
    }

    function getDestination(uint bookingNumber) public view returns(string memory) {
        return selectedTrain.destination;
    }

    function getTrainDepartureDate(uint bookingNumber) public view returns(uint) {
        return selectedTrain.effectiveDepartureDate;
    }

    function getTrainArrivalDate(uint bookingNumber) public view returns(uint) {
        return selectedTrain.effectiveArrivalDate;
    }

    function getTicketPrice(uint bookingNumber) public view returns(uint) {
        return selectedTrain.ticketPrice;
    }

    function pay(uint bookingNumber) public {
        // ...
    }

    function cancel(uint bookingNumber) public {
        // ...
    }
}