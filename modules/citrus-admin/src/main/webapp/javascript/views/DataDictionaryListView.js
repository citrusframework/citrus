(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var DataDictionaryListView = Backbone.View.extend({
            dictionaries: [],

            events: {
                "click div.dictionary": "showEditForm",
                "click .btn-new": "showNewForm",
                "click .btn-remove": "remove",
                "click #btn-add": "create",
                "click #btn-save": "save",
                "click #btn-cancel": "closeForm"
            },

            initialize: function () {
                this.getLibraries();
            },

            render: function () {
                $(this.el).html(TemplateManager.template('DataDictionaryListView', {dictionaryCount: this.dictionaries.length, dictionaries: this.dictionaries}));
                return this;
            },

            afterRender: function () {
            },

            getLibraries: function () {
                $.ajax({
                    url: "data-dictionary",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.dictionaries = response;
                    }, this),
                    async: false
                });
            },

            removeMapping: function (event) {
                $('div#mappings').find('#' + $(event.target).parent().attr('data-target')).remove();
                return false;
            },

            addMapping: function (event) {
                var mappingKey = $('#dictionary-edit').find('input[name = "path"]').val();
                var mappingValue = $('#dictionary-edit').find('input[name = "value"]').val();
                var uniqueId = _.uniqueId('new_');

                $('#dictionary-edit').find('div#mappings').append('<div id="' + uniqueId + '"' + ' title="' + mappingValue + '" class="list-group-item clickable"><i class="fa fa-file-text-o"></i>&nbsp;<b>' + mappingKey + '</b>=' + mappingValue + '&nbsp;<a class="btn-remove-mapping pull-right" href="#config" title="Remove mapping" data-target="' + uniqueId + '"><i class="fa fa-times" style="color: #A50000;"></i></a></div>');

                $('#dictionary-edit').find('div#mappings').find('div:last').find('a.btn-remove-mapping').click(_.bind(function(event) {
                    this.removeMapping(event);
                }, this));

                $('input[name = "path"]').val('');
                $('input[name = "value"]').val('');
                return false;
            },

            remove: function (event) {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "data-dictionary/" + id;

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
                var form = $('#dictionary-edit-form');
                var serializedForm = form.serializeObject();

                $.ajax({
                    url: "data-dictionary",
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getDataDictionaryJSON(serializedForm),
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
                var form = $('#dictionary-edit-form');

                var serializedForm = form.serializeObject();
                var elementId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "data-dictionary/" + elementId;
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getDataDictionaryJSON(serializedForm),
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
                $('#dictionary-edit').hide('slide', function() {
                    $('#dictionary-list').show('slide', function() {
                        if (callback) {
                            callback();
                        }
                    });
                });
            },

            showNewForm: function(event) {
                $('#dictionary-edit').html(TemplateManager.template('DataDictionaryEditView', {}));

                $('button.btn-option-search').click(_.bind(function(event) {
                    this.searchOptions(event);
                }, this));

                $('#dictionary-edit').find('#btn-add-mapping').click(_.bind(function(event) {
                    this.addMapping(event);
                }, this));

                $('#dictionary-list').hide('slide', function() {
                    $('#dictionary-edit').show('slide');
                });
            },

            showEditForm: function() {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "data-dictionary/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#dictionary-edit').html(TemplateManager.template('DataDictionaryEditView', response));

                        $('button.btn-option-search').click(_.bind(function(event) {
                            this.searchOptions(event);
                        }, this));

                        $('#dictionary-edit').find('.btn-remove-mapping').click(_.bind(function(event) {
                            this.removeMapping(event);
                        }, this));

                        $('#dictionary-edit').find('#btn-add-mapping').click(_.bind(function(event) {
                            this.addMapping(event);
                        }, this));

                        $('#dictionary-edit').find('option').each(function() {
                            if ($(this).attr('value') == $(this).parent().attr('value')) {
                                $(this).attr('selected', 'selected');
                            }
                        });

                        $('#dictionary-list').hide('slide', function() {
                            $('#dictionary-edit').show('slide');
                        });
                    }, this),
                    async: true
                });
            },

            getDataDictionaryJSON: function(serializedForm) {
                var mappings = [];

                $('#dictionary-edit').find('div#mappings').children().each(function(index) {
                    mappings.push( {path: $(this).attr('id'), value: $(this).attr('title')} );
                });

                var dataDictionary = { id: serializedForm.id,
                                        type: serializedForm.type,
                                        globalScope: serializedForm.globalScope,
                                        mappingStrategy: serializedForm.mappingStrategy,
                                        mappings: { mappings: mappings} };

                return JSON.stringify(dataDictionary);
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

        return DataDictionaryListView;
    });
}).call(this);
