(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var TestDetailsView = Backbone.View.extend({
    
            test: {},

            messages: [],

            events: {
                "click a.run-test" : "runTest",
                "click a.cancel-test" : "cancelTest",
                "click a.xml-source" : "getXmlSource",
                "click a.java-source" : "getJavaSource",
                "click button.close" : "hideResultsTab"
            },

            initialize: function() {
                CitrusWebSocket.on('change:onmessage', this.onSocketMessage, this);

                $.ajax({
                    url: "testcase/details/" + this.options.test.packageName + "/" + this.options.test.name,
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
                $(this.el).find('div.test-design').html(TemplateManager.template('TestDesignView', { test: this.test.detail }));
                return this;
            },
          
            runTest: function() {
                $('button.run-test').button('loading');
                $('div#test-result-' + this.test.detail.name).find('div.progress').find('.bar').width('0%');
                $('div#test-result-' + this.test.detail.name).find('div.progress').find('.bar').text('');
                $('div#test-result-' + this.test.detail.name).find('div.progress').addClass('progress-success');
                $('div#test-result-' + this.test.detail.name).find('div.progress').removeClass('progress-danger');

                // prepare and show test results tab
                this.messages = [];
                $(this.el).find('div.test-message-flow').html(TemplateManager.template('TestMessageFlow', { messages: this.messages }));
                $(this.el).find('ul.nav').find('li').last().show();
                $(this.el).find('ul.nav').find('li').last().find('a').tab('show');

                $.ajax({
                    url: "testcase/execute/" + this.test.detail.name,
                    type: 'GET',
                    dataType: "json"
                });

                return false;
            },

            cancelTest: function() {
                $.ajax({
                    url: "testcase/stop/" + this.test.detail.name,
                    type: 'GET',
                    dataType: "json",
                    success: function() {
                    }
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
                    var msg = jsMessage.msg;

                    if ("PROCESS_START" == jsMessage.event) {
                        $('div#test-result-' + processId).find('div.progress').find('.bar').width('1%');
                        $('a.run-test').hide();
                        $('a.cancel-test').show();
                    } else if ("PROCESS_FAILED" == jsMessage.event) {
                        $('button.run-test').button('reset');
                        $('a.run-test').show();
                        $('a.cancel-test').hide();
                        $('div#test-result-' + processId).find('div.progress').removeClass('progress-success');
                        $('div#test-result-' + processId).find('div.progress').addClass('progress-danger');
                    } else if ("TEST_START" == jsMessage.event) {
                        $('div#test-result-' + processId).find('div.progress').find('.bar').width('3%');
                    } else if ("TEST_ACTION_FINISH" == jsMessage.event) {
                        $('div#test-result-' + processId).find('div.progress').find('.bar').width(jsMessage.progress + '%');
                        $('div#test-result-' + processId).find('div.progress').find('.bar').text(jsMessage.msg);
                    } else if ("TEST_SUCCESS" == jsMessage.event) {
                        $('button.run-test').button('reset');
                        $('a.run-test').show();
                        $('a.cancel-test').hide();
                        $('div#test-result-' + processId).find('div.progress').find('.bar').width('100%');
                    } else if ("TEST_FAILED" == jsMessage.event) {
                        $('button.run-test').button('reset');
                        $('a.run-test').show();
                        $('a.cancel-test').hide();
                        $('div#test-result-' + processId).find('div.progress').find('.bar').width('100%');
                        $('div#test-result-' + processId).find('div.progress').removeClass('progress-success');
                        $('div#test-result-' + processId).find('div.progress').addClass('progress-danger');
                    } else if ("INBOUND_MESSAGE" == jsMessage.event || "OUTBOUND_MESSAGE" == jsMessage.event) {
                        this.messages.push({id: _.uniqueId("message_"),
                                            type: jsMessage.event,
                                            data: jsMessage.msg,
                                            timestamp: moment()});
                        $(this.el).find('div.test-message-flow').html(TemplateManager.template('TestMessageFlow', { messages: this.messages }));
                    } else {
                        return;
                    }
                }
            },

            getXmlSource: function() {
                $.ajax({
                    url: "testcase/source/" + this.test.packageName + "/" + this.test.detail.name + "/xml",
                    type: 'GET',
                    dataType: "html",
                    success: _.bind(function(fileContent) {
                        $('div#xml-source-' + this.test.detail.name).find('pre').text(fileContent);
                        $('div#xml-source-' + this.test.detail.name).find('pre').addClass("prettyprint");
                        prettyPrint();
                        $('div#xml-source-' + this.test.detail.name).find('pre').removeClass("prettyprint");
                        $('div#xml-source-' + this.test.detail.name).find('pre').addClass("prettycode");
                    }, this)
                });
            },

            getJavaSource: function() {
                $.ajax({
                    url: "testcase/source/" + this.test.packageName + "/" + this.test.detail.name + "/java",
                    type: 'GET',
                    dataType: "html",
                    success: _.bind(function(fileContent) {
                        $('div#java-source-' + this.test.detail.name).find('pre').text(fileContent);
                        $('div#java-source-' + this.test.detail.name).find('pre').addClass("prettyprint");
                        prettyPrint();
                        $('div#java-source-' + this.test.detail.name).find('pre').removeClass("prettyprint");
                        $('div#java-source-' + this.test.detail.name).find('pre').addClass("prettycode");
                    }, this)
                });
            },

            hideResultsTab: function() {
                $(this.el).find('ul.nav-tabs').find('li').last().css('display', "none");
                $(this.el).find('ul.nav-tabs').find('li').first().find('a').tab('show');
            }
    
        });
        
        return TestDetailsView;
    });
}).call(this);