(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var ConfigXsdSchemaView = Backbone.View.extend({
            schemas: undefined,
            schemaRepositories: undefined,

            events: {
                "click #btn-new-config-xsd-schema": "showNewXsdSchemaForm",
                "click #btn-save-config-xsd-schema": "createXsdSchema",
                "click #btn-update-config-xsd-schema": "updateXsdSchema",
                "click #btn-cancel-config-xsd-schema": "closeXsdSchemaForm",
                "click #btn-new-config-schema-repository": "showNewXsdSchemaRepositoryForm",
                "click #btn-save-config-schema-repository": "createXsdSchemaRepository",
                "click #btn-update-config-schema-repository": "updateXsdSchemaRepository",
                "click #btn-cancel-config-schema-repository": "closeXsdSchemaRepositoryForm",
                "click .edit-config-xsd-schema": "showEditXsdSchemaForm",
                "click .del-config-xsd-schema": "removeXsdSchema",
                "click .edit-config-schema-repository": "showEditXsdSchemaRepositoryForm",
                "click .del-config-schema-repository": "removeXsdSchemaRepository",
                "submit #form-filter-config-xsd-schemas": "filterXsdSchemas",
                "click #btn-reload-config-xsd-schemas": "reload",
                "click #btn-reload-config-schema-repository": "reload",
                "click a.xsd-schema-select": "selectXsdSchema",
                "click #btn-add-config-xsd-schema-ref": "addXsdSchemaReference",
                "click input[name='schema-ref']": "showXsdSchemaReferenceSelect"
            },

            initialize: function () {
            },

            render: function () {
                $(this.el).html(TemplateManager.template('ConfigXsdSchemaView'));
                return this;
            },

            afterRender: function () {
                $('#input-filter-config-xsd-schemas').keyup(_.bind(function () {
                    this.filterXsdSchemas();
                }, this));

                this.reload();
            },

            reload: function() {
                this.getXsdSchemas();
                this.getXsdSchemaRepositories();
            },

            getXsdSchemas: function () {
                $.ajax({
                    url: "config/xsd-schema",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.schemas = response;
                        $("#config-xsd-schemas-table").html(TemplateManager.template('XsdSchemaTableView', {matches: this.schemas}));
                        $('#input-filter-config-xsd-schemas').val('');
                    }, this),
                    async: true
                });
            },

            getXsdSchemaRepositories: function () {
                $.ajax({
                    url: "config/xsd-schema-repository",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.schemaRepositories = response;
                        $("#config-schema-repository-table").html(TemplateManager.template('XsdSchemaRepositoryTableView', {matches: this.schemaRepositories}));
                    }, this),
                    async: true
                });
            },

            filterXsdSchemas: function () {
                var searchKey = $('#input-filter-config-xsd-schemas').val();

                if (searchKey.length) {
                    $("#config-xsd-schemas-table").html(TemplateManager.template('XsdSchemaTableView', {matches: _.filter(this.schemas, function (schema) {
                        // regex: match all key names (e.g. "keyname":) in JSON String
                        var regex = /"(\w|0-9|_)+":/;
                        // replace key names with empty string and only search for match in values
                        return JSON.stringify(schema).replace(regex,"").indexOf(searchKey) >= 0;
                    })}));
                } else {
                    $("#config-xsd-schemas-table").html(TemplateManager.template('XsdSchemaTableView', {matches: this.schemas}));
                }

                return false;
            },

            removeXsdSchema: function (event) {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/xsd-schema/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });

            },

            removeXsdSchemaRepository: function (event) {
                var encodedId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedId);
                var url = "config/xsd-schema-repository/" + id;

                $.ajax({
                    url: url,
                    type: 'DELETE',
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });

            },

            showNewXsdSchemaForm: function() {
                $('#dialog-edit-config-xsd-schema').html(TemplateManager.template('XsdSchemaEditView', {schema: undefined}));
                $('#dialog-edit-config-xsd-schema .modal').modal();

            },

            showNewXsdSchemaRepositoryForm: function() {
                $('#dialog-edit-config-schema-repository').html(TemplateManager.template('XsdSchemaRepositoryEditView', {schemaRepository: undefined, schemas: this.schemas}));
                $('#dialog-edit-config-schema-repository .modal').modal();

            },

            showEditXsdSchemaForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/xsd-schema/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#dialog-edit-config-xsd-schema').html(TemplateManager.template('XsdSchemaEditView', {schema: response}));
                        $('#dialog-edit-config-xsd-schema .modal').modal();
                    }, this),
                    async: true
                });
            },

            showEditXsdSchemaRepositoryForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/xsd-schema-repository/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#dialog-edit-config-schema-repository').html(TemplateManager.template('XsdSchemaRepositoryEditView', {schemaRepository: response, schemas: this.schemas}));
                        $('#dialog-edit-config-schema-repository .modal').modal();
                    }, this),
                    async: true
                });
            },

            createXsdSchema: function() {
                var form = $('#form-edit-config-xsd-schema form');
                this.closeXsdSchemaForm();

                var serializedForm = form.serializeObject();
                var jsonForm = JSON.stringify(serializedForm);
                var url = "config/xsd-schema";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
                return false;
            },

            createXsdSchemaRepository: function() {
                var form = $('#form-edit-config-schema-repository form');
                this.closeXsdSchemaRepositoryForm();

                var serializedForm = form.serializeObject();
                var url = "config/xsd-schema-repository";
                $.ajax({
                    url: url,
                    type: 'POST',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getSchemaRepositoryJSON(serializedForm),
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
                return false;
            },

            updateXsdSchema: function() {
                var form = $('#form-edit-config-xsd-schema form');
                this.closeXsdSchemaForm();

                var serializedForm = form.serializeObject();
                var schemaId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var jsonForm = JSON.stringify(serializedForm);
                var url = "config/xsd-schema/" + schemaId;
                $.ajax({
                    url: url,
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: jsonForm,
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
                return false;
            },

            updateXsdSchemaRepository: function() {
                var form = $('#form-edit-config-schema-repository form');
                this.closeXsdSchemaRepositoryForm();

                var serializedForm = form.serializeObject();
                var schemaRepositoryId = serializedForm.id;

                if (serializedForm.id != serializedForm.newId) {
                    serializedForm.id = serializedForm.newId;
                }

                serializedForm = _.omit(serializedForm, "newId");

                var url = "config/xsd-schema-repository/" + schemaRepositoryId;
                $.ajax({
                    url: url,
                    type: 'PUT',
                    dataType: "json",
                    contentType: "application/json",
                    data: this.getSchemaRepositoryJSON(serializedForm),
                    success: _.bind(function (response) {
                        this.reload();
                    }, this),
                    async: true
                });
                return false;
            },

            getSchemaRepositoryJSON: function(serializedForm) {
                var schemas = [];

                $('ul#xsd-schema-refs').children('li').each(function(index) {
                    schemas.push({ ref: $(this).attr('id') });
                });

                var schemaRepository = { id: serializedForm.id, schemas: { schemas: schemas } };

                return JSON.stringify(schemaRepository);
            },

            closeXsdSchemaForm: function() {
                $('#dialog-edit-config-xsd-schema .modal').modal('hide');
            },

            closeXsdSchemaRepositoryForm: function() {
                $('#dialog-edit-config-schema-repository .modal').modal('hide');
            },

            extractId: function(encodedId) {
                var splitString = encodedId.split('-');
                return splitString[splitString.length-1];
            },

            selectXsdSchema: function(event) {
                $('input[name="schema-ref"]').val(event.currentTarget.innerText);
                $('#xsd-schema-ref-dropdown').removeClass('open');
                return false;
            },

            addXsdSchemaReference: function() {
                var schemaId = $('input[name="schema-ref"]').val();
                if (schemaId.length) {
                    if ($('ul#xsd-schema-refs').children('li#' + schemaId).size() == 0) {
                        $('ul#xsd-schema-refs').append('<li id="' + schemaId + '"><i class="icon-file-text-alt"></i>&nbsp;' + schemaId + '</li>');
                    }

                    $('input[name="schema-ref"]').val('');
                }

                return false;
            },

            showXsdSchemaReferenceSelect: function(event) {
                $('#xsd-schema-ref-dropdown').addClass('open');
                event.stopPropagation();
            }

        });

        return ConfigXsdSchemaView;
    });
}).call(this);
