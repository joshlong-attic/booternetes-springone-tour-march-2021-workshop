#!/usr/bin/env bash 

cd $(dirname $0 ) 

./main.py && markdown ../README-processed.md > ~/Desktop/out.html 