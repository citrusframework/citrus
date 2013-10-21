(function () {
    define(["TemplateManager"], function (TemplateManager) {
        var SchemaDefinitionView = Backbone.View.extend({
            schemas: undefined,
            schemaRepositories: undefined,

            events: {
                "click #new-schema": "showNewSchemaForm",
                "click #btn-save-config-xsd-schema": "createSchema",
                "click #btn-update-config-xsd-schema": "updateSchema",
                "click #btn-cancel-config-xsd-schema": "closeSchemaForm",
                "click #new-schema-repository": "showNewSchemaRepositoryForm",
                "click #btn-save-config-schema-repository": "createSchemaRepository",
                "click #btn-update-config-schema-repository": "updateSchemaRepository",
                "click #btn-cancel-config-schema-repository": "closeSchemaRepositoryForm",
                "click .config-item": "showEditSchemaForm",
                "click .config-item": "showEditSchemaRepositoryForm",
                "click a.xsd-schema-select": "selectSchema",
                "click #btn-add-config-xsd-schema-ref": "addSchemaReference",
                "click input[name='schema-ref']": "showSchemaReferenceSelect"
            },

            initialize: function () {
            },

            render: function () {
                this.reload();

                $(this.el).html(TemplateManager.template('SchemaDefinitionView', {schemaRepositories: this.schemaRepositories, schemas: this.schemas}));
                return this;
            },

            afterRender: function () {
            },

            reload: function() {
                this.getSchemas();
                this.getSchemaRepositories();
            },

            getSchemas: function () {
                $.ajax({
                    url: "config/xsd-schema",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.schemas = response;
                    }, this),
                    async: false
                });
            },

            getSchemaRepositories: function () {
                $.ajax({
                    url: "config/xsd-schema-repository",
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        this.schemaRepositories = response;
                    }, this),
                    async: false
                });
            },

            removeSchema: function (event) {
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

            removeSchemaRepository: function (event) {
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

            showNewSchemaForm: function() {
                $('#dialog-edit-config-xsd-schema').html(TemplateManager.template('SchemaEditView', {schema: undefined}));
                $('#dialog-edit-config-xsd-schema .modal').modal();

            },

            showNewSchemaRepositoryForm: function() {
                $('#dialog-edit-config-schema-repository').html(TemplateManager.template('SchemaRepositoryEditView', {schemaRepository: undefined, schemas: this.schemas}));
                $('#dialog-edit-config-schema-repository .modal').modal();

            },

            showEditSchemaForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/xsd-schema/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#dialog-edit-config-xsd-schema').html(TemplateManager.template('SchemaEditView', {schema: response}));
                        $('#dialog-edit-config-xsd-schema .modal').modal();
                    }, this),
                    async: true
                });
            },

            showEditSchemaRepositoryForm: function() {
                var encodedSchemaId = $(event.target).closest($("[id]")).attr('id');
                var id = this.extractId(encodedSchemaId);
                var url = "config/xsd-schema-repository/" + id;

                $.ajax({
                    url: url,
                    type: 'GET',
                    dataType: "json",
                    success: _.bind(function (response) {
                        $('#dialog-edit-config-schema-repository').html(TemplateManager.template('SchemaRepositoryEditView', {schemaRepository: response, schemas: this.schemas}));
                        $('#dialog-edit-config-schema-repository .modal').modal();
                    }, this),
                    async: true
                });
            },

            createSchema: function() {
                var form = $('#form-edit-config-xsd-schema form');
                this.closeSchemaForm();

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

            createSchemaRepository: function() {
                var form = $('#form-edit-config-schema-repository form');
                this.closeSchemaRepositoryForm();

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

            updateSchema: function() {
                var form = $('#form-edit-config-xsd-schema form');
                this.closeSchemaForm();

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

            updateSchemaRepository: function() {
                var form = $('#form-edit-config-schema-repository form');
                this.closeSchemaRepositoryForm();

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

            closeSchemaForm: function() {
                $('#dialog-edit-config-xsd-schema .modal').modal('hide');
            },

            closeSchemaRepositoryForm: function() {
                $('#dialog-edit-config-schema-repository .modal').modal('hide');
            },

            extractId: function(encodedId) {
                var splitString = encodedId.split('-');
                return splitString[splitString.length-1];
            },

            selectSchema: function(event) {
                $('input[name="schema-ref"]').val(event.currentTarget.innerText);
                $('#xsd-schema-ref-dropdown').removeClass('open');
                return false;
            },

            addSchemaReference: function() {
                var schemaId = $('input[name="schema-ref"]').val();
                if (schemaId.length) {
                    if ($('ul#xsd-schema-refs').children('li#' + schemaId).size() == 0) {
                        $('ul#xsd-schema-refs').append('<li id="' + schemaId + '"><i class="icon-file-text-alt"></i>&nbsp;' + schemaId + '</li>');
                    }

                    $('input[name="schema-ref"]').val('');
                }

                return false;
            },

            showSchemaReferenceSelect: function(event) {
                $('#xsd-schema-ref-dropdown').addClass('open');
                event.stopPropagation();
            }

        });

        return SchemaDefinitionView;
    });
}).call(this);
