#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
    COMMAND="mvn --batch-mode -q clean org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -Dsonar.projectKey=citrus"
else
    COMMAND="mvn --batch-mode -q clean install"
fi

echo ${COMMAND}
${COMMAND}