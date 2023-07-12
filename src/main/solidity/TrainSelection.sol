// SPDX-License-Identifier: GPL-3.0

pragma solidity ^0.6.5;
pragma experimental ABIEncoderV2;

/**
 * @title Owner
 * @dev Set & change owner
 */
contract SelecTrain {

    struct SelectedTrain {
        int number;
        uint effectiveDepartureDate;
        uint effectiveArrivalDate;
    }

    SelectedTrain[] trains;

    constructor() public {
        trains.push(SelectedTrain(1, 18, 20));
        trains.push(SelectedTrain(1, 19, 20));
    }

    function getTrain(uint dateP) public view returns(SelectedTrain memory) {
        uint length = trains.length;
        for(uint256 i=0; i< length; i++){
            if( trains[i].effectiveDepartureDate >=  dateP ) {
                return trains[i];
            }
        }
        revert("No train found");
    }

}