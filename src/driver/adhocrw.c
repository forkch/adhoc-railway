/*------------------------------------------------------------------------
 * 
 * <adhocrw.c>  -  <desc>
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

#include <linux/module.h>       /* Needed by all modules */
#include <linux/kernel.h>       /* Needed for KERN_INFO */
#include <linux/init.h>         /* Needed for the macros */
#include <linux/slab.h>     /* kmalloc() */
#include <linux/fs.h>       /* everything... */
#include <linux/errno.h>    /* error codes */
#include <linux/types.h>    /* size_t */
#include <linux/proc_fs.h>
#include <linux/fcntl.h>    /* O_ACCMODE */
#include <linux/seq_file.h>
#include <linux/cdev.h>


#include "adhocrw.h"

int adhocrw_major = ADHOCRW_MAJOR;
int adhocrw_minor = ADHOCRW_MINOR;
int adhocrw_nr_devs = ADHOCRW_NR_DEVS;

struct file_operations adhocrw_fops = {
    .owner =    THIS_MODULE,
    .read =     adhocrw_read,
    .write =    adhocrw_write,
    .ioctl =    adhocrw_ioctl,
    .open =     adhocrw_open,
    .release = adhocrw_release,
};

struct adhocrw_dev *adhocrw_devices;

ssize_t adhocrw_read    (struct file *filep, char __user *buf, size_t count, 
    loff_t *f_pos) {
    printk(KERN_INFO "adhocrw: received read\n");
    return count;
}

ssize_t adhocrw_write   (struct file *filep, const char __user *buf, 
    size_t count, loff_t *f_pos) {
    printk(KERN_INFO "adhocrw: received write\n");
    return count;
}

int     adhocrw_ioctl   (struct inode *inode, struct file *filep, 
    unsigned int cmd, unsigned long arg) {
    printk(KERN_INFO "adhocrw: received ioctl\n");
    return 0;
}

/* Open */
int     adhocrw_open    (struct inode *inode, struct file *filep) {
    struct adhocrw_dev *dev;
    printk(KERN_INFO "adhocrw: received open\n");
    dev = container_of(inode->i_cdev, struct adhocrw_dev, cdev);
    filep->private_data = dev;
    return 0;
}

/* Release */
int     adhocrw_release (struct inode *inode, struct file *filep) {
    printk(KERN_INFO "adhocrw: received release\n");
    return 0;
}

static int __init init_driver(void)

{
    int result, i;
    dev_t dev = 0;

    /*
     * Get a range of minor numbers to work with, asking
     * for a dynamic
     * major unless directed otherwise at load time.
     */
    if (adhocrw_major) {
        dev = MKDEV(adhocrw_major, adhocrw_minor);
        result = register_chrdev_region(dev, adhocrw_nr_devs, "adhocrw");
    } else {
        result = alloc_chrdev_region(&dev, adhocrw_minor, adhocrw_nr_devs,
            "adhocrw");
        adhocrw_major = MAJOR(dev);
    }
    if (result < 0) {
        printk(KERN_WARNING "adhocrw: can't get major %d\n", adhocrw_major);
        return result;
    }

    adhocrw_devices = kmalloc(adhocrw_nr_devs * sizeof(struct adhocrw_dev), GFP_KERNEL);
    if (!adhocrw_devices) {
        result = -ENOMEM;
        goto fail;  /* Make this more graceful */
    }

    /* Initialize each device. */
    for (i = 0; i < adhocrw_nr_devs; i++) {
        init_MUTEX(&adhocrw_devices[i].sem);
        adhocrw_setup_cdev(&adhocrw_devices[i], i);
    }

    printk(KERN_INFO "Adhoc-Railway-Driver loaded\n");
    return 0;

fail:
    cleanup_driver();
    return result;

}

static void __exit cleanup_driver(void)
{
    int i;
    dev_t devno = MKDEV(adhocrw_major, adhocrw_minor);

    /* Get rid of our char dev entries */
    if (adhocrw_devices) {
        for (i = 0; i < adhocrw_nr_devs; i++) {
            adhocrw_trim(adhocrw_devices + i);
            cdev_del(&adhocrw_devices[i].cdev);
        }
        kfree(adhocrw_devices);
    }

    /* cleanup_module is never called if registering failed */
    unregister_chrdev_region(devno, adhocrw_nr_devs);


    printk(KERN_INFO "Adhoc-Railway-Driver unloaded\n");
}

/*
 * Set up the char_dev structure for this device.
 */
static void adhocrw_setup_cdev(struct adhocrw_dev *dev, int index)
{
    int err, devno = MKDEV(adhocrw_major, adhocrw_minor + index);

    cdev_init(&dev->cdev, &adhocrw_fops);
    dev->cdev.owner = THIS_MODULE;
    dev->cdev.ops = &adhocrw_fops;
    err = cdev_add (&dev->cdev, devno, 1);
    /* Fail gracefully if need be */
    if (err)
        printk(KERN_NOTICE "Error %d adding adhocrw%d", err, index);
}

/*
 * Empty out the adhocrw device; must be called with the device
 * semaphore held.
 */ 
int adhocrw_trim(struct adhocrw_dev *dev)
{
    return 0;
}

module_init(init_driver);
module_exit(cleanup_driver);
/*
 * Get rid of taint message by declaring code as GPL.
 */
MODULE_LICENSE("GPL");
MODULE_AUTHOR(DRIVER_AUTHOR);    /* Who wrote this module? */
MODULE_DESCRIPTION(DRIVER_DESC);         /* What does this module do */
