#!/bin/bash


START=$(date +%s)
java -jar database.jar
END=$(date +%s)
DIFF=$(( $END - $START ))
echo "Timp executie: $DIFF secunde"
