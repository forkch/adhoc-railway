#!/bin/bash

ssh -t baehnle@adhocserver 'sudo killall node'
ssh baehnle@adhocserver 'mkdir ~/AdHoc-Railway/'
ssh baehnle@adhocserver 'rm -rf ~/AdHoc-Railway/AdHoc-Railway-Server'

mkdir build
cd build

rm AdHoc-Railway-Server.tar.gz
tar cvvf AdHoc-Railway-Server.tar ../AdHoc-Railway-Server
gzip AdHoc-Railway-Server.tar

scp -r AdHoc-Railway-Server.tar.gz baehnle@adhocserver:~/AdHoc-Railway

ssh baehnle@adhocserver 'cd ~/AdHoc-Railway && tar xvf AdHoc-Railway-Server.tar.gz'
ssh baehnle@adhocserver 'rm ~/AdHoc-Railway/AdHoc-Railway-Server.tar.gz'

ssh baehnle@adhocserver 'rm -rf ~/AdHoc-Railway/AdHoc-Railway-Webclient'

rm AdHoc-Railway-Webclient.tar.gz
tar cvvf AdHoc-Railway-Webclient.tar ../AdHoc-Railway-Webclient 
gzip AdHoc-Railway-Webclient.tar

scp -r AdHoc-Railway-Webclient.tar.gz baehnle@adhocserver:~/AdHoc-Railway

ssh baehnle@adhocserver 'cd ~/AdHoc-Railway && tar xvf AdHoc-Railway-Webclient.tar.gz'
ssh baehnle@adhocserver 'rm ~/AdHoc-Railway/AdHoc-Railway-Webclient.tar.gz'

ssh -t baehnle@adhocserver 'time sudo service adhoc-railway-server start && time sudo service adhoc-railway-webclient start'
cd ..

