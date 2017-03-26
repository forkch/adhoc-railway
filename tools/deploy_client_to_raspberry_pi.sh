#!/bin/bash

./gradlew --offline -x test clean build distZip generateWebStartFiles -x createDmg

ssh baehnle@adhocserver 'rm -rf /var/www/adhoc-railway/artifacts/'
ssh baehnle@adhocserver 'mkdir -p /var/www/adhoc-railway/artifacts'
ssh baehnle@adhocserver 'rm -rf ~/AdHoc-Railway/ch.fork.adhocrailway.ui*'
scp -r ch.fork.adhocrailway.ui/build/webstart/signed/* baehnle@adhocserver:/var/www/adhoc-railway/artifacts
scp -r ch.fork.adhocrailway.ui/build/distributions/* baehnle@adhocserver:~/AdHoc-Railway

