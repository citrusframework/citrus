(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var TestDetailsView = Backbone.View.extend({
    
            test: {},
          
            events: {
                "click a.run-test" : "runTest",
                "click a.xml-source" : "getXmlSource",
                "click a.java-source" : "getJavaSource"
            },

            initialize: function() {
                this.test = this.options.test;

                $.ajax({
                    url: "testcase/details/" + this.test.packageName + "/" + this.test.name,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function(test) {
                       this.test = test;
                    }, this),
                    async: false
                });
            },

            render: function() {
                $(this.el).html(TemplateManager.template('TestDetailsView', this.test));

                return this;
            },
          
            runTest: function() {
                $.ajax({
                    url: "testcase/execute/" + this.test.name,
                    type: 'GET',
                    dataType: "json",
                    success: function(testResult) {
                        if (testResult.success) {
                            console.log(testResult.testCase.name  + ' was executed successfully!</div>');
                        } else {
                            console.log(testResult.testCase.name  + ' failed! ' + testResult.failureStack + ' ' + testResult.stackTrace);
                        }
                    }
                });
            },

            getXmlSource: function() {
                $.ajax({
                    url: "testcase/source/" + this.test.packageName + "/" + this.test.name + "/xml",
                    type: 'GET',
                    dataType: "html",
                    success: _.bind(function(fileContent) {
                        $('div#xml-source-' + this.test.name).find('pre').text(fileContent);
                        $('div#xml-source-' + this.test.name).find('pre').addClass("prettyprint");
                        prettyPrint();
                        $('div#xml-source-' + this.test.name).find('pre').removeClass("prettyprint");
                    }, this)
                });
            },

            getJavaSource: function() {
                $.ajax({
                    url: "testcase/source/" + this.test.packageName + "/" + this.test.name + "/java",
                    type: 'GET',
                    dataType: "html",
                    success: _.bind(function(fileContent) {
                        $('div#java-source-' + this.test.name).find('pre').text(fileContent);
                        $('div#java-source-' + this.test.name).find('pre').addClass("prettyprint");
                        prettyPrint();
                        $('div#java-source-' + this.test.name).find('pre').removeClass("prettyprint");
                    }, this)
                });
            }
    
        });
        
        return TestDetailsView;
    });
}).call(this);