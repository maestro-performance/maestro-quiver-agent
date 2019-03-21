Maestro Quiver Agent: a Maestro Agent Endpoint for running performance tests with Quiver 
============


Introduction
----

The Maestro Quiver Agent contains the endpoint code, as well as the build image code, for running 
[Quiver Test Tool](https://github.com/ssorj/quiver/) tests using Maestro. Using this agent endpoint, Maestro becomes 
responsible for the test orchestration, whereas Quiver and the arrows implementations are responsible for running the 
actual tests. 

Due to the differences in implementations, not all the features are supported. Some of the unsupported features include:
 
- stats collection during test execution
- interactive graphics in the reports
- different messaging protocols (only AMQP is supported)
- unbalanced role distribution

Quiver tests should be run using the FlexibleTest script from Maestro 1.5 or greater. The broker URL must not contain 
query parameters. The following parameters should be exported for the test execution:

| Variable Name    | Default Value       | Description          |
|-------------------|---------------------|----------------------|
| `SEND_RECEIVE_URL` | `null` | The URL for the Maestro broker |
| `MAESTRO_BROKER` | `null` | The URL for the SUT (ie.: ```amqp://testhost/queue```) without options |
| `SOURCE_URL` | `null` |  https://github.com/maestro-performance/maestro-quiver-agent.git |
| `SOURCE_BRANCH` | `master` |  Git branch for the source URL |
| `TEST_DURATION` | `null` | The estimated test duration (see the notes below) |

Test Duration
----

Due to the differences in the implementations, Maestro is unable to determine the correct test duration. Therefore, one 
must be set according to the test format specification used by 
[Maestro](https://github.com/maestro-performance/maestro-java). Consult the Maestro documentation for further details 
about the duration format. 

For a modern hardware with a fast disk, using a Java-based broker, the duration can be set to `150s`. Naturally, Maestro 
will terminate early if the test finishes early. On the other hand, if the tests terminate unsuccessfully, this value 
may need to be increased to accommodate the capacity of the hardware in use.


Demo: OpenShift
----

Sample execution of a performance test, using the Agent and [Qpid JMS](https://qpid.apache.org/components/jms/index.html) 
implementation from Quiver.

[![asciicast](https://asciinema.org/a/nrKztBYUJ8VYQ1AawtTH6XDsL.svg)](https://asciinema.org/a/nrKztBYUJ8VYQ1AawtTH6XDsL)


Usage: Docker Compose
----

To launch a mini test cluster using Maestro, a Maestro Agent with Quiver support use the docker compose file provided 
with this project.

```
docker-compose -f docker-compose up -d --scale agent=1
```

Usage: OpenShift
----

To execute the tests using the Agent deployed on an OpenShift cluster (or a Kubernetes one), first deploy the agent to 
the OpenShift cluster:

```
oc apply -f agent 
oc scale deployment --replicas=1 maestro-agent-quiver 
```

After this is complete, you can run one of the pre-defined test-cases documented below.

# Running RHEA tests with Quiver

```
oc get pods
oc apply -f client/testcase-rhea.yaml
oc apply -f client/client.yaml
oc logs maestro-client -f -c client
oc delete pod maestro-client
```


# Running Qpid JMS tests with Quiver

```
oc get pods
oc apply -f client/testcase-qpid-jms.yaml
oc apply -f client/client.yaml
oc logs maestro-client -f -c client
oc delete pod maestro-client
```

# Running Artemis JMS tests with Quiver

```
oc get pods
oc apply -f client/testcase-activemq-artemis-jms.yaml
oc apply -f client/client.yaml
oc logs maestro-client -f -c client
oc delete pod maestro-client
```

# Running Qpid Messaging CPP tests with Quiver

```
oc get pods
oc apply -f client/testcase-qpid-messaging-cpp.yaml
oc apply -f client/client.yaml
oc logs maestro-client -f -c client
oc delete pod maestro-client
```


Usage: Docker Compose
----

To run the tests natively, you have to ensure that Quiver is installed and operational on your system. With Quiver and 
Maestro Agent installed on your system, Maestro will take care of the rest.

Building
----

To build the image, just run `make all` on the directory containing the Makefile (located on the same directory as the 
docker compose file).
