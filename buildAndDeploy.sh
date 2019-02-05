#!/usr/bin/env bash

gcloud endpoints services deploy openapi-appengine.yaml
mvn appengine:stage
gcloud app deploy target/appengine-staging -y