#! /bin/bash

TAG="$1"
VERSION=$(grep '<version>' pom.xml | head -n 1 | sed -e 's/<[^>]*>//g')

if [ ! "${TAG}" ] ; then
    echo *You need to specify version.*
    echo *The current version = ${VERSION}*
    exit 1
fi

echo New version = ${TAG}

set -e

echo Update version
mvn versions:set "-DnewVersion=${TAG}" | tee -a maven-release.log
git commit -am "Version ${TAG}" | tee -a maven-release.log
git tag "v${TAG}" | tee -a maven-release.log
mvn clean deploy | tee -a maven-release.log