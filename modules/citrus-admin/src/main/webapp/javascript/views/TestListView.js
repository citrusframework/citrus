(function() {
    define(["TemplateManager", "views/TestDetailsView"], function(TemplateManager, TestDetailsView) {
        var TestListView = Backbone.View.extend({
    
          tests: {},
          
          events: {
              "click tr.show-test-details" : "showDetails"
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
              $(this.el).html(TemplateManager.template('TestListView', {}));
              return this;
          },
          
          afterRender: function() {
              $('#search-results').html(TemplateManager.template('TestTableView', { tests: this.tests }));
              
              var searchkeys = _.map(this.tests, function(test){ return test.packageName + "." + test.name; });
              $('#test-name').typeahead({
                  source: searchkeys,
                  items: 5,
                  minLength: 1
              });
          },
          
          showDetails: function(event) {
              var test = this.tests[event.currentTarget.rowIndex - 1];
              var idHash= test.name.toLowerCase();
              
              if ($('ul#testlist-tabs li#tab-' + idHash).size() === 0) {
                  
                  $('ul#testlist-tabs').append(Handlebars.compile($('#test-details-tab').html())({hash: idHash, name: test.name}));
                  $('div#testlist-tab-content').append(Handlebars.compile($('#test-details-tab-pane').html())({hash: idHash}));
                
                  // bind close function on newly created tab
                  $('#tab-close-' + idHash).click(function() {
                      if ($(this).parent('li').hasClass('active')) {
                          // removed tab was active so display first tab (search tab)
                          $('#testlist-tabs a:first').tab('show');
                      }
                    
                      // remove tab item
                      $(this).parent('li').remove();
                  });
                
                  $('#test-case-details-' + idHash).html(new TestDetailsView({ test: test }).render().el);
              }
              
              // show test details tab
              $('#testlist-tabs a[href="#test-details-tab-' + idHash + '"]').tab('show');
          } 
    
        });
        
        return TestListView;
    });
}).call(this);