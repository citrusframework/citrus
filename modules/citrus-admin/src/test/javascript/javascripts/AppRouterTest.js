define([], function() {
    describe("AppRouter", function() {
      it("should have routes setup", function() {
          expect(CitrusAdmin.routes['']).toEqual('project');
          expect(CitrusAdmin.routes['project']).toEqual('project');
          expect(CitrusAdmin.routes['config']).toEqual('config');
          expect(CitrusAdmin.routes['tests']).toEqual('tests');
          expect(CitrusAdmin.routes['stats']).toEqual('stats');
          expect(CitrusAdmin.routes['settings']).toEqual('settings');
          expect(CitrusAdmin.routes['about']).toEqual('about');
      });
      
      it("should redirect home route to project page", function() {
          var routeSpy = sinon.spy();
          
          CitrusAdmin.bind("route:project", routeSpy);
          
          CitrusAdmin.navigate("elsewhere");
          CitrusAdmin.navigate('', true);
          
          expect(routeSpy.calledOnce).toBeTruthy();
      });
      
      it("should have project route", function() {
          var routeSpy = sinon.spy();
          
          CitrusAdmin.bind("route:project", routeSpy);
          
          CitrusAdmin.navigate("elsewhere");
          CitrusAdmin.navigate('project', true);
          
          expect(routeSpy.called).toBeTruthy();
      });
    });
});