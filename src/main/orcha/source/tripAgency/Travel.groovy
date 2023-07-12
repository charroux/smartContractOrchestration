package source.tripAgency

domain tripAgency
description "organize a trip for a costumer"
title "organize trip"
author "Ben C."
version "1.0"

receive tripInfo from tripAgency
compute selectTrain with tripInfo.value

when "selectTrain terminates"
compute selectHotel with selectTrain.value

when "selectHotel terminates and selectTrain terminates"
compute selectTaxi with selectHotel.value, selectTrain.value

when "selectTaxi terminates"
send selectTaxi.result to tripAgencyCustomer
