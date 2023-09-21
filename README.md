# Smart Contracts orchestration with Orcha

## Orcha program

https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/source/tripAgency/Travel.groovy

## Orcha configuration of the Orcha program

https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/configuration/tripAgency/OrganizeTripConfiguration.groovy

### Configuration 

'''
@Bean
EventHandler tripAgency(){
def eventHandler = new EventHandler(name: "tripAgency")
def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "tripInfo.json")
eventHandler.input = new Input(mimeType: "application/json", type: "java.util.List", adapter: fileAdapter)
return eventHandler
}
'''

## Services called by the Orcha program

https://github.com/charroux/smartContractOrchestration/tree/main/src/main/orcha/service/tripAgency

Train selection service colling the train selection smart contract: https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/service/tripAgency/TrainSelection.groovy

Train selection smart contract: https://github.com/charroux/smartContractOrchestration/blob/main/src/main/solidity/TrainSelection.sol

Hotel selection service colling the hotel selection smart contract: https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/service/tripAgency/HotelSelection.groovy

Hotel selection smart contract: https://github.com/charroux/smartContractOrchestration/blob/main/src/main/solidity/HotelSelection.sol

Taxi selection service colling the taxi selection smart contract: https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/service/tripAgency/TaxiSelection.groovy

Taxi selection smart contract: https://github.com/charroux/smartContractOrchestration/blob/main/src/main/solidity/TaxiSelection.sol

## Transpilation of the Orcha program

The Orcha program need to be transpiled before processed. The transpilation leads to an XML file compatible with Spring integration: https://github.com/charroux/smartContractOrchestration/blob/main/src/main/resources/organize%20trip.xml

## Test

Tested with Ganache with Gas limit extended to 9999999.
