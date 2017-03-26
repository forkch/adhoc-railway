#!/bin/bash

ssh -t baehnle@adhocserver 'sudo killall srcpd'
ssh baehnle@adhocserver 'mkdir ~/AdHoc-Railway-srcpd/'

rm AdHoc-Railway-Server.tar.gz
tar cvvf srcpd.tar srcpd 
gzip srcpd.tar

scp -r srcpd.tar.gz baehnle@adhocserver:~/AdHoc-Railway-srcpd
rm srcpd.tar.gz

ssh baehnle@adhocserver 'cd ~/AdHoc-Railway-srcpd && tar xvf srcpd.tar.gz'

ssh -t baehnle@adhocserver 'cd ~/AdHoc-Railway-srcpd/srcpd && ./configure  --disable-ddl --disable-intellibox --disable-li100 --disable-zimo --disable-selectrix  --disable-m605x --disable-ddls88 --disable-i2cdev --disable-hsi88 && make &&  src/srcpd -f mysrcpd.conf'

