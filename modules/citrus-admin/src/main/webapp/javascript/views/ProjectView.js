(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var ProjectView = Backbone.View.extend({

            status: false,
            project: {},

            initialize: function() {
                $.ajax({
                    url: "project/active",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function(response) {
                        this.project = response;
                    }, this),
                    async: false
                });
            },

            render: function() {
                $(this.el).html(TemplateManager.template('ProjectView', { project: this.project, latestTests: this.getLatestTests(), testReport: this.getTestReport() }));
                return this;
            },

            getLatestTests: function() {
                var latestTests = {};
                $.ajax({
                    url: "testcase/",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function(response) {
                        latestTests = response;
                    }, this),
                    async: false
                });

                latestTests = _.sortBy(latestTests, function(test) {
                    return test.lastModified;
                });

                return _.last(latestTests, 8).reverse();
            },

            getTestReport: function() {
                var testReport = {};
                $.ajax({
                    url: "project/testreport",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function(response) {
                        testReport = response;
                    }, this),
                    async: false
                });

                return testReport;
            }

        });

        return ProjectView;
    });
}).call(this);