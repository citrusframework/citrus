define([], function() {
    describe("AppRouter", function() {
      it("should have routes setup", function() {
          expect(CitrusAdmin.routes['']).toEqual('welcome');
          expect(CitrusAdmin.routes['project']).toEqual('welcome');
          expect(CitrusAdmin.routes['config']).toEqual('config');
          expect(CitrusAdmin.routes['testcases']).toEqual('testcases');
          expect(CitrusAdmin.routes['stats']).toEqual('stats');
          expect(CitrusAdmin.routes['about']).toEqual('about');
      });
      
      it("should redirect home route to welcome page", function() {
          var routeSpy = sinon.spy();
          
          CitrusAdmin.bind("route:welcome", routeSpy);
          
          CitrusAdmin.navigate("elsewhere");
          CitrusAdmin.navigate('', true);
          
          expect(routeSpy.calledOnce).toBeTruthy();
      });
      
      it("should have project route", function() {
          var routeSpy = sinon.spy();
          
          CitrusAdmin.bind("route:welcome", routeSpy);
          
          CitrusAdmin.navigate("elsewhere");
          CitrusAdmin.navigate('project', true);
          
          expect(routeSpy.called).toBeTruthy();
      });
    });
});