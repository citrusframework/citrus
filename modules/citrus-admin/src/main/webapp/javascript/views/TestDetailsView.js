(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var TestDetailsView = Backbone.View.extend({
    
            test: {},
            runConfigurations: {},
            activeConfiguration: {},
            messages: [],

            events: {
                "click a#btn-edit" : "editRunConfig",
                "change select#run-config" : "selectRunConfig",
                "click a#btn-run" : "runTest",
                "click a#btn-cancel" : "cancelTest",
                "click a#xml-source" : "getXmlSource",
                "click a#java-source" : "getJavaSource",
                "click button.close" : "hideResultsTab"
            },

            initialize: function(options) {
                CitrusWebSocket.on('change:onmessage', this.onSocketMessage, this);

                $.ajax({
                    url: "configuration/run",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function(runConfigurations) {
                        this.runConfigurations = runConfigurations;

                        this.activeConfiguration = _.find(runConfigurations, function(i) { return i.standard });
                    }, this),
                    async: false
                });

                $.ajax({
                    url: "testcase/details/"+ options.test.type + "/" + options.test.packageName + "/" + this.getTestNameUrl(options.test.name),
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function(test) {
                       this.test = test;
                    }, this),
                    async: false
                });

                if (this.test) {
                    _.extend(this.test, {id: options.test.id});
                }
            },

            render: function() {
                $(this.el).html(TemplateManager.template('TestDetailsView', { test: this.test, runConfigurations: this.runConfigurations, activeConfiguration: this.activeConfiguration }));
                $(this.el).find('div.test-design').html(TemplateManager.template('TestDesignView', { test: this.test }));
                return this;
            },

            selectRunConfig: function(event) {
                this.activeConfiguration = _.find(this.runConfigurations, function(i) { return i.id === event.currentTarget.id });
                return false;
            },
          
            runTest: function() {
                // prepare and show test results tab
                this.messages = [];
                $('div#test-result-' + this.test.id).html(TemplateManager.template('TestResultsView', { messages: this.messages }));

                $('div#test-result-' + this.test.id).find('div.progress').find('.bar').width('0%');
                $('div#test-result-' + this.test.id).find('div.progress').find('.bar').text('');
                $('div#test-result-' + this.test.id).find('div.progress').addClass('progress-success');
                $('div#test-result-' + this.test.id).find('div.progress').removeClass('progress-danger');

                var testNameUrl = this.getTestNameUrl(this.test.name);
                if (String(testNameUrl).indexOf('?') > 0) {
                    testNameUrl += "&runConfiguration=" + this.activeConfiguration.id;
                } else {
                    testNameUrl += "?runConfiguration=" + this.activeConfiguration.id;
                }

                $.ajax({
                    url: "testcase/execute/" + this.test.packageName + "/" + testNameUrl,
                    type: 'GET',
                    dataType: "json"
                });

                return false;
            },

            cancelTest: function() {
                $.ajax({
                    url: "testcase/stop/" + this.getTestNameUrl(this.test.name),
                    type: 'GET',
                    dataType: "json"
                });

                return false;
            },

            onSocketMessage: function (message) {
                if (message) {
                    jsMessage = $.parseJSON(message)

                    if ("PING" == jsMessage.event) {
                        return;
                    }

                    var processId = jsMessage.processId;
                    if (processId != this.test.name) {
                       return;
                    }

                    var msg = jsMessage.msg;
                    if ("PROCESS_START" == jsMessage.event) {
                        $('div#test-result-' + this.test.id).find('div.progress').find('.bar').width('1%');
                        $('a.run-test').hide();
                        $('a.cancel-test').show();
                    } else if ("PROCESS_FAILED" == jsMessage.event) {
                        $('button.run-test').button('reset');
                        $('a.run-test').show();
                        $('a.cancel-test').hide();
                        $('div#test-result-' + this.test.id).find('div.progress').removeClass('progress-success');
                        $('div#test-result-' + this.test.id).find('div.progress').addClass('progress-danger');
                    } else if ("TEST_START" == jsMessage.event) {
                        $('div#test-result-' + this.test.id).find('div.progress').find('.bar').width('3%');
                    } else if ("TEST_ACTION_FINISH" == jsMessage.event) {
                        $('div#test-result-' + this.test.id).find('div.progress').find('.bar').width(jsMessage.progress + '%');
                        $('div#test-result-' + this.test.id).find('div.progress').find('.bar').text(jsMessage.msg);
                    } else if ("TEST_SUCCESS" == jsMessage.event) {
                        $('button.run-test').button('reset');
                        $('a.run-test').show();
                        $('a.cancel-test').hide();
                        $('div#test-result-' + this.test.id).find('div.progress').find('.bar').width('100%');
                    } else if ("TEST_FAILED" == jsMessage.event) {
                        $('button.run-test').button('reset');
                        $('a.run-test').show();
                        $('a.cancel-test').hide();
                        $('div#test-result-' + this.test.id).find('div.progress').find('.bar').width('100%');
                        $('div#test-result-' + this.test.id).find('div.progress').removeClass('progress-success');
                        $('div#test-result-' + this.test.id).find('div.progress').addClass('progress-danger');
                    } else if ("INBOUND_MESSAGE" == jsMessage.event || "OUTBOUND_MESSAGE" == jsMessage.event) {
                        this.messages.push({id: _.uniqueId("message_"),
                                            type: jsMessage.event,
                                            data: jsMessage.msg,
                                            timestamp: moment()});
                        $('div#test-result-' + this.test.id).find('div.test-message-flow').html(TemplateManager.template('TestMessageFlow', { messages: this.messages }));
                    } else {
                        return;
                    }
                }
            },

            getXmlSource: function() {
                $.ajax({
                    url: "testcase/source/xml/" + this.test.packageName + "/" + this.getTestNameUrl(this.test.name),
                    type: 'GET',
                    dataType: "html",
                    success: _.bind(function(fileContent) {
                        if (fileContent) {
                            $('div#xml-source-' + this.test.id).find('pre').text(fileContent);
                        } else {
                            $('div#xml-source-' + this.test.id).find('pre').text("<xml>No XML sources available!</xml>");
                        }
                        $('div#xml-source-' + this.test.id).find('pre').addClass("prettyprint");
                        prettyPrint();
                        $('div#xml-source-' + this.test.id).find('pre').removeClass("prettyprint");
                        $('div#xml-source-' + this.test.id).find('pre').addClass("prettycode");
                    }, this)
                });
            },

            getJavaSource: function() {
                $.ajax({
                    url: "testcase/source/java/" + this.test.packageName + "/" + this.getTestNameUrl(this.test.name),
                    type: 'GET',
                    dataType: "html",
                    success: _.bind(function(fileContent) {
                        $('div#java-source-' + this.test.id).find('pre').text(fileContent);
                        $('div#java-source-' + this.test.id).find('pre').addClass("prettyprint");
                        prettyPrint();
                        $('div#java-source-' + this.test.id).find('pre').removeClass("prettyprint");
                        $('div#java-source-' + this.test.id).find('pre').addClass("prettycode");
                    }, this)
                });
            },

            getTestNameUrl: function(testName) {
                return String(testName).replace(".", "?method=");
            },

            hideResultsTab: function() {
                $(this.el).find('ul.nav-tabs').find('li').last().css('display', "none");
                $(this.el).find('ul.nav-tabs').find('li').first().find('a').tab('show');
            }
    
        });
        
        return TestDetailsView;
    });
}).call(this);