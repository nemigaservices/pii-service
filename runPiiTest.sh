#!/usr/bin/env bash

export API_KEY = ... your API KEY ...

curl --request POST \
    --header "content-type:application/json" \
    --data '{"name":"Test Test", "phone":"555-555-5555", "email":"test@test.com"}' \
    "https://pii-service.appspot.com/pii?key=${API_KEY}"

echo "\n"

export USER_ID = ... ID received from the previous call ...

curl --request GET \
    "https://pii-service.appspot.com/pii/${USER_ID}?key=${API_KEY}"

echo "\n"

curl --request GET \
    "https://pii-service.appspot.com/pii/${USER_ID}?key=${API_KEY}&data=email"


echo "\n"

curl --request PUT \
    --header "content-type:application/json" \
    --data '{"phone":"666-666-6666"}' \
    "https://pii-service.appspot.com/pii/${USER_ID}?key=${API_KEY}"

echo "\n"

curl --request DELETE \
    "https://pii-service.appspot.com/pii/${USER_ID}?key=${API_KEY}"

echo "\n"



