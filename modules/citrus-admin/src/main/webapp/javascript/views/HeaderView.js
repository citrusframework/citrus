(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var HeaderView = Backbone.View.extend({
        
          render: function() {
              $(this.el).html(TemplateManager.template('HeaderView',{}));
              
              $('ul.nav li').each(function(index) {
                  $(this).click(function() {
                      $('ul.nav li').removeClass('active');
                      $(this).addClass('active');
                  });
              });
              
              return this;
          }
        
        });
        
        return HeaderView;
    });
}).call(this);