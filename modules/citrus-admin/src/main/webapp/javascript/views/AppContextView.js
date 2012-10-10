(function() {
    define(["jquery", "backbone", "TemplateManager"], function($, Backbone, TemplateManager) {
        var AppContextView = Backbone.View.extend({

          render: function() {
              $(this.el).html(TemplateManager.template('AppContextView',{}));
              return this;
          }

        });
        
        return AppContextView;
    });
}).call(this);