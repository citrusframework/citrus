(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var NamespaceContextView = Backbone.View.extend({
            namespaceContext: {namespaces: {}},

            events: {
                "click .btn-remove": "remove",
                "click #btn-add": "add"
            },

            initialize: function () {
                this.getNamespaces();
            },

            render: function () {
                $(this.el).html(TemplateManager.template('NamespaceContextView', {namespaceCount: this.namespaceContext.namespaces.length, namespaces: this.namespaceContext.namespaces}));
                return this;
            },

            afterRender: function () {
            },

            getNamespaces: function () {
                $.ajax({
                    url: "namespace-context",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.namespaceContext = response;
                    }, this),
                    async: false
                });
            },

            remove: function (event) {
                var prefix = $(event.target).closest($("[id]")).attr('id');

                this.namespaceContext.namespaces = _.reject(this.namespaceContext.namespaces, function(namespace) {
                    return namespace.prefix === prefix;
                });

                $.ajax({
                    url: "namespace-context/",
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(this.namespaceContext),
                    success: _.bind(function (response) {
                    }, this),
                    async: true
                });

                this.render();
                return false;
            },

            add: function() {
                this.namespaceContext.namespaces.push({ prefix: $('input[name = "prefix"]').val(), uri: $('input[name = "uri"]').val() });

                $.ajax({
                    url: "namespace-context/",
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(this.namespaceContext),
                    success: _.bind(function (response) {
                    }, this),
                    async: true
                });

                this.render();
                return false;
            },

            searchOptions: function(event) {
                $.ajax({
                    url: "configuration/search",
                    type: 'POST',
                    dataType: "json",
                    contentType: "text/plain",
                    data: event.currentTarget.name,
                    success: _.bind(function (response) {
                        $('#dropdown-menu-' + event.currentTarget.id).children().remove();

                        if (response.length) {
                            _.each(response, function(item) {
                                $('#dropdown-menu-' + event.currentTarget.id)
                                    .append('<li><a name="' + item + '" class="clickable option-select"><i class="fa fa-cube"></i>&nbsp;' + item + '</a></li>');
                            });
                        } else {
                            $('#dropdown-menu-' + event.currentTarget.id).append('<li><a name="none">no suggestions</a></li>');
                        }

                        $('#dropdown-menu-' + event.currentTarget.id).find('a.option-select').click(function(e) {
                            $('input[name="' + event.currentTarget.id + '"]').val(e.currentTarget.name);
                        })
                    }, this),
                    async: true
                });

                // prevent default form submission
                return false;
            }

        });

        return NamespaceContextView;
    });
}).call(this);
