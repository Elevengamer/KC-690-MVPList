package de.elevengamer.pages;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.elevengamer.classes.MvpRun;
import de.elevengamer.classes.Player;
import de.elevengamer.classes.PlayerMvpState;
import de.elevengamer.utils.CsvUtil;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import de.elevengamer.HomePage;

public class CreateMvpRunPage extends WebPage {
    private static final String MVP_RUN_JSON = "data/MvpRun.json";
    private List<MvpRun> mvpRuns;
    private MvpRun selectedRun;
    private Integer runId;

    public CreateMvpRunPage(final PageParameters parameters) {
        super(parameters);
        loadMvpRuns();

        // Form to create a new MVP Run
        Form<Void> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                if (runId != null) {
                    if (mvpRuns.stream().anyMatch(run -> run.getRunId() == runId)) {
                        error("Run with ID " + runId + " already exists.");
                        return;
                    }
                    List<Player> players = CsvUtil.INSTANCE.getPlayers();
                    MvpRun newRun = MvpRun.Companion.create(runId, players);
                    mvpRuns.add(newRun);
                    saveMvpRuns();
                    // Reset fields and update components
                    runId = null;
                    setResponsePage(CreateMvpRunPage.class);
                }
            }
        };
        form.add(new TextField<>("runId", new PropertyModel<>(this, "runId")));
        add(form);

        WebMarkupContainer mvpStatesTable = new WebMarkupContainer("mvpStatesTable");
        mvpStatesTable.setOutputMarkupId(true);
        add(mvpStatesTable);


        DropDownChoice<MvpRun> runsDropDown = new DropDownChoice<>("runs", new PropertyModel<>(this, "selectedRun"),
                mvpRuns, new ChoiceRenderer<>("runId"));
        runsDropDown.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                mvpStatesTable.setVisible(selectedRun != null);
                target.add(mvpStatesTable);
            }
        });
        add(runsDropDown);

        ListView<PlayerMvpState> mvpStatesListView = new ListView<PlayerMvpState>("mvpStates", new LoadableDetachableModel<List<PlayerMvpState>>() {
            @Override
            protected List<PlayerMvpState> load() {
                return selectedRun != null ? selectedRun.getPlayers() : Collections.emptyList();
            }
        }) {
            @Override
            protected void populateItem(ListItem<PlayerMvpState> item) {
                PlayerMvpState playerState = item.getModelObject();
                item.add(new Label("playerName", playerState.getPlayerName()));
                item.add(new Label("event", playerState.getEvent() != null ? playerState.getEvent().getEventName() : ""));
                item.add(new Label("rank", playerState.getRank() != null ? playerState.getRank().toString() : ""));
                item.add(new Label("eventDate", playerState.getEventDate() != null ? playerState.getEventDate() : ""));
                item.add(new Label("hasMvp", String.valueOf(playerState.getHasMvp())));
                item.add(new Label("isMvpBanned", String.valueOf(playerState.isMvpBanned())));
                item.add(new Label("reasonForBan", playerState.getReasonForBan() != null ? playerState.getReasonForBan() : ""));
            }
        };
        mvpStatesTable.add(mvpStatesListView.setOutputMarkupId(true));
        mvpStatesTable.setVisible(selectedRun != null);

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