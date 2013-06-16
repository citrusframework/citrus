(function() {
    define(["TemplateManager", "views/TestDetailsView"], function(TemplateManager, TestDetailsView) {
        var TestListView = Backbone.View.extend({
    
          tests: {},

          events: {
              "submit #search-form" : "searchTests"
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
                      this.afterRender();
                  }, this)
              });
              
              return this;
          },
          
          afterRender: function() {
              $('#test-file-tree').fileTree({
                  root: '/',
                  script: 'testcase',
                  multiFolder: true,
                  expandSpeed: 1,
                  collapseSpeed: 1
              }, _.bind(function(file) {
                  this.showDetails(file);
              }, this));
              
              var searchKeys = _.map(this.tests, function(test){ return test.name; });
              $('#test-name').typeahead({
                  source: _.uniq(searchKeys),
                  items: 5,
                  minLength: 1,
                  updater: _.bind(function(item) {
                      $('#test-name').val(item);
                      this.searchTests();
                      return item;
                  }, this)
              });
          },
          
          searchTests: function() {
              var searchKey = $('#test-name').val();
              var test = _.find(this.tests, function(t) {return t.name == searchKey});

              var directory = test.packageName.replace(/\./g, "/");
              $('li.directory.collapsed').children('a[rel*="' + directory + '"]').click();

              this.showDetails(test.file);

              // prevent default form submission
              return false;
          },
          
          showDetails: function(file) {
              var test = _.find(this.tests, function(t) {return t.file == file});
              var idHash= test.name.toLowerCase();
              
              if ($('ul#test-tabs li#tab-' + idHash).size() === 0) {
                  
                  $('ul#test-tabs').append(Handlebars.compile($('#test-details-tab').html())({hash: idHash, name: test.name}));
                  $('div#test-tab-content').append(Handlebars.compile($('#test-details-tab-pane').html())({hash: idHash}));
                
                  // bind close function on newly created tab
                  $('#tab-close-' + idHash).click(function() {
                      if ($(this).parent('li').hasClass('active')) {
                          // removed tab was active so display first tab (search tab)
                          $(this).parent('li').prev().find('a').tab('show');
                      }
                    
                      // remove tab item
                      $(this).parent('li').remove();
                  });
                
                  $('#test-case-details-' + idHash).html(new TestDetailsView({ test: test }).render().el);
              }
              
              // show test details tab
              $('#test-tabs a[href="#test-details-tab-' + idHash + '"]').tab('show');

              return false;
          } 
    
        });
        
        return TestListView;
    });
}).call(this);