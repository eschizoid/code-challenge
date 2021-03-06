#!/usr/bin/env bash

eksctl create cluster \
  --name=otus-code-challenge \
  --nodes=3 \
  --version=1.12 \
  --region=us-east-1 \
  --node-type=t3.xlarge \
  --zones=us-east-1a,us-east-1b,us-east-1d
