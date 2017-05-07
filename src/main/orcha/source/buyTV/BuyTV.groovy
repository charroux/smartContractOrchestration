package source.buyTV

domain TVSelling
description "buy TV"
title "buy TV"
author "Ben C."
version "1.0"

receive tvSpecifications from buyer
compute buyTV with tvSpecifications.value

when "buyTV terminates"
compute deliverTV with buyTV.result

when "deliverTV terminates" send deliverTV.result to seller