(function() {
    define(["TemplateManager", "views/TestDetailsView"], function(TemplateManager, TestDetailsView) {
        var TestListView = Backbone.View.extend({
    
          tests: {},
          
          events: {
              "click a.test-case" : "showTestCase"
          },
          
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
                  $('#test-list').prepend('<li class=""><a id="' + index + '" href="#testcases" class="test-case"><i class="icon-chevron-right"></i> ' + test.name + '</a></li>');
              });
              
              $('ul.side-nav li').each(function(index) {
                  $(this).click(function() {
                      $('ul.side-nav li').removeClass('active');
                      $(this).addClass('active');
                  });
              });
              
              return this;
          },
          
          showTestCase: function(event) {
              $('#test-case').html(new TestDetailsView({ test: this.tests[$(event.currentTarget).attr('id')] }).render().el);
          } 
    
        });
        
        return TestListView;
    });
}).call(this);