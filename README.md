# What

This repo is just a little pet project to be able to use absence.io api with siri

# Why

Altough absence has alredy an api, it needs hawk authentication , and i wanted to use it with shorcuts.
I also wanted to do it using a new languaje , and in this case is kotlin.

# Architecture.

This is basically a kotlin lambda app, that has a congito authorizer, absent credentials are get from SSM

All is delployed via serverless




# How

- Add credentials to the ssm store: key and id

- create a use and a add it to cognito

- TODO how to validate user
- Run  ´./gradlew server less deploy´  