(function() {
    define(["jquery", "backbone", "TemplateManager"], function($, Backbone, TemplateManager) {
        var TestCasesView = Backbone.View.extend({
    
          render: function() {
              $(this.el).html(TemplateManager.template('TestCasesView',{}));
              
              $.ajax({
                  url: "testcase",
                  type: 'GET',
                  dataType: "json",
                  success: function(response) {
                      $('.test-case').remove();
                      $('.test-package').remove();
                      
                      var packageName = '';
                      $.each(response.testCaseInfos, function(index, test) {
                          if (test.packageName != packageName) {
                              if (packageName) {
                                  $('#test-cases').prepend('<tr class="test-package"><td colspan="2"><b>' + packageName + '</b></td></tr>');
                              }
                              
                              packageName = test.packageName;
                          }
                          
                          $('#test-cases').prepend('<tr class="test-case"><td style="padding-left: 35px;"><p id="' + test.name + '"><i id="' + test.name + '" test-package="' + test.packageName + '" class="icon-list-alt"></i>&nbsp;&nbsp;<strong>' + test.name + '</strong></p></td><td><a id="' + test.name + '" class="btn btn-success run-test"><i class="icon-play icon-white"></i></a></td></tr>');
                          
                          $('a#' + test.name).click(function() {
                              $('div.alert').hide('fast');
                              $('div.alert').remove();
                              var id = $(this).attr('id');
                              $('p#' + id).append('<div class="alert"><strong>Running test!</strong> Run Citrus test case ... <img src="images/ajax-loader.gif" alt="ajax-loader.gif" class="ajax-loader"/></div>');
                              
                              logOutput = $('p#' + id);
                              
                              $.ajax({
                                  url: "testcase/execute/" + $(this).attr('id'),
                                  type: 'GET',
                                  dataType: "json",
                                  success: function(testResult) {
                                      $('div.alert').hide('fast');
                                      $('div.alert').remove();
                                      
                                      var id = testResult.testCase.name;
                                      
                                      if (testResult.success) {
                                          $('p#' + id).append('<div class="alert alert-success" style="display:none;"><a class="close" data-dismiss="alert">×</a><strong>SUCCESS!</strong> ' + id + ' was executed successfully!</div>');
                                      } else {
                                          $('p#' + id).append('<div class="alert alert-error" style="display:none;"><a class="close" data-dismiss="alert">×</a><strong>FAILED!</strong> ' + id + ' failed!<p>' + testResult.failureStack + '</p><p>' + testResult.stackTrace + '</p></div>');
                                      }
                                      
                                      $('div.alert').alert(); // enable alert dismissal
                                      $('div.alert').show('fast');
                                  }
                              });
                          });
                          
                          $('i#' + test.name).click(function() {
                              $('pre.test-case-code').remove();
                              $(this).parent().append('<pre class="prettyprint linenums test-case-code">Loading test ...</pre>');
                              
                              $.ajax({
                                  url: "testcase/" + $(this).attr('test-package') + "/" + $(this).attr('id'),
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
                          });
                      });
                      
                      if (packageName) {
                          $('#test-cases').prepend('<tr class="test-package"><td colspan="2"><b>' + packageName + '</b></td></tr>');
                      }
                      
                      $('#cnt-tests').text(response.testCaseInfos.length);
                  }
              });
              
              return this;
          }
    
        });
        
        return TestCasesView;
    });
}).call(this);