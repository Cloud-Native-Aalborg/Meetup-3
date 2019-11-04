package dk.slyng.meetup.meetup3.quote.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class Quote {
    @JsonProperty
    private String quote;
    @JsonProperty
    private String author;
}
