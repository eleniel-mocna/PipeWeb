#!/usr/bin/env bash

printf "Test script started...\n">"test_output"
for (( i = 1; i <= $#; i++ )); do
    sleep 5
    echo "$i: ${@:i:1}">>"test_output"
done
printf "Test script ended!\n">>"test_output"