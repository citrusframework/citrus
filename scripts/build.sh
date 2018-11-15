#!/bin/bash

if [ "${SONAR_SCANNER_HOME}" != "" ]; then
    COMMAND="mvn --batch-mode -q clean org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -Dsonar.projectKey=citrus"
else
    COMMAND="mvn --batch-mode -q clean install"
fi

echo ${COMMAND}
${COMMAND}