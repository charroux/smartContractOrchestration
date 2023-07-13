// SPDX-License-Identifier: MIT
pragma solidity ^0.6.5;
pragma experimental ABIEncoderV2;


contract TaxiSelectionSmartContrat {

    struct Taxi {
        uint number;
        string departureLocation;
        uint departureDate;
        string arrivalLocation;
        uint price;
    }

    Taxi selectedTaxi;

    function getTaxi(string calldata depertureLocation, uint departureDate, string calldata arrivalLocation) public {
        selectedTaxi = Taxi(114, depertureLocation, departureDate, arrivalLocation, 100);
    }

    function getTaxiFrom() public view returns(string memory) {
        return selectedTaxi.departureLocation;
    }

    function getDepartureDate() public view returns(uint) {
        return selectedTaxi.departureDate;
    }

    function getTaxiTo() public view returns(string memory) {
        return selectedTaxi.arrivalLocation;
    }

    function getNumber() public view returns(uint) {
        return selectedTaxi.number;
    }

    function getPrice() public view returns(uint) {
        return selectedTaxi.price;
    }


}