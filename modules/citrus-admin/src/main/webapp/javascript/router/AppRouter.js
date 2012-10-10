(function() {
    define(["jquery", "underscore", "backbone", "handlebars", "views/HeaderView", "views/AppContextView", "views/TestCasesView"], function($, _, Backbone, Handlebars, HeaderView, AppContextView, TestCasesView) {
        var AppRouter = Backbone.Router.extend({
        
          routes: {
            "": "appcontext", // #appcontext
            "testcases": "testcases", // #testcases
            "about": "about" // #about
          },
        
          initialize: function() {
              var headerView = new HeaderView();
              $('.header').html(headerView.render().el);
          },
          
          appcontext: function() {
              var appContextView = new AppContextView();
              $('#content').html(appContextView.render().el);
          },
        
          testcases: function() {
              var testCasesView = new TestCasesView();
              $('#content').html(testCasesView.render().el);
          },
          
          about: function() {
              
          }
        
        });
        
        return AppRouter;
    });
}).call(this);