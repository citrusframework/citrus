(function () {
    define(["TemplateManager", "views/SchemaListView", "views/EndpointListView", "views/NamespaceContextView", "views/GlobalVariablesView", "views/FunctionLibraryListView", "views/ValidationMatcherListView", "views/DataDictionaryListView"], function (TemplateManager, SchemaListView, EndpointListView, NamespaceContextView, GlobalVariablesView, FunctionLibraryListView, ValidationMatcherListView, DataDictionaryListView) {
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
                    idSuffix: "global-variables",
                    displayName: "Global Variables",
                    view: GlobalVariablesView
                },
                {
                    idSuffix: "functions",
                    displayName: "Functions",
                    view: FunctionLibraryListView
                },
                {
                    idSuffix: "validation-matcher",
                    displayName: "Validation Matcher",
                    view: ValidationMatcherListView
                },
                {
                    idSuffix: "data-dictionaries",
                    displayName: "Data Dictionaries",
                    view: DataDictionaryListView
                },
                {
                    idSuffix: "namespace-context",
                    displayName: "Namespace Context",
                    view: NamespaceContextView
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
