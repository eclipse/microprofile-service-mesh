## MicroProfile Istio Sample
This project is intended to show the interaction between Istio and the various MicroProfile specifications.

### Requirements
* [Git](https://git-scm.com/)
* [Docker](https://www.docker.com/)
* [Maven](https://maven.apache.org/install.html)
* [Java 8]: Any compliant JDK should work.
  * [Java 8 JDK from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Java 8 JDK from IBM (AIX, Linux, z/OS, IBM i)](http://www.ibm.com/developerworks/java/jdk/),
    or [Download a Liberty server package](https://developer.ibm.com/assets/wasdev/#filter/assetTypeFilters=PRODUCT)
    that contains the IBM JDK (Windows, Linux)
* [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
    * If your cluster is [minikube](https://kubernetes.io/docs/getting-started-guides/minikube/), you will already have this
* [Istio](https://istio.io/)

### Project Contents
The project comprises a set of communicating services designed to be run as docker containers deployed into an Istio enabled Kubernetes cluster.

Currently, the services are serviceA and serviceB which are independent maven projects each in its own subdirectory of this project.

serviceA is currently the entry point for the sample. Once installed, the sample can be accessed at:
```
http://<your kubernetes cluster>/mp-istio-sample/serviceA
```

### Build

The steps involved in building the project are:
1.  [Build the services](#build-the-services)
2.  [Package the services as docker images](#package-the-services)
3.  [Push the images to a repository](#push-the-services)
4.  [Install Istio into your cluster](#install-istio)
5.  [Configure your cluster to run the sample](#install-the-sample-services)

### Build the Services

Each service resides in its own directory under this project. Currently they are serviceA and serviceB. To build the services:
1.  (cd serviceA; mvn install)
2.  (cd serviceB; mvn install)

### Package the Services

The services sould be packaged within the environment you use to access docker in your develpoment environment. Each service has a Dockerfile which is used to assemble its docker image. Each image should be tagged such that it can be subsequently pushed to your docker repository. Assuming you will be using dockerhub, and your docker id is `<docker id>`:
1.  (cd serviceA; docker build -t `<docker id>`/mp-istio-sample-servicea .)
2.  (cd serviceB; docker build -t `<docker id>`/mp-istio-sample-serviceb .)

### Push the Services

The service images need to be published to a repository so they will be available to run in your cluster. Assuming you will be using Docker Hub:
1.  docker login
2.  docker push 
3.  docker push `<docker id>`/mp-istio-sample-serviceb

### Install Istio

Ensure that Istio is installed in your cluster and istioctl is on your path. Follow the instructions at the link below. Do not enable automatic proxy injection.
```
https://istio.io/docs/setup/kubernetes/quick-start.html
```
If you want to view distributed trace, install the zipkin provided with Istio.
```
https://istio.io/docs/tasks/telemetry/distributed-tracing.html
```

### Install the Sample Services

The sample services are installed into your cluster using the deployment yaml in the manifests directory. The yaml is preprocessed by istioctl and applied using kubectl.
1.  Edit manifests/sample.yaml, replacing SERVICE_TAG_A with the tag you used when you pushed servicea to Docker Hub e.g. `<docker id>`/mp-istio-sample-servicea
2.  Edit manifests/sample.yaml, replacing SERVICE_TAG_B with the tag you used when you pushed serviceb to Docker Hub e.g. `<docker id>`/mp-istio-sample-serviceb
3.  kubectl apply -f <(istioctl kube-inject --debug -f manifests/istio-sample.yaml)

### Run the Sample

You now have the sample installed in your cluster. The entrypoint for the sample is:
```
http://<your kubernetes cluster>/mp-istio-sample/serviceA
```
The result should look something like:
```
Hello from serviceA
Calling service at: http://serviceb-service:9080/mp-istio-sample/serviceB (ServiceA call count: 5, tries: 1)
Hello from serviceB at Thu May 17 14:28:08 UTC 2018 on serviceb-deployment-57bbdcf656-9ntwq (ServiceB call count: 19, failFrequency: 0)
```
or
```
Hello from serviceAFallback at Thu May 17 14:29:07 UTC 2018 (ServiceA call count: 22)
Completely failed to call http://serviceb-service:9080/mp-istio-sample/serviceB after 4 tries
```
This shows that serviceA is working and has tried to communicate with serviceB. Sometimes this falls back depending on the current code in serviceB and whether Istio traffic management has already been applied.

### Inject Faults to Provoke Fault Tolerance Behavior

**ServiceB can be configured to succeed only every nth time it's called - where n is the failFrequency property in the serviceB configmap**

Delays and faults can be injected into the service calls to test the fault tolerant behavior of the application. A sample Istio routing rule is provided which will cause 75% of calls to serviceB to fail. The sample routing rule can be installed with this command

    kubectl create -f manifests/fault-injection.yaml

and deleted with:

    kubectl delete -f manifests/fault-injection.yaml

You can experiment with the percentage to provoke different fault tolerance behavior. For example, a percentage of 100 will cause the fallback method of serviceA to be invoked every time and the result will always be similar to:
```
Hello from serviceAFallback at Thu May 17 14:29:07 UTC 2018 (ServiceA call count: 22)
Completely failed to call http://serviceb-service:9080/mp-istio-sample/serviceB after 4 tries
```
The percentage can be modified by editing fault-injection.yaml and re-running the kubectl command above to update the routing rule.

### Uninstall the Sample Services

You can uninstall the sample services with the following command.

1.  kubectl apply -f <(istioctl kube-inject --debug -f manifests/istio-sample.yaml)
