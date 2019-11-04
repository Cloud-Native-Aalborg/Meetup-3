package dk.slyng.meetup.meetup3.frontend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class FrontendResource {

    private final RestTemplateBuilder restTemplateBuilder;
    @Value("${quote-service.url}")
    private String quoteServiceUrl;

    public FrontendResource(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }


    @GetMapping("/quote/random")
    public Quote quoteRandom() {
        log.info("Random quote is requested");


        final String uri = quoteServiceUrl + "/quote/random";

        log.debug("Getting quote from {}", uri);

        return restTemplateBuilder.build().getForObject(uri, Quote.class);
    }

    @GetMapping("/quote/fail")
    public Quote quoteFail() {
        throw new FailException();
    }
}
