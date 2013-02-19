(function() {
    define(["TemplateManager", "views/LoggerView"], function(TemplateManager, LoggerView) {
        var FooterView = Backbone.View.extend({
        
          render: function() {
              $(this.el).html(TemplateManager.template('FooterView',{}));

              $('#logger').html(this.getLoggerView().render().el);
              
              return this;
          },
        
          getLoggerView: function() {
              if(this.loggerView == null) {
                  this.loggerView = new LoggerView({ model: CitrusWebSocket });
              }
              return this.loggerView;
          }

        });
        
        return FooterView;
    });
}).call(this);