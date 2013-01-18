(function() {
    define(["TemplateManager", "views/WebSocketView"], function(TemplateManager, WebSocketView) {
        var HeaderView = Backbone.View.extend({
        
          render: function() {
              $(this.el).html(TemplateManager.template('HeaderView',{}));
              
              $('ul.nav li').each(function(index) {
                  $(this).click(function() {
                      $('ul.nav li').removeClass('active');
                      $(this).addClass('active');
                  });
              });
              
              $('#web-socket').html(new WebSocketView({ model: CitrusWebSocket }).render().el);
              
              return this;
          }
        
        });
        
        return HeaderView;
    });
}).call(this);