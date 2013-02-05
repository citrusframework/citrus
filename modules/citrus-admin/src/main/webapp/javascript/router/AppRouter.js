(function() {
    define(["views/HeaderView", "views/WelcomeView", "views/TestListView", "views/FooterView"], function(HeaderView, WelcomeView, TestListView, FooterView) {
        var AppRouter = Backbone.Router.extend({
        
          routes: {
            "": "welcome", // #welcome
            "config": "config", //#config
            "testcases": "testcases", // #testcases
            "stats": "stats", // #stats
            "about": "about" // #about
          },
        
          initialize: function() {
              var headerView = new HeaderView({el: $('#header')});
              headerView.render();
              var footerView = new FooterView({el: $('#footer')});
              footerView.render();
          },
          
          welcome: function() {
              var welcomeView = new WelcomeView({el: $('#content')});
              welcomeView.render();
          },
          
          config: function() {
              $('#content').html('<div class="container"><h1 class="page-header">Configuration <small>Manage project settings</small></h1><br/><p>Not implemented yet! Coming soon!</p></div>');
          },
        
          testcases: function() {
              var testListView = new TestListView({el: $('#content')});
              testListView.render();
          },
          
          stats: function() {
              $('#content').html('<div class="container"><h1 class="page-header">Statistics <small>Manage project statistics</small></h1><br/><p>Not implemented yet! Coming soon!</p></div>');
          },
          
          about: function() {
              $('#content').html('<div class="container"><h1 class="page-header">About <small>Behind the scenes</small></h1><br/><p>Not implemented yet! Coming soon!</p></div>');
          }
        
        });
        
        return AppRouter;
    });
}).call(this);