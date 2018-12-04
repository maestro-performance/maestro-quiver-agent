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
MAESTRO_VERSION?=1.5.0-SNAPSHOT
MAESTRO_BRANCH?=devel
MAESTRO_LABEL_VERSION?=${MAESTRO_BRANCH}

default: build

clean:
	docker rmi -f maestroperf/maestro-agent-quiver:${MAESTRO_VERSION}
	docker rmi -f maestroperf/maestro-agent-quiver:${MAESTRO_LABEL_VERSION}

maestro:
	cd agent-quiver && docker build --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} -t maestroperf/maestro-agent-quiver:${MAESTRO_VERSION} -t maestroperf/maestro-agent-quiver:${MAESTRO_LABEL_VERSION} --target maestro-agent-quiver .

build: maestro

push: build
	docker push maestroperf/maestro-agent-quiver:${MAESTRO_VERSION}
	docker push maestroperf/maestro-agent-quiver:${MAESTRO_LABEL_VERSION}

all: push

.PHONY: clean all maestro