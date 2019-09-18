#!/usr/bin/env bash

kubectl apply -f manifests/graphql-api.yaml

kubectl apply -f manifests/pre-seeded-mongodb.yaml
