(function() {
    define(["views/HeaderView", "views/AppContextView", "views/TestListView"], function(HeaderView, AppContextView, TestListView) {
        var AppRouter = Backbone.Router.extend({
        
          routes: {
            "": "appcontext", // #appcontext
            "testcases": "testcases", // #testcases
            "about": "about" // #about
          },
        
          initialize: function() {
              var headerView = new HeaderView({el: $('#header')});
              headerView.render();
          },
          
          appcontext: function() {
              var appContextView = new AppContextView({el: $('#content')});
              appContextView.render();
          },
        
          testcases: function() {
              var testListView = new TestListView({el: $('#content')});
              testListView.render();
              testListView.afterRender();
          },
          
          about: function() {
              
          }
        
        });
        
        return AppRouter;
    });
}).call(this);