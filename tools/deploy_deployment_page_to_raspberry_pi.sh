#!/bin/bash

cd AdHoc-Railway-Deployment-Page

grunt build

scp -r dist/* baehnle@adhocserver:/var/www/
