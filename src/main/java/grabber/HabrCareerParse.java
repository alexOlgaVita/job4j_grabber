package grabber;

import grabber.model.Post;
import grabber.utils.DateTimeParser;
import grabber.utils.HabrCareerDateTimeParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGE_COUNT = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> result = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= PAGE_COUNT; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(link, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String postLink = String.format("%s%s ", link, linkElement.attr("href"));
                Element dateTimeElement = row.select(".vacancy-card__date").first();
                String dateTime = dateTimeElement.child(0).attr("datetime");
                HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
                try {
                    result.add(new Post(vacancyName, postLink, retrieveDescription(postLink), dateTimeParser.parse(dateTime)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return result;
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        String result = "";
        Optional<String> description = document.select(".basic-section--appearance-vacancy-description")
                .first()
                .children()
                .eachText()
                .stream()
                .reduce((x, y) -> x + System.lineSeparator() + y);
        return description.orElse("");
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrcareerparse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> postList = habrcareerparse.list(SOURCE_LINK);
        for (Post post : postList) {
            System.out.println(post);
        }
    }
}
