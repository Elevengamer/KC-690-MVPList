package de.elevengamer.pages;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.elevengamer.classes.MvpRun;
import de.elevengamer.classes.PlayerMvpState;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import de.elevengamer.HomePage;

public class ManageBanPage extends WebPage {

    private static final String MVP_RUN_JSON = "data/MvpRun.json";

    private List<MvpRun> mvpRuns;
    private MvpRun selectedRun;
    private PlayerMvpState selectedPlayerState;
    private String banReason;

    public ManageBanPage() {
        loadMvpRuns();

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        Form<Void> banForm = new Form<>("banForm");
        add(banForm);

        WebMarkupContainer playerSelectionContainer = new WebMarkupContainer("playerSelectionContainer");
        playerSelectionContainer.setOutputMarkupId(true);
        playerSelectionContainer.setVisible(false); // Initially hidden
        banForm.add(playerSelectionContainer);

        // Dropdown for selecting the MVP Run
        ChoiceRenderer<MvpRun> runRenderer = new ChoiceRenderer<>("runId", "runId");
        DropDownChoice<MvpRun> runsDropDown = new DropDownChoice<>("runsDropDown",
                new PropertyModel<>(this, "selectedRun"),
                mvpRuns,
                runRenderer);

        banForm.add(runsDropDown);

        // Dropdown for players
        ChoiceRenderer<PlayerMvpState> playerRenderer = new ChoiceRenderer<>("playerName", "playerName");
        DropDownChoice<PlayerMvpState> playersDropDown = new DropDownChoice<>("playersDropDown",
                new PropertyModel<>(this, "selectedPlayerState"),
                Collections.emptyList(), // Initially empty
                playerRenderer);
        playersDropDown.setOutputMarkupId(true);
        playerSelectionContainer.add(playersDropDown);

        // Label for ban status
        Label banStatusLabel = new Label("banStatusLabel", new PropertyModel<>(this, "banStatusText"));
        banStatusLabel.setOutputMarkupId(true);
        playerSelectionContainer.add(banStatusLabel);

        // Text field for ban reason
        TextField<String> reasonInput = new TextField<>("reasonInput", new PropertyModel<>(this, "banReason"));
        reasonInput.setOutputMarkupId(true);
        playerSelectionContainer.add(reasonInput);

        // AJAX update behavior for runs dropdown
        runsDropDown.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (selectedRun != null) {
                    playersDropDown.setChoices(selectedRun.getPlayers());
                    playerSelectionContainer.setVisible(true);
                    selectedPlayerState = null; // Reset player selection
                } else {
                    playersDropDown.setChoices(Collections.emptyList());
                    playerSelectionContainer.setVisible(false);
                }
                target.add(playerSelectionContainer);
            }
        });

        // AJAX update behavior for players dropdown
        playersDropDown.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // Refresh the whole container to ensure all related state is updated visually
                target.add(playerSelectionContainer);
            }
        });


        // Ban Button
        AjaxButton banButton = new AjaxButton("banButton", banForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (selectedRun == null || selectedPlayerState == null) {
                    error("Please select a run and a player.");
                    target.add(feedbackPanel);
                    return;
                }
                if (banReason == null || banReason.trim().isEmpty()) {
                    error("Please provide a reason for the ban.");
                    target.add(feedbackPanel);
                    return;
                }
                try {
                    selectedRun.setPlayerBanned(selectedPlayerState.getPlayerName(), banReason);
                    saveMvpRuns();
                    success("Player '" + selectedPlayerState.getPlayerName() + "' has been banned.");
                    banReason = null; // Clear model
                } catch (IllegalStateException e) {
                    error(e.getMessage());
                }
                target.add(playerSelectionContainer); // Refresh container to show new status and clear input
                target.add(feedbackPanel);
            }
        };
        playerSelectionContainer.add(banButton);

        // Remove Ban Button
        AjaxButton removeBanButton = new AjaxButton("removeBanButton", banForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                 if (selectedRun == null || selectedPlayerState == null) {
                    error("Please select a run and a player.");
                    target.add(feedbackPanel);
                    return;
                }
                try {
                    selectedRun.removeBan(selectedPlayerState.getPlayerName());
                    saveMvpRuns();
                    success("Ban for player '" + selectedPlayerState.getPlayerName() + "' has been removed.");
                } catch (IllegalStateException e) {
                    error(e.getMessage());
                }
                target.add(playerSelectionContainer); // Refresh container to show new status
                target.add(feedbackPanel);
            }
        };
        playerSelectionContainer.add(removeBanButton);

        add(new Link<Void>("homePageLink") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });
    }

    public String getBanStatusText() {
        if (selectedPlayerState != null) {
            if (selectedPlayerState.isMvpBanned()) {
                return "BANNED - Reason: " + selectedPlayerState.getReasonForBan();
            } else {
                return "Not banned.";
            }
        }
        return "No player selected.";
    }


    private void loadMvpRuns() {
        File file = new File(MVP_RUN_JSON);
        if (file.exists() && file.length() > 0) {
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<MvpRun>>() {}.getType();
                mvpRuns = new Gson().fromJson(reader, listType);
                if (mvpRuns == null) {
                    mvpRuns = new ArrayList<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
                mvpRuns = new ArrayList<>();
            }
        } else {
            mvpRuns = new ArrayList<>();
        }
    }

    private void saveMvpRuns() {
        try (FileWriter writer = new FileWriter(MVP_RUN_JSON)) {
            new Gson().toJson(mvpRuns, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}