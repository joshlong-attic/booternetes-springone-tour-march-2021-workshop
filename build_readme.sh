#!/usr/bin/env bash 

cd $(dirname $0 )

cd ./preprocessor && ./main.py && \
	 markdown ../README-processed.md > ~/Desktop/out.html  && \
	 echo "produced a new m"