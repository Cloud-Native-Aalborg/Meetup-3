package dk.slyng.meetup.meetup3.quote.frontend;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
class FailException extends RuntimeException {
}
