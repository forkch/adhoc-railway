#!/bin/bash

cd AdHoc-Railway-Deployment-Page

grunt clean build

scp -r dist/* baehnle@adhocserver:/var/www/
