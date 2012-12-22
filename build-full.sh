#!/bin/bash

cd ./capture/
ant capture-clean
ant capture-zip

cd ../connector/
ant connector-clean
ant connector-zip

cd ../phybots/
ant clean
ant phybots-zip
ant jar-utils

cd ../napkit/
ant napkit-clean
ant napkit-zip
ant mqoloader-zip

cd ../phybots/
ant -f build-full-javadoc.xml javadoc-zip
ant -f build-full-zip.xml phybots-full-zip

cd ../
