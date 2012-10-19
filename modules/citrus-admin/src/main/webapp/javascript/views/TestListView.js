(function() {
    define(["jquery", "underscore", "backbone", "TemplateManager", "views/TestItemView"], function($, _, Backbone, TemplateManager, TestItemView) {
        var TestListView = Backbone.View.extend({
    
          tests: {},
            
          initialize: function() {
              $.ajax({
                  url: "testcase",
                  type: 'GET',
                  dataType: "json",
                  success: _.bind(function(response) {
                               this.tests = response.testCaseInfos;
                           }, this),
                  async: false
              });
          },
          
          render: function() {
              $(this.el).html(TemplateManager.template('TestListView', { testsTotal: this.tests.length }));
              
              $.each(this.tests, function(index, test) {
                  $('#test-cases').prepend(new TestItemView({ test: test }).render().el);
              });
              
              return this;
          }
    
        });
        
        return TestListView;
    });
}).call(this);