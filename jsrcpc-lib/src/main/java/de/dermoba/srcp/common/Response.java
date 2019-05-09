/**
 * 
 */
package de.dermoba.srcp.common;

import org.apache.log4j.Logger;

import de.dermoba.srcp.common.exception.SRCPUnsufficientDataException;

/**
 * This class bundles the data received from an SRCP server as response to a
 * command.
 * 
 * @author mnl
 */
public class Response {

	private static Logger LOGGER = Logger.getLogger(Response.class);

	private double timestamp;
	private int code;
	private String detail;

	/**
	 * Creates a new Response using the string returned by the server.
	 * 
	 * @param responseString
	 *            the string returned by the server
	 * @throws SRCPUnsufficientDataException
	 * @throws NumberFormatException
	 */
	public Response(final String responseString)
			throws SRCPUnsufficientDataException {
		final TokenizedLine line = new TokenizedLine(responseString);
		try {
			timestamp = line.nextDoubleToken();
			code = line.nextIntToken();
			final StringBuffer detailTokens = new StringBuffer();
			while (line.hasMoreElements()) {
				if (detailTokens.length() > 0) {
					detailTokens.append(' ');
				}
				detailTokens.append(line.nextStringToken());
			}
		} catch (final NumberFormatException e) {
			LOGGER.error("error parsing response: " + responseString);
			throw new SRCPUnsufficientDataException(e);
		}
	}

	/**
	 * @return the timestamp
	 */
	public double getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}
}
