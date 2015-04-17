(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var FunctionLibraryListView = Backbone.View.extend({
            defaultLibrary: {},
            libraries: [],

            events: {
                "click div.library": "showEditForm",
                "click .btn-new": "showNewForm",
                "click .btn-remove": "remove",
                "click #btn-add": "create",
                "click #btn-save": "save",
                "click #btn-cancel": "closeForm"
            },

            initialize: function () {
                this.getLibraries();

                $.ajax({
                    url: "function-library/default",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.defaultLibrary = response;
                    }, this),
                    async: false
                });
            },

            render: function () {
                $(this.el).html(TemplateManager.template('FunctionLibraryListView', {libraryCount: this.libraries.length, libraries: this.libraries, defaultLibrary: this.defaultLibrary}));
                return this;
            },

            afterRender: function () {
            },

            getLibraries: function () {
                $.ajax({
                    url: "function-library",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.libraries = response;
                    }, this),
                    async: false
                });
            },

            removeFunction: function (event) {
                $('div#functions').find('#' + $(event.target).parent().attr('data-target')).remove();
                return false;
            },

            addFunction: function (event) {
                var functionName = $('#library-edit').find('input[name = "name"]').val();
                var functionClass = $('#library-edit').find('input[name = "clazz"]').val();

                $('#library-edit').find('div#functions').append('<div id="' + functionName + '"' + ' title="' + functionClass + '" class="list-group-item clickable"><i class="fa fa-file-text-o"></i>&nbsp;<b>' + functionName + '()</b> class=' + functionClass + '&nbsp;<a class="btn-remove-function pull-right" href="#config" title="Remove function" data-target="' + functionName + '"><i class="fa fa-times" style="color: #A50000;"></i></a></div>');

                $('#library-edit').find('div#functions').find('div:last').find('a.btn-remove-function').click(_.bind(function(event) {
                    this.removeFunction(event);
                }, this));

                $('input[name = "name"]').val('');
                $('input[name = "clazz"]').val('');
                return false;
            },

            remove: function (event) {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "function-library/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.getLibraries();
                        this.render();
                    }, this),
                    async: true
                });

                return false;
            },

            create: function() {
                var form = $('#library-edit-form');
                var serializedForm = form.serializeObject();

                $.ajax({
                    url: "function-library",
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getFunctionLibraryJSON(serializedForm),
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeForm(undefined, _.bind(function() {
                    this.getLibraries();
                    this.render();
                }, this));

                return false;
            },

            save: function() {
                var form = $('#library-edit-form');

                var serializedForm = form.serializeObject();
                var elementId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "function-library/" + elementId;
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getFunctionLibraryJSON(serializedForm),
                    success: _.bind(function (response) {
                    }, this),
                    async: false
                });

                this.closeForm(undefined, _.bind(function() {
                    this.getLibraries();
                    this.render();
                }, this));

                return false;
            },

            closeForm: function(event, callback) {
                $('#library-edit').hide('slide', function() {
                    $('#library-list').show('slide', function() {
                        if (callback) {
                            callback();
                        }
                    });
                });
            },

            showNewForm: function(event) {
                $('#library-edit').html(TemplateManager.template('FunctionLibraryEditView', {}));

                $('button.btn-option-search').click(_.bind(function(event) {
                    this.searchOptions(event);
                }, this));

                $('#library-edit').find('#btn-add-function').click(_.bind(function(event) {
                    this.addFunction(event);
                }, this));

                $('#library-list').hide('slide', function() {
                    $('#library-edit').show('slide');
                });
            },

            showEditForm: function() {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "function-library/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#library-edit').html(TemplateManager.template('FunctionLibraryEditView', response));

                        $('button.btn-option-search').click(_.bind(function(event) {
                            this.searchOptions(event);
                        }, this));

                        $('#library-edit').find('.btn-remove-function').click(_.bind(function(event) {
                            this.removeFunction(event);
                        }, this));

                        $('#library-edit').find('#btn-add-function').click(_.bind(function(event) {
                            this.addFunction(event);
                        }, this));

                        $('#library-edit').find('option').each(function() {
                            if ($(this).attr('value') == $(this).parent().attr('value')) {
                                $(this).attr('selected', 'selected');
                            }
                        });

                        $('#library-list').hide('slide', function() {
                            $('#library-edit').show('slide');
                        });
                    }, this),
                    async: true
                });
            },

            getFunctionLibraryJSON: function(serializedForm) {
                var functions = [];

                $('#library-edit').find('div#functions').children().each(function(index) {
                    functions.push( {name: $(this).attr('id'), clazz: $(this).attr('title')} );
                });

                var functionLibrary = { id: serializedForm.id, prefix: serializedForm.prefix, functions: functions };

                return JSON.stringify(functionLibrary);
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

        return FunctionLibraryListView;
    });
}).call(this);
