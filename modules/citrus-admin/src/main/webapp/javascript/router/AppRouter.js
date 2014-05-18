(function() {
    define(["views/HeaderView", "views/ProjectView", "views/ConfigView", "views/TestListView", "views/FooterView"], function(HeaderView, ProjectView, ConfigView, TestListView, FooterView) {
        var AppRouter = Backbone.Router.extend({

          headerView: undefined,
          footerView: undefined,
          projectView: undefined,
          configView: undefined,
          testListView: undefined,
          statsView: undefined,
          aboutView: undefined,

          routes: {
            "": "project", // #project
            "project": "project", // #project
            "config": "config", //#config
            "tests": "tests", // #tests
            "tests/:testName": "testDetails", // #tests/EchoActionITest
            "stats": "stats", // #stats
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
          
          config: function() {
              if (!this.configView) {
                  this.configView = new ConfigView({el: $('#config-content')});
                  this.configView.render();
                  this.configView.afterRender();
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