/*------------------------------------------------------------------------
 * 
 * <adhocrw_driver.c>  -  <desc>
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
#define DRIVER_AUTHOR "Benjamin Mueller fork_ch@sourceforge.net"
#define DRIVER_DESC   "Adhoc-Railway-Driver"
static int __init init_driver(void)
{
    printk(KERN_INFO "Adhoc-Railway-Driver loaded\n");
    return 0;
}

static void __exit cleanup_driver(void)
{
    printk(KERN_INFO "Adhoc-Railway-Driver unloaded\n");
}
module_init(init_driver);
module_exit(cleanup_driver);
/*
 * Get rid of taint message by declaring code as GPL.
 */
MODULE_LICENSE("GPL");
MODULE_AUTHOR(DRIVER_AUTHOR);    /* Who wrote this module? */
MODULE_DESCRIPTION(DRIVER_DESC);         /* What does this module do */
