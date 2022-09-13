#!/usr/bin/env bash

printf "Test script started...\n">"test_output"
for (( i = 0; i < $#; i++ )); do
    sleep 5
    echo "$i">>"test_output"
done
printf "Test script ended!\n">"test_output"