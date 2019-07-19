#!/bin/bash

set -e

# cd to project root directory
cd "$(dirname "$(dirname "$0")")"

docker build --target base -t genomealmanac/chipseq-filter-base .

docker run --name chipseq-filter-base --rm -i -t -d \
    -v /tmp/chipseq-test:/tmp/chipseq-test \
    genomealmanac/chipseq-filter-base /bin/sh