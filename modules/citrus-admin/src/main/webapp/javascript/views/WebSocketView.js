(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var WebSocketView = Backbone.View.extend({

            initialize: function () {
                this.model.on('change:status', this.render, this);
                this.model.on('change:onerror', this.onSocketErrorMessage, this);
            },

            render: function () {
                $(this.el).html(TemplateManager.template('WebSocketView', { socket: this.model }));
                return this;
            },

            onSocketErrorMessage: function (message) {
                console.log(message.msg);
            }
        });

        return WebSocketView;
    });
}).call(this);