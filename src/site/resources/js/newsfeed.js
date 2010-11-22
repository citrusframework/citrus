$(document).ready(function () {
    $('#newsfeed').rssfeed('http://labs.consol.de/tags/citrus/feed/', {
      header: false,
      limit: 3
    });
});