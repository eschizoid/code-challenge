#!/usr/bin/env bash

docker exec -it mongodb mongo otus \
  --eval "db.dropDatabase()" \
  --host mongodb \
  --username mongo \
  --password mongo \
  --authenticationDatabase admin

docker exec -it mongodb mongoimport \
  --username mongo \
  --password mongo \
  --authenticationDatabase admin \
  --host mongodb \
  --db otus \
  --collection students \
  --type json \
  --file /mongo-seed/students.json
