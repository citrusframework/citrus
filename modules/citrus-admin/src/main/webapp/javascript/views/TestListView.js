(function() {
    define(["TemplateManager", "views/TestDetailsView"], function(TemplateManager, TestDetailsView) {
        var TestListView = Backbone.View.extend({
    
          tests: {},

          events: {
              "submit #search-form" : "searchTest"
          },
          
          initialize: function() {
          },
          
          render: function() {
              $(this.el).html(TemplateManager.template('TestListView', {}));

              $.ajax({
                  url: "testcase",
                  type: 'GET',
                  dataType: "json",
                  success: _.bind(function(response) {
                      this.tests = response;
                  }, this),
                  async: false
              });

              $('#test-file-tree').fileTree({
                  root: '/',
                  script: 'testcase',
                  multiFolder: true,
                  expandSpeed: 1,
                  collapseSpeed: 1
              }, _.bind(function(file) {
                  CitrusAdmin.navigate("testcases/" + _.find(this.tests, function(t) {return t.file == file}).name, true);
              }, this));

              var searchKeys = _.map(this.tests, function(test){ return test.name; });
              $('#test-name').typeahead({
                  source: _.uniq(searchKeys),
                  items: 15,
                  minLength: 1,
                  updater: _.bind(function(item) {
                      $('#test-name').val(item);
                      this.searchTest();
                      return item;
                  }, this)
              });

              return this;
          },
          
          searchTest: function() {
              var searchKey = $('#test-name').val();
              var test = _.find(this.tests, function(t) {return t.name == searchKey});

              var pathTokens = test.packageName.split(".");
              var path = "";
              _.each(pathTokens, _.bind(function (token) {
                  path += token + "/";
                  this.openDirectory(path);
              }, this));

              $('#test-name').val('');
              CitrusAdmin.navigate("testcases/" + test.name, true);

              // prevent default form submission
              return false;
          },
          
          showDetails: function(testName) {
              var test = _.find(this.tests, function(t) {return t.name == testName});
              var idHash= test.name.toLowerCase();
              
              if ($('ul#test-tabs li#tab-' + idHash).size() === 0) {
                  
                  $('ul#test-tabs').append(Handlebars.compile($('#test-details-tab').html())({hash: idHash, name: test.name}));
                  $('div#test-tab-content').append(Handlebars.compile($('#test-details-tab-pane').html())({hash: idHash}));
                
                  // bind close function on newly created tab
                  $('#tab-close-' + idHash).click(function() {
                      var isActiveTab = $(this).parent('li').hasClass('active')

                      // remove tab item
                      $(this).parent('li').remove();

                      if (isActiveTab) {
                          // removed tab was active so display next tab
                          $('ul#test-tabs').find('li:last').find('a').tab('show');
                      }

                      if ($('ul#test-tabs').find('li').size() == 1) {
                          // last tab was closed so navigate to testcase base page
                          CitrusAdmin.navigate('testcases', false);
                      } else {
                          // navigate to new active tab
                          CitrusAdmin.navigate('testcases/' + $('ul#test-tabs').find('li.active').find('a').text(), false);
                      }
                  });
                
                  $('#test-case-details-' + idHash).html(new TestDetailsView({ test: test }).render().el);
              }
              
              // show test details tab
              $('#test-tabs a[href="#test-details-tab-' + idHash + '"]').tab('show');

              return false;
          },

          openDirectory: function(path) {
              $('li.directory.collapsed').children('a[rel$="' + path + '"]').trigger('click');
          }
    
        });
        
        return TestListView;
    });
}).call(this);