(function() {
    define(["TemplateManager", "views/TestItemView"], function(TemplateManager, TestItemView) {
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
                  $('#test-list').prepend('<li class=""><a href="#' + test.name + '"><i class="icon-chevron-right"></i> ' + test.name + '</a></li>');
                  
                  $('#test-cases').prepend(new TestItemView({ test: test }).render().el);
              });
              
              $('ul.side-nav li').each(function(index) {
                  $(this).click(function() {
                      $('ul.side-nav li').removeClass('active');
                      $(this).addClass('active');
                  });
              });
              
              return this;
          }
    
        });
        
        return TestListView;
    });
}).call(this);