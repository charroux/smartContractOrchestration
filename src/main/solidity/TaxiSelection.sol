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

    event bookingNumber(uint number);

    function getTaxi(string calldata depertureLocation, uint departureDate, string calldata arrivalLocation) public {
        selectedTaxi = Taxi(114, depertureLocation, departureDate, arrivalLocation, 100);
        emit bookingNumber(1);
    }

    function getTaxiFrom(uint bookingNumber) public view returns(string memory) {
        return selectedTaxi.departureLocation;
    }

    function getDepartureDate(uint bookingNumber) public view returns(uint) {
        return selectedTaxi.departureDate;
    }

    function getTaxiTo(uint bookingNumber) public view returns(string memory) {
        return selectedTaxi.arrivalLocation;
    }

    function getNumber(uint bookingNumber) public view returns(uint) {
        return selectedTaxi.number;
    }

    function getPrice(uint bookingNumber) public view returns(uint) {
        return selectedTaxi.price;
    }

    function pay(uint bookingNumber) public {
        // ...
    }

    function cancel(uint bookingNumber) public {
        // ...
    }

}