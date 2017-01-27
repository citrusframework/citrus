FROM openjdk:8-jdk
MAINTAINER citrusframework.org <citrus-dev-l@consol.de>

ARG MAVEN_VERSION=3.3.9
ARG USER_HOME_DIR="/root"

# install maven
RUN mkdir -p /usr/share/maven \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

RUN mkdir citrus-project
COPY pom.xml citrus-project/
COPY src citrus-project/src

# Download all dependencies with compile and skip tests, we use a simple install and clean the 'target' folder afterwards. This way almost all dependencies are bundled in the local m2 repo.
RUN cd citrus-project && mvn install clean

ENV APP_DIR /maven

ONBUILD WORKDIR ${APP_DIR}

CMD mvn install