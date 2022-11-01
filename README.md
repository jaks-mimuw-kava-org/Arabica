# Kava
Aleksander Tudruj, Piotr Uznański, Karol Kuźniak, Jacek Ciszewski

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
