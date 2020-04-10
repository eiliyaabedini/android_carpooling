#!/usr/bin/env bash

printf "Please remove the plugins folder before running this command.\n"

while true; do
    read -p "Did you remove the plugins folder already and want to continue?" yn
    case $yn in
        [Yy]* ) git clone https://github.com/deftmove/android_carpool_plugins; mv android_carpool_plugins plugins; break;;
        [Nn]* ) exit;;
        * ) echo "Please answer yes or no.";;
    esac
done
