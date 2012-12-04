(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var TestDetailsView = Backbone.View.extend({
    
          test: {},
          
          initialize: function() {
            this.test = this.options.test;  
          },
          
          events: {
              "click a.run-test" : "runTest",
              "click a.xml-source" : "getXmlSource",
              "click a.java-source" : "getJavaSource"
          },
          
          render: function() {
              $(this.el).html(TemplateManager.template('TestDetailsView', this.test));

              return this;
          },
          
          runTest: function() {
              $('div.alert').hide('fast');
              $('div.alert').remove();
              $('div#' + this.test.name).append('<div class="alert"><strong>Running test!</strong> Run Citrus test case ... <img src="images/ajax-loader.gif" alt="ajax-loader.gif" class="ajax-loader"/></div>');
              
              CitrusAdminLogging.bindOutput('div#' + this.test.name);
              
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
          
          getXmlSource: function() {
              $('pre.xml-code').remove();
              $('div#' + this.test.name + ' > div.tab-content > div.tab-pane > div.xml-source').append('<pre class="prettyprint linenums xml-code">Loading sources ...</pre>');
              
              $.ajax({
                  url: "testcase/" + this.test.packageName + "/" + this.test.name + "/xml",
                  type: 'GET',
                  dataType: "html",
                  success: function(fileContent) {
                      $('pre.xml-code').text(fileContent);
                      $('pre.xml-code').prepend('<a class="close close-code">×</a>');
                      
                      $('a.close-code').click(function() {
                          $('pre.xml-code').remove();
                      });
                      
                      prettyPrint();
                  }
              });
          },
          
          getJavaSource: function() {
              $('pre.java-code').remove();
              $('div#' + this.test.name + ' > div.tab-content > div.tab-pane > div.java-source').append('<pre class="prettyprint linenums java-code">Loading sources ...</pre>');
              
              $.ajax({
                  url: "testcase/" + this.test.packageName + "/" + this.test.name + "/java",
                  type: 'GET',
                  dataType: "html",
                  success: function(fileContent) {
                      $('pre.java-code').text(fileContent);
                      $('pre.java-code').prepend('<a class="close close-code">×</a>');
                      
                      $('a.close-code').click(function() {
                          $('pre.java-code').remove();
                      });
                      
                      prettyPrint();
                  }
              });
          }
    
        });
        
        return TestDetailsView;
    });
}).call(this);