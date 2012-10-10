(function() {
    define(["jquery", "backbone", "TemplateManager"], function($, Backbone, TemplateManager) {
        var HeaderView = Backbone.View.extend({
        
          render: function() {
              $(this.el).html(TemplateManager.template('HeaderView',{}));
              return this;
          }
        
        });
        
        return HeaderView;
    });
}).call(this);