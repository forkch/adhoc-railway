/*
 * $RCSfile: SRCPOutOfResourcesException.java,v $
 *
 * History
 $Log: not supported by cvs2svn $
 Revision 1.2  2006/04/16 11:14:17  fork_ch
 - added CommanChannelListener
 - updated Exceptions to also get the cause of the exception

 Revision 1.1  2006/01/02 15:55:54  fork_ch
 - Exception generation is done by ReceivedException Handler
 - added many exceptions
 - added a "old protocol"-mode

 Revision 1.1  2005/07/09 13:11:58  harders
 balkon050709

 Revision 1.1  2005/06/30 14:41:31  harders
 Aufgero?=umte erste Version

 Revision 1.1.1.1  2002/01/08 18:21:54  osc3
 import of jsrcpd


 */

package de.dermoba.srcp.common.exception;


/**
 *
 * @author  osc
 * @version $Revision: 1.3 $
  */

public class SRCPOutOfResourcesException extends SRCPCommandException {

	private static final long serialVersionUID = 204358036979493838L;

    public final static int NUMBER = 500;

    public SRCPOutOfResourcesException () {
        super(NUMBER,"out of resources");
    }

    public SRCPOutOfResourcesException (Throwable cause) {
        super(NUMBER,"out of resources", cause);
    }
    public SRCPException cloneExc() {
    	return new SRCPOutOfResourcesException();
    }
}

