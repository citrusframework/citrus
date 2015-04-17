(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var EndpointListView = Backbone.View.extend({
            endpoints: undefined,

            events: {
                "click div.endpoint": "showEditForm",
                "click .btn-new": "showNewForm",
                "click .btn-remove": "remove",
                "click #btn-add": "create",
                "click #btn-save": "save",
                "click #btn-cancel": "closeForm"
            },

            initialize: function () {
                this.getEndpoints();
            },

            render: function () {
                var groupedEndpoints = _.groupBy(this.endpoints, function(endpoint) { return endpoint.type; });
                groupedEndpoints = _.map(groupedEndpoints, function(value, key) { return { type: key, endpoints: value }; });

                $(this.el).html(TemplateManager.template('EndpointListView', {endpointCount: this.endpoints.length, endpointTypes: groupedEndpoints}));
                return this;
            },

            afterRender: function () {
            },

            getEndpoints: function () {
                $.ajax({
                    url: "endpoint",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.endpoints = response;
                    }, this),
                    async: false
                });
            },

            remove: function (event) {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "endpoint/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.getEndpoints();
                        this.render();
                    }, this),
                    async: true
                });

                return false;
            },

            create: function() {
                var form = $('#endpoint-edit-form');
                var serializedForm = form.serializeObject();
                var url = "endpoint";

                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(serializedForm),
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeForm(undefined, _.bind(function() {
                    this.getEndpoints();
                    this.render();
                }, this));

                return false;
            },

            save: function() {
                var form = $('#endpoint-edit-form');

                var serializedForm = form.serializeObject();
                var elementId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "endpoint/" + elementId;
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(serializedForm),
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeForm(undefined, _.bind(function() {
                    this.getEndpoints();
                    this.render();
                }, this));

                return false;
            },

            closeForm: function(event, callback) {
                $('#endpoint-edit').hide('slide', function() {
                    $('#endpoint-list').show('slide', function() {
                        if (callback) {
                            callback();
                        }
                    });
                });
            },

            showNewForm: function(event) {
                var endpointType;

                if (event.currentTarget) {
                    endpointType = event.currentTarget.name;
                } else {
                    endpointType = event;
                }

                $.ajax({
                    url: "endpoint/type/" + endpointType,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#endpoint-edit').html(TemplateManager.template('EndpointEditView', response));

                        $('button.btn-option-search').click(_.bind(function(event) {
                            this.searchOptions(event);
                        }, this));

                        $('#endpoint-edit').find('option').each(function() {
                            if ($(this).attr('value') == $(this).parent().attr('value')) {
                                $(this).attr('selected', 'selected');
                            }
                        });

                        $('#endpointType').change(_.bind(function(event) {
                            this.showNewForm(event.currentTarget.value);
                        }, this));

                        $('#endpoint-list').hide('slide', function() {
                            $('#endpoint-edit').show('slide');
                        });
                    }, this),
                    async: true
                });
            },

            showEditForm: function() {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "endpoint/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#endpoint-edit').html(TemplateManager.template('EndpointEditView', response));

                        $('button.btn-option-search').click(_.bind(function(event) {
                            this.searchOptions(event);
                        }, this));

                        $('#endpoint-edit').find('option').each(function() {
                            if ($(this).attr('value') == $(this).parent().attr('value')) {
                                $(this).attr('selected', 'selected');
                            }
                        });

                        $('#endpoint-list').hide('slide', function() {
                            $('#endpoint-edit').show('slide');
                        });
                    }, this),
                    async: true
                });
            },

            extractId: function(encodedId) {
                var splitString = encodedId.split('-');
                return splitString[splitString.length-1];
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

        return EndpointListView;
    });
}).call(this);
