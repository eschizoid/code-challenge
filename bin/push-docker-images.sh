#!/usr/bin/env bash

################################################
# Replace this value with your docker hub user #
################################################
DOCKER_HUB_USER=eschizoid

docker tag code-challenge_graphql-api ${DOCKER_HUB_USER}/graphql-api:otus
docker tag mongo:latest ${DOCKER_HUB_USER}/mongodb:otus
docker push ${DOCKER_HUB_USER}/graphql-api:otus
docker push ${DOCKER_HUB_USER}/mongodb:otus
