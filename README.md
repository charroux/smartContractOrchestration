# Smart Contracts orchestration with Orcha

## Orcha program

https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/source/tripAgency/Travel.groovy

## Orcha configuration of the Orcha program

https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/configuration/tripAgency/OrganizeTripConfiguration.groovy

### Configuration of tripAgency the from instruction: receive tripInfo from tripAgency
```
@Bean
EventHandler tripAgency(){
def eventHandler = new EventHandler(name: "tripAgency")
def fileAdapter = new InputFileAdapter(directory: 'data/input', filenamePattern: "tripInfo.json")
eventHandler.input = new Input(mimeType: "application/json", type: "java.util.List", adapter: fileAdapter)
return eventHandler
}
```

### Configuration of selectATrain the from instruction: compute selectATrain with tripInfo.value
```
@Bean
Application selectATrain(){
	def program = new Application(name: "selectATrain", specifications: "Select a train.", description: "Select a train.", language: "groovy")
	def blockchainAdapter = new BlockchainAdapter(javaClass: 'service.tripAgency.TrainSelection', method:'select')
	program.input = new Input(type: "configuration.tripAgency.TripInfo", adapter: blockchainAdapter)
	program.output = new Output(type: "configuration.tripAgency.TripInfo", adapter: blockchainAdapter)
	return program
}
```

### Configuration of selectHotel the from instruction: compute selectHotel with selectATrain.value
```
@Bean
Application selectHotel(){
	def program = new Application(name: "selectHotel", specifications: "Select a hotel.", description: "Select a hotel.", language: "groovy")
	def javaAdapter = new JavaServiceAdapter(javaClass: 'service.travelAgency.HotelSelection', method:'select')
	program.input = new Input(type: "configuration.tripAgency.TripInfo", adapter: javaAdapter)
	program.output = new Output(type: "configuration.tripAgency.TripInfo", adapter: javaAdapter)
	return program
}
```

### Configuration of selectTaxi the from instruction: compute selectTaxi with selectHotel.value, selectATrain.value
```
@Bean
	Application selectTaxi(){
	def program = new Application(name: "selectTaxi", specifications: "Select a taxi.", description: "Select a taxi.", language: "groovy")
	def javaAdapter = new JavaServiceAdapter(javaClass: 'service.tripAgency.TaxiSelection', method:'select')
	program.input = new Input(type: "java.util.List", adapter: javaAdapter)
	program.output = new Output(type: "configuration.tripAgency.TripInfo", adapter: javaAdapter)
	return program
}
```

### Configuration of payment the from instruction: compute payment with selectTaxi.value
```
@Bean
Application payment(){
	def program = new Application(name: "payment", specifications: "Payment.", description: "Pay for the train, the hotel and the taxi.", language: "groovy")
	def javaAdapter = new JavaServiceAdapter(javaClass: 'service.tripAgency.PaymentService', method:'pay')
	program.input = new Input(type: "configuration.tripAgency.TripInfo", adapter: javaAdapter)
	program.output = new Output(type: "configuration.tripAgency.TripInfo", adapter: javaAdapter)
	return program
}
```
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
