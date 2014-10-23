(function () {
    define(["TemplateManager", "views/SchemaListView", "views/EndpointListView"], function (TemplateManager, SchemaListView, EndpointListView) {
        var ConfigView = Backbone.View.extend({
            tabs:[
                {
                    active: true,
                    idSuffix: "endpoints",
                    displayName: "Endpoints",
                    view: EndpointListView
                },
                {
                    idSuffix: "schemas",
                    displayName: "Schema Definitions",
                    view: SchemaListView
                },
                {
                    idSuffix: "functions",
                    displayName: "Functions"
                },
                {
                    idSuffix: "validation-matcher",
                    displayName: "Validation Matcher"
                },
                {
                    idSuffix: "data-dictionaries",
                    displayName: "Data Dictionaries"
                },
                {
                    idSuffix: "namespace-context",
                    displayName: "Namespace Context"
                },
                {
                    idSuffix: "message-validators",
                    displayName: "Message Validators"
                },
                {
                    idSuffix: "global-variables",
                    displayName: "Global Variables"
                }
            ],

            events: {
            },

            render: function () {
                $(this.el).html(TemplateManager.template('ConfigView', {}));
                return this;
            },

            afterRender: function () {
                this.createConfigTabs();
            },

            createConfigTabs: function () {
                _.each(this.tabs, _.bind(this.createConfigTab, this));
            },

            createConfigTab: function (tab) {
                // generate tab-content-containers
                $('#config-tab-content').append(Handlebars.compile($('#config-tab-content-template').html())({id: tab.idSuffix}));

                // generate tab-header-container
                $('ul#config-tabs').append(Handlebars.compile($('#config-tab-template').html())({id: tab.idSuffix, tabDisplayName: tab.displayName, active: tab.active}));

                if (tab.view) {
                    var view = new tab.view({el: $('#config-' + tab.idSuffix)});
                    view.render();
                    view.afterRender();
                }

                if (tab.active) {
                    this.show(tab.idSuffix);
                }
            },

            show: function(tabId) {
                $('#config-tabs a[href="#config-tab-' + tabId + '"]').tab('show');
            }

        });

        return ConfigView;
    });
}).call(this);
