(function() {
    define(["jquery", "underscore", "backbone", "TemplateManager"], function($, _, Backbone, TemplateManager) {
        var TestItemView = Backbone.View.extend({
    
          test: {},
          
          initialize: function() {
            this.test = this.options.test;  
          },
          
          events: {
              "click a.run-test" : "runTest",
              "click a.source" : "getSource"
          },
          
          render: function() {
              $(this.el).html(TemplateManager.template('TestItemView', this.test));

              return this;
          },
          
          runTest: function() {
              $('div.alert').hide('fast');
              $('div.alert').remove();
              $('div#' + this.test.name).append('<div class="alert"><strong>Running test!</strong> Run Citrus test case ... <img src="images/ajax-loader.gif" alt="ajax-loader.gif" class="ajax-loader"/></div>');
              
              logOutput = $('div#' + this.test.name);
              
              $.ajax({
                  url: "testcase/execute/" + this.test.name,
                  type: 'GET',
                  dataType: "json",
                  success: function(testResult) {
                      $('div.alert').hide('fast');
                      $('div.alert').remove();
                      
                      var id = testResult.testCase.name;
                      
                      if (testResult.success) {
                          $('div#' + id).append('<div class="alert alert-success" style="display:none;"><a class="close" data-dismiss="alert">×</a><strong>SUCCESS!</strong> ' + id + ' was executed successfully!</div>');
                      } else {
                          $('div#' + id).append('<div class="alert alert-error" style="display:none;"><a class="close" data-dismiss="alert">×</a><strong>FAILED!</strong> ' + id + ' failed!<p>' + testResult.failureStack + '</p><p>' + testResult.stackTrace + '</p></div>');
                      }
                      
                      $('div.alert').alert(); // enable alert dismissal
                      $('div.alert').show('fast');
                  }
              });
          },
          
          getSource: function() {
              $('pre.test-case-code').remove();
              $(this.el).append('<pre class="prettyprint linenums test-case-code">Loading test ...</pre>');
              
              $.ajax({
                  url: "testcase/" + this.test.packageName + "/" + this.test.name,
                  type: 'GET',
                  dataType: "html",
                  success: function(fileContent) {
                      $('pre.test-case-code').text(fileContent);
                      $('pre.test-case-code').prepend('<a class="close close-code">×</a>');
                      
                      $('a.close-code').click(function() {
                          $('pre.test-case-code').remove();
                      });
                      
                      prettyPrint();
                  }
              });
          }
    
        });
        
        return TestItemView;
    });
}).call(this);