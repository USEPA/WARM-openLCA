package gov.epa.warm.rcp.utils.webui;

/**
 * Interface for an HTML page used in the SWT browser widget.
 */
public interface IHtmlPage {

	/**
	 * Get the URL to the HTML page.
	 */
	String getUrl();

	/**
	 * Is executed when the page is ready in the browser (first time).
	 */
	void onLoaded();

	/**
	 * Is executed when the page is ready in the browser (all the other times).
	 */
	void onReloaded();

}
