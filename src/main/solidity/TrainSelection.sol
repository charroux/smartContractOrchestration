// SPDX-License-Identifier: GPL-3.0

pragma solidity ^0.6.5;
pragma experimental ABIEncoderV2;

/**
 * @title Owner
 * @dev Set & change owner
 */
contract SelecTrainSmartContrat {

    struct Train {
        uint number;
        uint effectiveDepartureDate;
        uint effectiveArrivalDate;
    }

    Train[] trains;

    Train selectedTrain;

    constructor() public {
        trains.push(Train(1, 18, 20));
        trains.push(Train(2, 19, 20));
    }

   function getTrain(uint dateP) public {
        uint length = trains.length;
        for(uint256 i=0; i< length; i++){
            if( trains[i].effectiveDepartureDate >=  dateP ) {
               //return trains[i];
                selectedTrain = trains[i];
                return;
            }
        }
        revert("No train found");
    }

    function getTrainNumber() public view returns(uint) {
        return selectedTrain.number;
    }

    function getTrainDepartureDate() public view returns(uint) {
        return selectedTrain.effectiveDepartureDate;
    }

    function getTrainArrivalDate() public view returns(uint) {
        return selectedTrain.effectiveArrivalDate;
    }

}