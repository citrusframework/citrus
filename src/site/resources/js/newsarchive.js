$(document).ready(function () {
    $.get('news/feed', '', function (data) {
        $(data).find("entry").each(function () {
            var entry = $(this);

            if (entry.find("title").text().indexOf("Citrus") > -1 || entry.find("content").text().indexOf("Citrus") > -1 || entry.find("content").text().indexOf("citrus") > -1) {
                $('ul.newsfeed').append('<li class="rssRow"><h3> <a target="_blank"' +
                    'title="' + entry.find("title").text() +
                    '" href="' + entry.find("link").attr("href") + '">' +
                    entry.find("title").text() +
                    '</a></h3><div>' + entry.find("published").text() +
                    '</div>' + $('<div/>').html(entry.find("content").text()).text() + ' <a target="_blank"' +
                    'title="' + entry.find("title").text() +
                    '" href="' + entry.find("link").attr("href") + '">[more]</a></li>');
            }
        });

        return false;
    }, 'xml');
});