#!/usr/bin/env bash

curl --request POST \
    --header "content-type:application/json" \
    --data '{"message":"hello world", "test":"test"}' \
    "https://pii-service.appspot.com/pii/1?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I"
K