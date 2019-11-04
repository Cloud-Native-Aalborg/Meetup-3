package dk.slyng.meetup.meetup3.quote.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;


@RestController
@Slf4j
public class QuoteResource {

    @GetMapping("/quote/random")
    public Quote quoteRandom() {
        log.info("Random quote is requested");
        List<Quote> quotes = getQuotes();

        Random random = new Random();

        return quotes.get(random.nextInt(quotes.size() - 1));
    }

    @GetMapping("/quote/fail")
    public Quote quoteFail() {
        log.warn("Failed to get quote.");
        throw new FailException();
    }


    private List<Quote> getQuotes() {
        JsonParser jsonParser = JsonParserFactory.getJsonParser();

        List<Object> objects = jsonParser.parseList(readQuoteFile());

        return objects.stream()
                .map(obj -> {
                    LinkedHashMap map = (LinkedHashMap) obj;
                    return new Quote(((String) map.get("quoteText")), ((String) map.get("quoteAuthor")));
                }).collect(Collectors.toList());
    }

    private String readQuoteFile() {
        InputStream stream = getClass().getResourceAsStream("/quotes.json");
        try (Reader reader = new InputStreamReader(stream, UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
