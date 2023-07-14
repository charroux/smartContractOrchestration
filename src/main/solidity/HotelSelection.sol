// SPDX-License-Identifier: MIT
pragma solidity ^0.6.5;
pragma experimental ABIEncoderV2;


contract HotelSelectionSmartContrat {

    struct Hotel {
        string hotelAddress;
        uint roomPrice;
    }

    Hotel[] hotels;

    Hotel selectedHotel;

    event bookingNumber(uint number);

    constructor() public {
        hotels.push(Hotel("Paris", 200));
        hotels.push(Hotel("Marseille", 100));
    }

    function getRoomInHotel(string calldata destination, uint arrivalDate) public {
        uint length = hotels.length;
        for(uint256 i=0; i< length; i++){
            if(keccak256(abi.encodePacked(hotels[i].hotelAddress)) == keccak256(abi.encodePacked(destination))) {
                selectedHotel = hotels[i];
                emit bookingNumber(1);
                return;
            }
        }
        revert("No hotel found");
    }

    function getRoomNumber(uint bookingNumber) public view returns(uint) {
        return 101;
    }

    function getPrice(uint bookingNumber) public view returns(uint) {
        return selectedHotel.roomPrice;
    }

    function getAddress() public view returns(string memory) {
        return selectedHotel.hotelAddress;
    }

    function pay(uint bookingNumber) public {
        // ...
    }

    function cancel(uint bookingNumber) public {
        // ...
    }

}