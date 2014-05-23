var CitrusAdmin;
var CitrusWebSocket;

define(["TemplateManager", "AppRouter", "WebSocketHolder"], function (TemplateManager, AppRouter, WebSocketHolder) {
    beforeEach(function () {
        if (!CitrusWebSocket) {
            // we need to stub the websocket client, since HtmlUnit does not
            // have full support for websockets (a NullPotinerException is thrown).
            sinon.stub(window, "WebSocket");
            CitrusWebSocket = new WebSocketHolder();
        }

        if (!CitrusAdmin) {
            $('body').append(readFixtures("admin-body.html"));

            // preload templates as we use sinon fake server below and then lazy loading will return preloaded template 
            // otherwise we would get 404 not found from fake server
            TemplateManager.load([
                "ConfigView",
                "SchemaEditView",
                "SchemaTableView",
                "SchemaListView",
                "FooterView",
                "HeaderView",
                "LoggerView",
                "OpenProjectView",
                "TestDetailsView",
                "TestListView",
                "TestTableView",
                "WebSocketView",
                "ProjectView"], function () {
            });

            CitrusAdmin = new AppRouter();

            Backbone.history.start();
        }
    });
});
