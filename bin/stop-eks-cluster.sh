#!/usr/bin/env bash

eksctl delete cluster \
  --name=otus-code-challenge \
  --region=us-east-1
