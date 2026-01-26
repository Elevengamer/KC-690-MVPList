package de.elevengamer.pages;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.elevengamer.classes.Event;
import de.elevengamer.classes.MvpRun;
import de.elevengamer.classes.PlayerMvpState;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import de.elevengamer.HomePage;

public class AddMvpPage extends WebPage {

    private static final String MVP_RUN_JSON = "data/MvpRun.json";

    private List<MvpRun> mvpRuns;
    private MvpRun selectedRun;

    // Models for the form
    private PlayerMvpState selectedPlayer;
    private Event selectedEvent;
    private Integer rank;
    private String eventDate;

    public AddMvpPage() {
        loadMvpRuns();

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        WebMarkupContainer mvpContainer = new WebMarkupContainer("mvpContainer");
        mvpContainer.setOutputMarkupId(true);
        mvpContainer.setVisible(false); // Hide until a run is selected
        add(mvpContainer);

        // Dropdown to select the run
        ChoiceRenderer<MvpRun> runRenderer = new ChoiceRenderer<>("runId", "runId");
        DropDownChoice<MvpRun> runsDropDown = new DropDownChoice<>("runsDropDown",
                new PropertyModel<>(this, "selectedRun"),
                mvpRuns,
                runRenderer);
        runsDropDown.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                mvpContainer.setVisible(selectedRun != null);
                target.add(mvpContainer);
            }
        });
        add(runsDropDown);


        // Form for adding a new MVP
        Form<Void> addMvpForm = new Form<>("addMvpForm");
        mvpContainer.add(addMvpForm);

        IModel<List<PlayerMvpState>> eligiblePlayersModel = new LoadableDetachableModel<List<PlayerMvpState>>() {
            @Override
            protected List<PlayerMvpState> load() {
                if (selectedRun == null) {
                    return Collections.emptyList();
                }
                // Players who are not banned and do not have an MVP yet
                return selectedRun.getPlayers().stream()
                        .filter(p -> !p.isMvpBanned() && !p.getHasMvp())
                        .collect(Collectors.toList());
            }
        };

        ChoiceRenderer<PlayerMvpState> playerRenderer = new ChoiceRenderer<>("playerName", "playerName");
        DropDownChoice<PlayerMvpState> playersDropDown = new DropDownChoice<>("playersDropDown",
                new PropertyModel<>(this, "selectedPlayer"),
                eligiblePlayersModel,
                playerRenderer);
        playersDropDown.setRequired(true);
        addMvpForm.add(playersDropDown);

        ChoiceRenderer<Event> eventRenderer = new ChoiceRenderer<>("EventName");
        DropDownChoice<Event> eventsDropDown = new DropDownChoice<>("eventsDropDown",
                new PropertyModel<>(this, "selectedEvent"),
                Arrays.asList(Event.values()),
                eventRenderer);
        eventsDropDown.setRequired(true);
        addMvpForm.add(eventsDropDown);

        NumberTextField<Integer> rankInput = new NumberTextField<>("rankInput", new PropertyModel<>(this, "rank"), Integer.class);
        rankInput.setRequired(true);
        rankInput.setMinimum(1);
        addMvpForm.add(rankInput);

        TextField<String> dateInput = new TextField<>("dateInput", new PropertyModel<>(this, "eventDate"));
        dateInput.setRequired(true);
        addMvpForm.add(dateInput);

        addMvpForm.add(new AjaxButton("addMvpButton", addMvpForm) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    selectedRun.assignMvp(selectedPlayer.getPlayerName(), selectedEvent, rank, eventDate);
                    saveMvpRuns();
                    success("MVP added for " + selectedPlayer.getPlayerName());
                    // Reset form models
                    selectedPlayer = null;
                    selectedEvent = null;
                    rank = null;
                    eventDate = null;
                } catch (IllegalStateException e) {
                    error(e.getMessage());
                }
                target.add(mvpContainer); // Refresh the whole container
                target.add(feedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(feedbackPanel);
            }
        });

        // Container for the list of assigned MVPs
        WebMarkupContainer assignedMvpsListContainer = new WebMarkupContainer("assignedMvpsListContainer");
        assignedMvpsListContainer.setOutputMarkupId(true);
        mvpContainer.add(assignedMvpsListContainer);

        IModel<List<PlayerMvpState>> assignedPlayersModel = new LoadableDetachableModel<List<PlayerMvpState>>() {
            @Override
            protected List<PlayerMvpState> load() {
                if (selectedRun == null) {
                    return Collections.emptyList();
                }
                // Players who have an MVP
                return selectedRun.getPlayers().stream()
                        .filter(PlayerMvpState::getHasMvp)
                        .collect(Collectors.toList());
            }
        };

        ListView<PlayerMvpState> assignedMvpsListView = new ListView<PlayerMvpState>("assignedMvpsListView", assignedPlayersModel) {
            @Override
            protected void populateItem(ListItem<PlayerMvpState> item) {
                PlayerMvpState playerState = item.getModelObject();
                item.add(new Label("playerName", playerState.getPlayerName()));
                item.add(new Label("event", playerState.getEvent().getEventName()));
                item.add(new Label("rank", playerState.getRank()));
                item.add(new Label("eventDate", new PropertyModel<>(item.getModel(), "eventDate")));

                AjaxLink<Void> removeLink = new AjaxLink<Void>("removeMvpLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        try {
                            selectedRun.removeMvp(playerState.getPlayerName());
                            saveMvpRuns();
                            info("Removed MVP from " + playerState.getPlayerName());
                        } catch (IllegalStateException e) {
                            error(e.getMessage());
                        }
                        target.add(mvpContainer);
                        target.add(feedbackPanel);
                    }
                };
                item.add(removeLink);
            }
        };
        assignedMvpsListContainer.add(assignedMvpsListView);
        add(new Link<Void>("homePageLink") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });
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