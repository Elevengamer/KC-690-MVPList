package de.elevengamer.pages;

import de.elevengamer.HomePage;
import de.elevengamer.WicketApplication;
import de.elevengamer.classes.MvpRun;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ConvertRunToHtmlPage extends WebPage {

    private MvpRun selectedRun;

    public ConvertRunToHtmlPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        List<MvpRun> mvpRuns = MvpRun.Companion.loadMvpRuns(WicketApplication.MVP_RUN_FILE);

        DropDownChoice<MvpRun> runsDropDown = new DropDownChoice<>("runsDropDown", new PropertyModel<>(this, "selectedRun"), mvpRuns, new ChoiceRenderer<>("runId"));
        runsDropDown.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // You can add logic here if needed when the selection changes
            }
        });
        add(runsDropDown);

        Form<Void> convertForm = new Form<>("convertForm");
        add(convertForm);

        WebMarkupContainer generatedFileLinkContainer = new WebMarkupContainer("generatedFileLinkContainer");
        generatedFileLinkContainer.setOutputMarkupId(true);
        generatedFileLinkContainer.setVisible(false);
        add(generatedFileLinkContainer);

        convertForm.add(new AjaxButton("convertButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (selectedRun != null) {
                    try {
                        String generatedHtmlFileName = "mvp_run_" + selectedRun.getRunId() + ".html";
                        File generatedFile = new File("docs/tables/" + generatedHtmlFileName);
                        FileWriter writer = new FileWriter(generatedFile);
                        writer.write(generateHtmlContent(selectedRun));
                        writer.close();

                        // Now, add a link to docs/index.html
                        File indexHtmlFile = new File("docs/index.html");
                        if (!indexHtmlFile.exists()) {
                            // Create a basic index.html if it doesn't exist
                            FileWriter indexWriter = new FileWriter(indexHtmlFile);
                            indexWriter.write("<!DOCTYPE html>\n" +
                                    "<html lang=\"en\">\n" +
                                    "<head>\n" +
                                    "    <meta charset=\"UTF-8\">\n" +
                                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                                    "    <title>MVP Reports</title>\n" +
                                    "    <style>\n" +
                                    "        body { font-family: sans-serif; margin: 20px; }\n" +
                                    "        h1 { color: #333; }\n" +
                                    "        ul { list-style-type: none; padding: 0; }\n" +
                                    "        li { margin-bottom: 10px; }\n" +
                                    "        a { text-decoration: none; color: #007bff; }\n" +
                                    "        a:hover { text-decoration: underline; } \n" +
                                    "    </style>\n" +
                                    "</head>\n" +
                                    "<body>\n" +
                                    "    <h1>Generated MVP Run Reports</h1>\n" +
                                    "    <ul id=\"mvp-report-links\">\n" +
                                    "        <!-- Links to MVP run reports will be added here -->\n" +
                                    "    </ul>\n" +
                                    "</body>\n" +
                                    "</html>");
                            indexWriter.close();
                        }

                        String indexHtmlContent = new String(Files.readAllBytes(indexHtmlFile.toPath()), StandardCharsets.UTF_8);
                        String linkToAdd = "<li><a href=\"tables/" + generatedHtmlFileName + "\">MVP Run " + selectedRun.getRunId() + "</a></li>";
                        String updatedIndexHtmlContent;

                        // Check if the link already exists to avoid duplicates
                        if (!indexHtmlContent.contains(linkToAdd)) {
                            updatedIndexHtmlContent = indexHtmlContent.replace(
                                "<!-- Links to MVP run reports will be added here -->",
                                linkToAdd + "\n        <!-- Links to MVP run reports will be added here -->"
                            );
                            FileWriter indexFileWriter = new FileWriter(indexHtmlFile);
                            indexFileWriter.write(updatedIndexHtmlContent);
                            indexFileWriter.close();
                        }

                        DownloadLink generatedFileLink = new DownloadLink("generatedFileLink", generatedFile);
                        generatedFileLink.setBody(Model.of(generatedHtmlFileName));
                        generatedFileLinkContainer.addOrReplace(generatedFileLink);
                        generatedFileLinkContainer.setVisible(true);

                        success("HTML file generated successfully and link added to index.html!");



                    } catch (IOException e) {
                        error("Error generating HTML file or updating index.html: " + e.getMessage());
                    }
                } else {
                    error("Please select an MVP Run.");
                }
                target.add(feedback);
                target.add(generatedFileLinkContainer);
            }
        });

        add(new Link<Void>("homePageLink") {
            @Override
            public void onClick() {
                setResponsePage(HomePage.class);
            }
        });
    }

    private String generateHtmlContent(MvpRun run) {
        StringBuilder html = new StringBuilder();
        html.append("<html>\n");
        html.append("<head><title>MVP Run ").append(run.getRunId()).append("</title>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"../styles/style.css\">");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"maincontainer\">");
        html.append("<h1>MVP Run ").append(run.getRunId()).append("</h1>\n");
        html.append("<table border=\"1\">\n");
        html.append("<tr><th>Player Name</th><th>Event</th><th>Rank</th><th>Event Date</th><th>Has MVP</th><th>Is MVP Banned</th><th>Reason for Ban</th></tr>\n");
        run.getPlayers().forEach(player -> {
            html.append("<tr>");
            html.append("<td>").append(player.getPlayerName()).append("</td>");
            html.append("<td>").append(player.getEvent() != null ? player.getEvent().getEventName() : "").append("</td>");
            html.append("<td>").append(player.getRank() != null ? player.getRank() : "").append("</td>");
            html.append("<td>").append(player.getEventDate() != null ? player.getEventDate() : "").append("</td>");
            html.append("<td>").append(player.getHasMvp()).append("</td>");
            html.append("<td>").append(player.isMvpBanned()).append("</td>");
            html.append("</tr>\n");
        });
        html.append("</table>\n");
        html.append("</div>")
        html.append("</body>\n");
        html.append("</html>");
        return html.toString();
    }
}

