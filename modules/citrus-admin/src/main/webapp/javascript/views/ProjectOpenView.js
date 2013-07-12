(function() {
    define(["TemplateManager"], function(TemplateManager) {
        var ProjectOpenView = Backbone.View.extend({
          
          projectHome: "",
          
          events: {
              "click #button-browse" : "showFileTree",
              "click #button-close" : "hideFileTree",
              "click #button-select" : "selectProject"
          },

          initialize: function() {
              $.ajax({
                  url: "config/projecthome",
                  type: 'GET',
                  dataType: "text",
                  success: _.bind(function(response) {
                      this.projectHome = response;
                  }, this),
                  async: false
              });
          },
          
          render: function() {
              $(this.el).html(TemplateManager.template('ProjectOpenView', {projectHome: this.projectHome}));
              
              $('#file-tree').fileTree({ 
                  root: '/',
                  script: 'project',
                  multiFolder: false,
                  expandSpeed: 1, 
                  collapseSpeed: 1
              }, function(file) {
                  $('input[name="projecthome"]').val(file);
                  $('#file-tree').hide();
              });
              
              return this;
          },
          
          showFileTree: function() {
              $('#dialog-file-tree').modal();
          },
          
          hideFileTree: function() {
              $('#dialog-file-tree').modal('hide');
          },
          
          selectProject: function() {
              var selected = $('ul.jqueryFileTree li.expanded').last().children('a:first').attr('rel');
              $('input[name="projecthome"]').val(selected);
              $('#dialog-file-tree').modal('hide');
          }
        
        });
        
        return ProjectOpenView;
    });
}).call(this);