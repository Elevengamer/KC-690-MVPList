package de.elevengamer.pages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.elevengamer.HomePage;
import de.elevengamer.classes.Event;
import de.elevengamer.classes.EventScore;
import de.elevengamer.classes.Player;
import de.elevengamer.utils.CsvUtil;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddEventScorePage extends WebPage {

    private EventScore eventScore;
    private final Model<String> playerModel = new Model<>();
    private final Model<String> scoreModel = new Model<>();
    private final Model<Integer> rankModel = new Model<>();
    private final List<EventScore.ps> playerScores = new ArrayList<>();

    public AddEventScorePage(final PageParameters parameters) {
        super(parameters);

        eventScore = new EventScore(Event.Nibelung_Int, "");

        Form<Void> form = new Form<>("form");
        add(form);

        DropDownChoice<Event> eventDropDown = new DropDownChoice<>("event", new PropertyModel<>(eventScore, "event"), Arrays.asList(Event.values()));
        form.add(eventDropDown);

        TextField<String> dateField = new TextField<>("date", new PropertyModel<>(eventScore, "date"));
        form.add(dateField);

        List<String> playerNames = new ArrayList<>();
        for (Player player : CsvUtil.INSTANCE.getPlayers()) {
            playerNames.add(player.getName());
        }

        DropDownChoice<String> playerDropDown = new DropDownChoice<>("player", playerModel, playerNames);
        form.add(playerDropDown);

        TextField<String> scoreField = new TextField<>("score", scoreModel);
        form.add(scoreField);

        NumberTextField<Integer> rankField = new NumberTextField<>("rank", rankModel, Integer.class);
        form.add(rankField);

        ListView<EventScore.ps> scoresView = new ListView<EventScore.ps>("scores", playerScores) {
            @Override
            protected void populateItem(ListItem<EventScore.ps> item) {
                item.add(new Label("playername", new PropertyModel<>(item.getModel(), "playername")));
                item.add(new Label("playerscore", new PropertyModel<>(item.getModel(), "score")));
                item.add(new Label("playerrank", new PropertyModel<>(item.getModel(), "rank")));
                item.add(new Button("remove") {
                    @Override
                    public void onSubmit() {
                        playerScores.remove(item.getModelObject());
                    }
                });
            }
        };
        scoresView.setReuseItems(true);
        form.add(scoresView);

        form.add(new Button("addPlayer") {
            @Override
            public void onSubmit() {
                try {
                    playerScores.add(new EventScore.ps(playerModel.getObject(), scoreModel.getObject(), rankModel.getObject()));
                    playerModel.setObject(null);
                    scoreModel.setObject(null);
                    rankModel.setObject(null);
                } catch (ClassCastException e) {
                    error("Rank must be a valid number.");
                }
            }
        });

        form.add(new Button("save") {
            @Override
            public void onSubmit() {
                eventScore.getScores().addAll(playerScores);
                saveEventScore(eventScore);
                setResponsePage(HomePage.class);
            }
        });

        form.add(new FeedbackPanel("feedback"));
    }

    private void saveEventScore(EventScore eventScore) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File("data/EventScores.json");
        List<EventScore> scores = new ArrayList<>();

        if (file.exists() && file.length() > 0) {
            try (FileReader reader = new FileReader(file)) {
                scores = gson.fromJson(reader, new TypeToken<List<EventScore>>() {}.getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (scores == null) {
            scores = new ArrayList<>();
        }

        scores.add(eventScore);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(scores, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
