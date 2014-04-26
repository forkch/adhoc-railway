#!/bin/bash

gradle clean build distZip generateWebStartFiles

ssh baehnle@adhocserver 'rm -rf /var/www/adhoc-railway/*'
ssh baehnle@adhocserver 'rm -rf ~/AdHoc-Railway/ch.fork.adhocrailway.ui*'
scp -r ch.fork.adhocrailway.ui/build/webstart/signed/* baehnle@adhocserver:/var/www/adhoc-railway
scp -r ch.fork.adhocrailway.ui/build/distributions/* baehnle@adhocserver:~/AdHoc-Railway

