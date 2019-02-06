#!/usr/bin/env bash

curl --request POST \
    --header "content-type:application/json" \
    --data '{"message":"hello world", "test":"test"}' \
    "https://localhost:8080/pii?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I"

curl --request GET \
    --header "content-type:application/json" \
    --data '{"message":"hello world", "test":"test"}' \
    "https://localhost:8080/pii/1?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I"
