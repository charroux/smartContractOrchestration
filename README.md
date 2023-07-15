# Smart Contracts orchestration with Orcha

## Orcha program

https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/source/tripAgency/Travel.groovy

## Orcha configuration of the Orcha program

https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/configuration/tripAgency/OrganizeTripConfiguration.groovy

## Services called by the Orcha program

https://github.com/charroux/smartContractOrchestration/tree/main/src/main/orcha/service/tripAgency

Train selection service colling the train selection smart contract: https://github.com/charroux/smartContractOrchestration/blob/main/src/main/orcha/service/tripAgency/TrainSelection.groovy

Train selection smart contract: 
========

Un Oracle :
appel RPC avec comme arg : 
- ad contrat = public key
- info
- authen via compte utili block  sign requete avce clef privée


Le mécanisme de validation est interne au contrat

un smart contrat peut vérifier la finitude 


welcome to the ORCHA project
========

<a href="http://orchalang.com">Discover Orcha</a> 

See the <a href="https://gitlab.com/BenOrcha/orcha/blob/master/CONTRIBUTING.md">contribution guide</a> to know how to contribute.
