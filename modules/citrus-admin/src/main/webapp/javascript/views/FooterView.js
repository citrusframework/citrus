(function() {
    define(["TemplateManager", "views/LoggerView"], function(TemplateManager, LoggerView) {
        var FooterView = Backbone.View.extend({
        
          render: function() {
              $(this.el).html(TemplateManager.template('FooterView',{}));

              $('#logger').html(new LoggerView({ model: CitrusWebSocket }).render().el);
              
              return this;
          }
        
        });
        
        return FooterView;
    });
}).call(this);