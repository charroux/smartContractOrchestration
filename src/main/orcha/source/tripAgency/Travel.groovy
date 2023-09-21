package source.tripAgency

domain tripAgency
description "organize a trip for a costumer"
title "organize trip"
author "Ben C."
version "1.0"

receive tripInfo from tripAgency
compute selectATrain with tripInfo.value

when "selectATrain terminates"
compute selectHotel with selectATrain.value

when "selectHotel terminates and selectATrain terminates"
compute selectTaxi with selectHotel.value, selectATrain.value

when "selectTaxi terminates"
compute payment with selectTaxi.value

when "payment terminates"
send payment.result to tripAgencyCustomer



