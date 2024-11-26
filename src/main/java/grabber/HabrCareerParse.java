package grabber;

import grabber.utils.HabrCareerDateTimeParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGE_COUNT = 5;

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        String result = "";
//        Optional<String> sentence = document.select(".basic-section basic-section--appearance-vacancy-description")
        Optional<String> sentence = document.select(".basic-section--appearance-vacancy-description")
                .first()
                .children()
                .eachText()
                .stream()
                .reduce((x, y) -> System.lineSeparator() + x + System.lineSeparator() + y);
        return sentence.get();
    }

    public static void main(String[] args) throws IOException {
        for (int pageNumber = 1; pageNumber <= PAGE_COUNT; pageNumber++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s ", SOURCE_LINK, linkElement.attr("href"));
                Element dateTimeElement = row.select(".vacancy-card__date").first();
                String dateTime = dateTimeElement.child(0).attr("datetime");
                HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
                System.out.printf("%s %s %s %n", vacancyName, link,
                        parser.parse(dateTime).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
                try {
                    System.out.println(retrieveDescription(link));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
