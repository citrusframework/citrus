(function() {
    define(["views/HeaderView", "views/WelcomeView", "views/ConfigView", "views/TestListView", "views/FooterView"], function(HeaderView, WelcomeView, ConfigView, TestListView, FooterView) {
        var AppRouter = Backbone.Router.extend({
        
          routes: {
            "": "welcome", // #welcome
            "welcome": "welcome", // #welcome
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
              footerView.minimize();

              $.fn.serializeObject = function () {
                  var o = {};
                  var a = this.serializeArray();
                  $.each(a, function () {
                      if (o[this.name] !== undefined) {
                          if (!o[this.name].push) {
                              o[this.name] = [o[this.name]];
                          }
                          o[this.name].push(this.value || null);
                      }
                      else {
                          // treat empty string as undefined
                          if (this.value == '') {
                              o[this.name] = undefined;
                          }
                          else {
                              o[this.name] = this.value;
                          }
                      }
                  });
                  return o;
              };

          },
          
          welcome: function() {
              var welcomeView = new WelcomeView({el: $('#content')});
              welcomeView.render();
          },
          
          config: function() {
              var configView = new ConfigView({el: $('#content')});
              configView.render();
              configView.afterRender();
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