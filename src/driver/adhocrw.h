/*------------------------------------------------------------------------
 * 
 * <adhocrw.h>  -  <desc>
 * 
 * begin     : Tue Jan  2 14:29:16 CET 2007
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : c/c++
 * version   : $Id$
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

#ifndef ADHOCRW_H
#define ADHOCRW_H

#define DRIVER_AUTHOR "Benjamin Mueller fork_ch@sourceforge.net"
#define DRIVER_DESC   "Adhoc-Railway-Driver"

#ifndef ADHOCRW_MAJOR
#define ADHOCRW_MAJOR 0   /* dynamic major by default */
#endif

#ifndef ADHOCRW_MINOR
#define ADHOCRW_MINOR 0   /* dynamic major by default */
#endif

#ifndef ADHOCRW_NR_DEVS
#define ADHOCRW_NR_DEVS 1    /* scull0 through scull3 */
#endif

struct adhocrw_dev {
    struct semaphore sem;
    struct cdev cdev;
};


static int __init init_driver(void);
static void __exit cleanup_driver(void);
static void adhocrw_setup_cdev(struct adhocrw_dev *dev, int index);
int adhocrw_trim(struct adhocrw_dev *dev);

ssize_t adhocrw_read    (struct file *, char __user *, size_t, loff_t *);
ssize_t adhocrw_write   (struct file *, const char __user *, size_t, loff_t *);
int     adhocrw_ioctl   (struct inode *, struct file *, unsigned int, unsigned long);
int     adhocrw_open    (struct inode *, struct file *);
int     adhocrw_release (struct inode *, struct file *);

#endif
