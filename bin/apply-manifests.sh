#!/usr/bin/env bash

kubectl apply -f manifests/mongodb.yaml
sleep 15
kubectl apply -f manifests/graphql-api.yaml
