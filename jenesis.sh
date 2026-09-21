#!/usr/bin/env bash

git submodule update --rebase jenesis
mkdir -p build
cd build || exit
rm -f jenesis
ln -s ../jenesis/sources/build/jenesis jenesis
cd ..

java build/jenesis/Make.java "$@"
