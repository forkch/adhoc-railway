#!/bin/bash

./gradlew --offline -x test clean build distZip shadowJar 

ssh baehnle@adhocserver 'rm -rf /var/www/adhoc-railway/artifacts/ && mkdir -p /var/www/adhoc-railway/artifacts && rm -rf ~/AdHoc-Railway/ch.fork.adhocrailway.ui*'
#scp -r ch.fork.adhocrailway.ui/build/webstart/signed/* baehnle@adhocserver:/var/www/adhoc-railway/artifacts
#scp -r ch.fork.adhocrailway.ui/build/distributions/* baehnle@adhocserver:~/AdHoc-Railway
#scp -r ch.fork.adhocrailway.ui/build/distributions/* baehnle@adhocserver:/var/www/adhoc-railway/artifacts
scp -r ch.fork.adhocrailway.ui/build/libs/adhocrailway-gui.jar baehnle@adhocserver:/var/www/adhoc-railway/artifacts

