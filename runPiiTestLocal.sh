#!/usr/bin/env bash

curl --request POST \
    --header "content-type:application/json" \
    --data '{"name":"Test Test", "phone":"555-555-5555", "email":"test@test.com"}' \
    "https://localhost:8080/pii?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I"

echo "\n"

curl --request GET \
    "https://localhost:8080/pii/1?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I"

echo "\n"

curl --request GET \
    "https://localhost:8080/pii/1?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I&data=email"


echo "\n"

curl --request PUT \
    --header "content-type:application/json" \
    --data '{"phone":"666-666-6666"}' \
    "https://localhost:8080/pii/1?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I"

echo "\n"

curl --request DELETE \
    "https://localhost:8080/pii/1?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I"

echo "\n"