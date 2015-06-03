(function() {
    define(["TemplateManager", "views/TestDetailsView"], function(TemplateManager, TestDetailsView) {
        var TestListView = Backbone.View.extend({
    
          tests: {},

          events: {
              "submit #search-test-form" : "searchTest"
          },
          
          initialize: function() {
            $.ajax({
                  url: "testcase",
                  type: 'GET',
                  dataType: "json",
                  success: _.bind(function(response) {
                      this.tests = response;
                  }, this),
                  async: false
              });
          },
          
          render: function() {
              var testItems = _.map(this.tests, function(item) {
                  return _.pick(item, 'name', 'packageName');
              });

              var grouped = _.groupBy(testItems, 'packageName');
              var packages = _.map(_.uniq(_.pluck(testItems, 'packageName')), function(item) {
                  return { packageName: item, tests: _.property(item)(grouped) };
              });

              $(this.el).html(TemplateManager.template('TestListView', { testPackages: packages }));

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
              CitrusAdmin.navigate("tests/" + test.name, true);

              // prevent default form submission
              return false;
          },
          
          showDetails: function(testName) {
              var test = _.find(this.tests, function(t) {return t.name == testName});

              if (!test.id) {
                _.extend(test, {id: _.uniqueId()});
              }

              if ($('ul#test-tabs li#tab-' + test.id).size() === 0) {
                  $('ul#test-tabs').append(Handlebars.compile($('#test-details-tab').html())({id: test.id, name: test.name}));
                  $('div#test-tab-content').append(Handlebars.compile($('#test-details-tab-pane').html())({id: test.id}));

                  $('li#tab-' + test.id).find('a:first').on('shown.bs.tab', function (e) {
                    CitrusAdmin.navigate('tests/' + $(e.target).text(), false);
                  });
                
                  // bind close function on newly created tab
                  $('#tab-close-' + test.id).click(function() {
                      var isActiveTab = $(this).parent('li').hasClass('active')

                      // remove tab item
                      $(this).parent('li').remove();

                      if (isActiveTab) {
                          // removed tab was active so display next tab
                          $('ul#test-tabs').children('li:last').find('a:first').tab('show');
                      }

                      if ($('ul#test-tabs').children('li').size() == 1) {
                          // last tab was closed so navigate to testcase base page
                          CitrusAdmin.navigate('tests', false);
                      }
                  });
                
                  $('#test-case-details-' + test.id).html(new TestDetailsView({ test: test }).render().el);
              }
              
              // show test details tab
              $('#test-tabs a[href="#test-details-tab-' + test.id + '"]').tab('show');

              return false;
          },

          openDirectory: function(path) {
              $('li.directory.collapsed').children('a[rel$="' + path + '"]').trigger('click');
          }
    
        });
        
        return TestListView;
    });
}).call(this);