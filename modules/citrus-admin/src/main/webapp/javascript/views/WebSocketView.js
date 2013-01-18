(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var WebSocketView = Backbone.View.extend({
        
            initialize: function() {
                this.model.on('change:status', this.render, this);
                this.model.on('change:onmessage', this.onSocketMessage, this);
                this.model.on('change:onmessage', this.onSocketErrorMessage, this);
            },
            
            render: function() {
                $(this.el).html(TemplateManager.template('WebSocketView', { socket: this.model }));
                return this;
            },
            
            onSocketMessage: function(message) {
            },
            
            onSocketErrorMessage: function(message) {
            }
        
        });
        
        return WebSocketView;
    });
}).call(this);