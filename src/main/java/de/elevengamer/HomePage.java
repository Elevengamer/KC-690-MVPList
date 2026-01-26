package de.elevengamer;

import de.elevengamer.pages.AddEventScorePage;
import de.elevengamer.pages.AddMvpPage;
import de.elevengamer.pages.CreateMvpRunPage;
import de.elevengamer.pages.ManageBanPage;
import de.elevengamer.pages.ConvertRunToHtmlPage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.basic.Label;

public class HomePage extends WebPage {
    public HomePage() {
        add(new Label("message", "Welcome to the Home Page!"));

        // Link to AddMVPPage
        add(new Link<Void>("addMvpLink") {
            @Override
            public void onClick() {
                setResponsePage(AddMvpPage.class);
            }
        });

        // Link to CreateMvpRunPage
        add(new Link<Void>("createMvpRunLink") {
            @Override
            public void onClick() {
                setResponsePage(CreateMvpRunPage.class);
            }
        });

        // Link to ManageBanPage
        add(new Link<Void>("manageBanLink") {
            @Override
            public void onClick() {
                setResponsePage(ManageBanPage.class);
            }
        });

        // Link to AddEventScorePage
        add(new Link<Void>("addEventScoreLink") {
            @Override
            public void onClick() {
                setResponsePage(AddEventScorePage.class);
            }
        });

        // Link to ConvertRunToHtmlPage
        add(new Link<Void>("convertRunLink") {
            @Override
            public void onClick() {
                setResponsePage(ConvertRunToHtmlPage.class);
            }
        });
    }
}
