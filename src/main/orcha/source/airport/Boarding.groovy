package source.airport

domain transport
title "passenger boarding"
description "control passenger identity and passenger luggage"
author "Ben C."
version "1.0"

receive passport from passenger condition "name == 'Ethan Hunt'"
compute controlIdentity with passport.value

receive luggage from airportHandling condition "passenger.name == 'Ethan Hunt'"
compute scanLuggage with luggage.value

when "scanLuggage terminates with alert == true and controlIdentity terminates"
compute alertAuthorities with scanLuggage.result

when "alertAuthorities termintes"
send alertAuthorities.result to diplomaticService