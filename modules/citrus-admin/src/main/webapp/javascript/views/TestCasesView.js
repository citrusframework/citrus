(function() {
    define(["jquery", "backbone", "TemplateManager"], function($, Backbone, TemplateManager) {
        var TestCasesView = Backbone.View.extend({
    
          render: function() {
              $(this.el).html(TemplateManager.template('TestCasesView',{}));
              return this;
          }
    
        });
        
        return TestCasesView;
    });
}).call(this);