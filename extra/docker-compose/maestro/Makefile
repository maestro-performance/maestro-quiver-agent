MAESTRO_VERSION=1.5.0-SNAPSHOT
MAESTRO_BRANCH=devel

clean:
	docker rmi -f maestroperf/maestro-agent-quiver:${MAESTRO_VERSION}

maestro:
	cd agent-quiver && docker build --build-arg MAESTRO_VERSION=${MAESTRO_VERSION} --build-arg MAESTRO_BRANCH=${MAESTRO_BRANCH} -t maestroperf/maestro-agent-quiver:${MAESTRO_VERSION} --target maestro-agent-quiver .

build: maestro

push: build
	docker push maestroperf/maestro-agent-quiver:${MAESTRO_VERSION}

all: push

.PHONY: clean all maestro