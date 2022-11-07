# Arabica
Aleksander Tudruj, Piotr Uznański, Karol Kuźniak, Jacek Ciszewski

## Description
Arabica is the best base for your coffee you can get. Same here. Arabica is the best Servlet Container for your application.

## Installation
Build and run with docker-compose:
```bash
docker-compose up --build
```

## Actions explanation
- `unit.yaml` - unit tests - on every pull request
- `java.yml` - build and run java app using standard way (:8080) - deployed on every push to master
- `docker.yml` - build and push docker image to docker hub - on every release
- `deployments.yml` - deploy app to server, but using docker (:4080) - run manually
