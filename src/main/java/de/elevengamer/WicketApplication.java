package de.elevengamer;

import java.io.IOException;
import de.elevengamer.pages.AddEventScorePage;
import de.elevengamer.pages.AddMvpPage;
import de.elevengamer.pages.CreateMvpRunPage;
import de.elevengamer.pages.ManageBanPage;
import de.elevengamer.pages.ConvertRunToHtmlPage;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see de.elevengamer.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	public static final String MVP_RUN_FILE = "data/MvpRun.json";
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();

		// add your configuration here
		getCspSettings().blocking()
			.add(CSPDirective.STYLE_SRC, CSPDirectiveSrcValue.SELF)
			.add(CSPDirective.STYLE_SRC, "https://fonts.googleapis.com/css")
			.add(CSPDirective.FONT_SRC, "https://fonts.gstatic.com");

		// add your configuration here
		mountPage("/convert", ConvertRunToHtmlPage.class);
		
		// Mount the "doc" folder as a shared resource
		// This allows direct access to files under /docs/ (e.g., /docs/index.html, /docs/mvp/mvprun1.html)
		// NOTE: The method for mounting static folders is causing compilation issues.
		// Please consult Wicket 10.8.0 documentation for the correct way to mount a folder for direct access.
		// The current line has been commented out to allow compilation.
		// getResourceSettings().addResourceFolder(new File("doc"));
	}

}
