var routeController = require('../controllers/routecontroller');

exports.init = function (server, sendDataToWebsocketClients, sendResponse) {
    server.get('/routeGroup', function (req, res, next) {
        routeController.getAllRouteGroups(function (err, routeGroups) {
            sendResponse(err, res, next, 200, 404, routeGroups);
        });
    });
    server.del('/routeGroup', function (req, res, next) {
        routeController.clear(function (err, routeGroups) {
            sendResponse(err, res, next, 200, 400, routeGroups);
        });
    });

    server.get('/routeGroup/:id', function (req, res, next) {
        routeController.getRouteGroupById(req.params.id, function (err, routeGroup) {
            sendResponse(err, res, next, 200, 404, routeGroup);
        });
    });

    server.post('/routeGroup', function (req, res, next) {
        routeController.addRouteGroup(req.params, function (err, routeGroup) {
            sendDataToWebsocketClients(err, req, 'routeGroup:added', routeGroup);
            sendResponse(err, res, next, 201, 400, routeGroup);
        });
    });

    server.put('/routeGroup', function (req, res, next) {
        routeController.updateRouteGroup(req.params, function (err, routeGroup) {
            sendDataToWebsocketClients(err, req, 'routeGroup:updated', routeGroup);
            sendResponse(err, res, next, 201, 400, routeGroup);
        });
    });

    server.del('/routeGroup/:id', function (req, res, next) {
        routeController.removeRouteGroup(req.params.id, function (err, routeGroup) {

            sendDataToWebsocketClients(err, req, 'routeGroup:removed', routeGroup);
            sendResponse(err, res, next, 200, 404, routeGroup);
        });
    });

    server.get('/route/:id', function (req, res, next) {
        routeController.getRouteById(req.params.id, function (err, route) {
            sendResponse(err, res, next, 200, 404, route);
        });
    });

    server.post('/route', function (req, res, next) {
        routeController.addRoute(req.params, function (err, route) {

            sendDataToWebsocketClients(err, req, 'route:added', route);
            sendResponse(err, res, next, 201, 400, route);
        });
    });

    server.put('/route', function (req, res, next) {
        routeController.updateRoute(req.params, function (err, route) {

            sendDataToWebsocketClients(err, req, 'route:updated', route);
            sendResponse(err, res, next, 201, 400, route);
        });
    });

    server.del('/route/:id', function (req, res, next) {
        routeController.removeRoute(req.params.id, function (err, route) {

            sendDataToWebsocketClients(err, req, 'route:removed', route);
            sendResponse(err, res, next, 201, 404, route);
        });
    });
}