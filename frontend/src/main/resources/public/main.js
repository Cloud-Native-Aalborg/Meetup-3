function getQuote(fail) {
    console.log("getting quote...");

    var path = "/quote/random";
    if (fail) {
        path = "quote/fail";
    }

    $.getJSON({
        url: window.location.origin + path,
        cache: false,
        success: function (result) {
            $("#quote").html(result['quote']);
            $("#author").html("- " + result['author']);
        },
        error: function () {
            $("#quote").html("Oh no - error getting quote!");
            $("#author").html("");
        }
    }).always(function (data, textStatus, xhr) {
        if (typeof xhr.getResponseHeader !== "undefined")
            $("#traceId").html(xhr.getResponseHeader("Trace-Id"));
    });
}

$(document).ready(function () {
    $("#newButton").on("click", function () {
        getQuote(false);
    });
    setInterval(function () {
        getQuote(false);
    }, 30000);
    getQuote();

    $("#failButton").on("click", function () {
        getQuote(true);
    });
});
