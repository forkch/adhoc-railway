Summary: srcpd is a SRCP server daemon to control digital model railroads
Summary(de): srcpd ist ein SRCP-Server zur Steuerung von digitalen Modelleisenbahnen
Name: srcpd
Version: 2.1.1
Release: 1%{?dist}
Source0: %{name}-%{version}.tar.bz2
License: GPL
Group: Games/Daemon
Vendor: the srcpd team
URL: http://srcpd.sourceforge.net/
Prefix: /usr
Buildroot: %{_tmppath}/%{name}-%{version}-buildroot
Provides: srcpd
# if you use the LSB compliant init.d script, add "redhat-lsb" here:
Requires: libxml2
BuildRequires: libxml2-devel

%description
The srcpd is a server daemon that enables you to control and play with 
a digital model railroad using any SRCP client. Currently it supports many
interface (both self made and commercally) and direct signal generation.

More information about SRCP and links to many really cool clients (and 
other servers for different hardware) can be found at 
http://srcpd.sourceforge.net/ and http://www.der-moba.de/

%prep
%setup -q

%build
CFLAGS=$RPM_OPT_FLAGS \
./configure --prefix=%{_prefix} \
            --mandir=%{_mandir} \
	    --sysconfdir=%{_sysconfdir}
make

%install
[ -n "$RPM_BUILD_ROOT" -a "$RPM_BUILD_ROOT" != / ] && rm -rf $RPM_BUILD_ROOT

install -d $RPM_BUILD_ROOT%{_sysconfdir}/init.d
make DESTDIR=$RPM_BUILD_ROOT install

# to use the LSB compliant init.d script change "init.d.redhat" to
# "init.d.lsb":
install -m 755 init.d.redhat $RPM_BUILD_ROOT%{_sysconfdir}/init.d/%{name}

%clean
[ -n "$RPM_BUILD_ROOT" -a "$RPM_BUILD_ROOT" != / ] && rm -rf $RPM_BUILD_ROOT

%post
# Initialize runlevel links
if [ -x /usr/lib/lsb/install_initd ] ; then
    /usr/lib/lsb/install_initd %{_sysconfdir}/init.d/%{name}
fi

%preun
# stop running server daemon and remove runlevel links
%{_sysconfdir}/init.d/%{name} stop
if [ -x /usr/lib/lsb/install_initd ] ; then
    /usr/lib/lsb/remove_initd %{_sysconfdir}/init.d/%{name}
fi

%files
%defattr(-,root,root)
%{_sbindir}/%{name}
%{_sysconfdir}/init.d/%{name}
%{_sysconfdir}/udev/rules.d/*
%docdir %{_mandir}/*
%{_mandir}/man8/*
%{_mandir}/de/man8/*
%{_mandir}/man5/*
%{_mandir}/de/man5/*
%config(noreplace) %{_sysconfdir}/%{name}.conf
%doc COPYING AUTHORS README NEWS DESIGN PROGRAMMING-HOWTO
%doc README.freebsd README.selectrix README.loconet TODO

%changelog
* Wed Mar 03 2010 Guido Scholz <guido.scholz@bayernline.de> 2.1.1-1
- Update to version 2.1.1

* Sat Dec 19 2009 Guido Scholz <guido.scholz@bayernline.de> 2.1.0-1
- Update to version 2.1.0

* Wed Jan 21 2009 Guido Scholz <guido.scholz@bayernline.de> 2.0.13-1
- Update to version 2.0.13

* Sat Dec 29 2007 Guido Scholz <guido.scholz@bayernline.de> 2.0.11-1
- update to version 2.0.11
- man page (man5) added
- translated man pages added
- udev rules file added

* Sat Dec 09 2006 Guido Scholz <guido.scholz@bayernline.de>
- update to version 2.0.10

* Mon Dec 26 2005 Guido Scholz <guido.scholz@bayernline.de>
- adaptation to redhat/fedora style

* Sun Dec 11 2005 Guido Scholz <guido.scholz@bayernline.de>
- dist tag added, hard coded packager removed

* Mon Jul 11 2005 Guido Scholz <guido.scholz@bayernline.de>
- More documentation files added

* Mon Nov 01 2004 Guido Scholz <guido.scholz@bayernline.de>
- Changed sysconfdir patch

* Mon Oct 18 2004 Guido Scholz <guido.scholz@bayernline.de>
- Update to srcpd-2.0-6

* Thu Jan 08 2004 Guido Scholz <guido.scholz@bayernline.de>
- adaptation to SuSE 9.0

