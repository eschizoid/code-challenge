#!/usr/bin/env bash

docker-compose rm
docker-compose pull
docker-compose build --no-cache
docker-compose up -d mongodb
sleep 15
docker-compose up -d graphql-api
