#!/bin/bash

if [ "${SONAR_SCANNER_HOME}" != "" ]; then
    COMMAND="mvn --batch-mode -q clean org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -Dsonar.projectKey=citrus -Pvintage"
else
    COMMAND="mvn --batch-mode -q clean install -Pvintage"
fi

echo ${COMMAND}
${COMMAND}
