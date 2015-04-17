(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var ValidationMatcherListView = Backbone.View.extend({
            defaultLibrary: {},
            libraries: [],

            events: {
                "click div.matcher": "showEditForm",
                "click .btn-new": "showNewForm",
                "click .btn-remove": "remove",
                "click #btn-add": "create",
                "click #btn-save": "save",
                "click #btn-cancel": "closeForm"
            },

            initialize: function () {
                this.getValidationMatcher();

                $.ajax({
                    url: "validation-matcher/default",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.defaultLibrary = response;
                    }, this),
                    async: false
                });
            },

            render: function () {
                $(this.el).html(TemplateManager.template('ValidationMatcherListView', {matcherCount: this.libraries.length, libraries: this.libraries, defaultLibrary: this.defaultLibrary}));
                return this;
            },

            afterRender: function () {
            },

            getValidationMatcher: function () {
                $.ajax({
                    url: "validation-matcher",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.libraries = response;
                    }, this),
                    async: false
                });
            },

            removeMatcher: function (event) {
                $('div#matcher').find('#' + $(event.target).parent().attr('data-target')).remove();
                return false;
            },

            addMatcher: function (event) {
                var matcherName = $('#matcher-edit').find('input[name = "name"]').val();
                var matcherClass = $('#matcher-edit').find('input[name = "clazz"]').val();

                $('#matcher-edit').find('div#matcher').append('<div id="' + matcherName + '"' + ' title="' + matcherClass + '" class="list-group-item clickable"><i class="fa fa-file-text-o"></i>&nbsp;<b>' + matcherName + '()</b> class=' + matcherClass + '&nbsp;<a class="btn-remove-matcher pull-right" href="#config" title="Remove matcher" data-target="' + matcherName + '"><i class="fa fa-times" style="color: #A50000;"></i></a></div>');

                $('#matcher-edit').find('div#matcher').find('div:last').find('a.btn-remove-matcher').click(_.bind(function(event) {
                    this.removeMatcher(event);
                }, this));

                $('input[name = "name"]').val('');
                $('input[name = "clazz"]').val('');
                return false;
            },

            remove: function (event) {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "validation-matcher/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.getValidationMatcher();
                        this.render();
                    }, this),
                    async: true
                });

                return false;
            },

            create: function() {
                var form = $('#matcher-edit-form');
                var serializedForm = form.serializeObject();

                $.ajax({
                    url: "validation-matcher",
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getValidationMatcherJSON(serializedForm),
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeForm(undefined, _.bind(function() {
                    this.getValidationMatcher();
                    this.render();
                }, this));

                return false;
            },

            save: function() {
                var form = $('#matcher-edit-form');

                var serializedForm = form.serializeObject();
                var elementId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "validation-matcher/" + elementId;
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getValidationMatcherJSON(serializedForm),
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeForm(undefined, _.bind(function() {
                    this.getValidationMatcher();
                    this.render();
                }, this));

                return false;
            },

            closeForm: function(event, callback) {
                $('#matcher-edit').hide('slide', function() {
                    $('#matcher-list').show('slide', function() {
                        if (callback) {
                            callback();
                        }
                    });
                });
            },

            showNewForm: function(event) {
                $('#matcher-edit').html(TemplateManager.template('ValidationMatcherEditView', {}));

                $('button.btn-option-search').click(_.bind(function(event) {
                    this.searchOptions(event);
                }, this));

                $('#matcher-edit').find('#btn-add-matcher').click(_.bind(function(event) {
                    this.addMatcher(event);
                }, this));

                $('#matcher-list').hide('slide', function() {
                    $('#matcher-edit').show('slide');
                });
            },

            showEditForm: function() {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "validation-matcher/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#matcher-edit').html(TemplateManager.template('ValidationMatcherEditView', response));

                        $('button.btn-option-search').click(_.bind(function(event) {
                            this.searchOptions(event);
                        }, this));

                        $('#matcher-edit').find('.btn-remove-matcher').click(_.bind(function(event) {
                            this.removeMatcher(event);
                        }, this));

                        $('#matcher-edit').find('#btn-add-matcher').click(_.bind(function(event) {
                            this.addMatcher(event);
                        }, this));

                        $('#matcher-edit').find('option').each(function() {
                            if ($(this).attr('value') == $(this).parent().attr('value')) {
                                $(this).attr('selected', 'selected');
                            }
                        });

                        $('#matcher-list').hide('slide', function() {
                            $('#matcher-edit').show('slide');
                        });
                    }, this),
                    async: true
                });
            },

            getValidationMatcherJSON: function(serializedForm) {
                var matchers = [];

                $('#matcher-edit').find('div#matcher').children().each(function(index) {
                    matchers.push( {name: $(this).attr('id'), clazz: $(this).attr('title')} );
                });

                var validationMatcherLibrary = { id: serializedForm.id, prefix: serializedForm.prefix, matchers: matchers };

                return JSON.stringify(validationMatcherLibrary);
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

        return ValidationMatcherListView;
    });
}).call(this);
