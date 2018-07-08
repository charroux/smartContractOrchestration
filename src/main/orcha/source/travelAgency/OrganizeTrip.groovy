package source.travelAgency

domain travelAgency
description "organize a trip for a costumer"
title "organize trip"
author "Ben C."
version "1.0"

receive travelInfo from travelAgency
compute selectTrain with travelInfo.value

when "selectTrain terminates"
compute selectHotel with selectTrain.result

when "selectHotel terminates and selectTrain terminates"
compute selectTaxi with selectHotel.result, selectTrain.result

when "selectTaxi terminates"
send selectTaxi.result to travelAgencyCustomer
  