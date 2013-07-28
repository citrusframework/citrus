(function() {
    define(["views/HeaderView", "views/WelcomeView", "views/ConfigView", "views/TestListView", "views/FooterView"], function(HeaderView, WelcomeView, ConfigView, TestListView, FooterView) {
        var AppRouter = Backbone.Router.extend({

          headerView: undefined,
          footerView: undefined,
          welcomeView: undefined,
          configView: undefined,
          testListView: undefined,
          statsView: undefined,
          aboutView: undefined,

          routes: {
            "": "welcome", // #welcome
            "project": "welcome", // #welcome
            "config": "config", //#config
            "testcases": "testcases", // #testcases
            "testcases/:testName": "testDetails", // #testcases/EchoActionITest
            "stats": "stats", // #stats
            "about": "about" // #about
          },
        
          initialize: function() {
              this.headerView = new HeaderView({el: $('#header')});
              this.headerView.render();

              this.footerView = new FooterView({el: $('#footer')});
              this.footerView.render();
          },
          
          welcome: function() {
              if (!this.welcomeView) {
                  this.welcomeView = new WelcomeView({el: $('#welcome-content')});
                  this.welcomeView.render();
              }

              $('#content').children().hide();
              $('#welcome-content').show();
          },
          
          config: function() {
              if (!this.configView) {
                  this.configView = new ConfigView({el: $('#config-content')});
                  this.configView.render();
                  this.configView.afterRender();
              }

              $('#content').children().hide();
              $('#config-content').show();
          },
        
          testcases: function() {
              if (!this.testListView) {
                  this.testListView = new TestListView({el: $('#test-list-content')});
                  this.testListView.render();
              }

              $('#content').children().hide();
              $('#test-list-content').show();
          },

          testDetails: function(testName) {
              this.testcases();
              this.testListView.showDetails(testName);
          },
          
          stats: function() {
              if (!this.statsView) {
                  $('#stats-content').html('<div class="container-fluid"><h1 class="page-header">Statistics <small>Manage project statistics</small></h1><br/><p>Not implemented yet! Coming soon!</p></div>');
              }

              $('#content').children().hide();
              $('#stats-content').show();

          },

          about: function() {
              if (!this.statsView) {
                  $('#about-content').html('<div class="container-fluid"><h1 class="page-header">About <small>Behind the scenes</small></h1><br/><p>Not implemented yet! Coming soon!</p></div>');
              }

              $('#content').children().hide();
              $('#about-content').show();

          }
        
        });
        
        return AppRouter;
    });
}).call(this);