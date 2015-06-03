(function() {
    define(["views/HeaderView", "views/ProjectView", "views/ConfigView", "views/TestListView", "views/FooterView"], function(HeaderView, ProjectView, ConfigView, TestListView, FooterView) {
        var AppRouter = Backbone.Router.extend({

          headerView: undefined,
          footerView: undefined,
          projectView: undefined,
          configView: undefined,
          testListView: undefined,
          statsView: undefined,
          settingsView: undefined,
          aboutView: undefined,

          routes: {
            "": "project", // #project
            "project": "project", // #project
            "config": "config", //#config
            "config/:page": "config", //#config/endpoints
            "tests": "tests", // #tests
            "tests/:testName": "testDetails", // #tests/EchoActionITest
            "stats": "stats", // #stats
            "settings": "settings", // #settings
            "about": "about" // #about
          },
        
          initialize: function() {
              this.headerView = new HeaderView({el: $('#header')});
              this.headerView.render();

              this.footerView = new FooterView({el: $('#footer')});
              this.footerView.render();
          },
          
          project: function() {
              if (!this.projectView) {
                  this.projectView = new ProjectView({el: $('#project-content')});
                  this.projectView.render();
              }

              $('#content').children().hide();
              $('#project-content').show();
          },
          
          config: function(page) {
              if (!this.configView) {
                  this.configView = new ConfigView({el: $('#config-content')});
                  this.configView.render();
                  this.configView.afterRender();
              }

              if (page) {
                  this.configView.show(page);
              }

              $('#content').children().hide();
              $('#config-content').show();
          },
        
          tests: function() {
              if (!this.testListView) {
                  this.testListView = new TestListView({el: $('#tests-content')});
                  this.testListView.render();
              }

              $('#content').children().hide();
              $('#tests-content').show();
          },

          testDetails: function(testName) {
              this.tests();
              this.testListView.showDetails(testName);
          },
          
          stats: function() {
              if (!this.statsView) {
                  $('#stats-content').html('<div class="container content-header"><h1>Statistics <small>Manage project statistics</small></h1></div><div class="container"><div class="content-nav"><p>Not implemented yet! Coming soon!</p></div></div>');
              }

              $('#content').children().hide();
              $('#stats-content').show();

          },

          settings: function() {
                if (!this.statsView) {
                    $('#settings-content').html('<div class="container content-header"><h1>Settings <small>Administration settings</small></h1></div><div class="container"><div class="content-nav"><p>Not implemented yet! Coming soon!</p></div></div>');
                }

                $('#content').children().hide();
                $('#settings-content').show();

            },

          about: function() {
              if (!this.statsView) {
                  $('#about-content').html('<div class="container content-header"><h1>About <small>Behind the scenes</small></h1></div><div class="container"><div class="content-nav"><p>Not implemented yet! Coming soon!</p></div></div>');
              }

              $('#content').children().hide();
              $('#about-content').show();

          }
        
        });
        
        return AppRouter;
    });
}).call(this);