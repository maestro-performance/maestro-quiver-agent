#   Copyright 2017 Otavio Rodolfo Piske
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
FROM centos:7
MAINTAINER Otavio Rodolfo Piske <angusyoung@gmail.com>
ARG ARTEMIS_VERSION
ENV ARTEMIS_VERSION ${ARTEMIS_VERSION:-2.6.0}
ARG ARTEMIS_JOURNAL
ENV ARTEMIS_JOURNAL ${ARTEMIS_JOURNAL:-aio}
ENV MAESTRO_SUT_ROOT /opt/maestro/sut/
EXPOSE 5672 8161 61613 61616
RUN yum install -y java-1.8.0-openjdk libaio
RUN yum clean all
ENV JAVA_HOME /etc/alternatives/jre
RUN mkdir -p ${MAESTRO_SUT_ROOT}
WORKDIR ${MAESTRO_SUT_ROOT}
RUN curl https://archive.apache.org/dist/activemq/activemq-artemis/${ARTEMIS_VERSION}/apache-artemis-${ARTEMIS_VERSION}-bin.tar.gz -o apache-artemis.tar.gz
RUN tar -xvf apache-artemis.tar.gz && ln -s ${MAESTRO_SUT_ROOT}/apache-artemis-${ARTEMIS_VERSION} ${MAESTRO_SUT_ROOT}/apache-artemis
RUN rm -f apache-artemis.tar.gz
RUN ${MAESTRO_SUT_ROOT}/apache-artemis/bin/artemis create --${ARTEMIS_JOURNAL} --allow-anonymous --http-host 0.0.0.0 --user admin --password "admin" --role amq ${MAESTRO_SUT_ROOT}/apache-artemis-instance
ADD jolokia-access.xml ${MAESTRO_SUT_ROOT}/apache-artemis-instance/etc/jolokia-access.xml
CMD [ "sh", "-c", "./apache-artemis-instance/bin/artemis run" ]