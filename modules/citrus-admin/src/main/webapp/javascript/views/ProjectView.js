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
                $(this.el).html(TemplateManager.template('ProjectView', this.project));
                return this;
            }

        });

        return ProjectView;
    });
}).call(this);