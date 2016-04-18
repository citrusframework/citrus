$(document).ready(function () {
    $.get('news/feed', '', function (data) {
        var entries = 0;

        $(data).find("entry").each(function () {
            var entry = $(this);

            if (entries < 5 && (entry.find("title").text().indexOf("Citrus") > -1 || entry.find("content").text().indexOf("Citrus") > -1 || entry.find("content").text().indexOf("citrus") > -1)) {
                $('ul.newsfeed').append('<li class="rssRow"><h4> <a target="_blank"' +
                    'title="' + entry.find("title").text() +
                    '" href="' + entry.find("link").attr("href") + '">' +
                    entry.find("title").text() +
                    '</a></h4>' + $('<div/>').html(entry.find("content").text()).text().substring(0, 100) + ' ...</li>');

                entries++;
            }
        });

        return false;
    }, 'xml');
});