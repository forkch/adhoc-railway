var locomotiveController = require('../controllers/locomotivecontroller');

exports.init = function (server, sendDataToWebsocketClients, sendResponse) {
    server.get('/locomotiveGroup', function (req, res, next) {
        locomotiveController.getAllLocomotiveGroups(function (err, locomotiveGroups) {
            sendResponse(err, res, next, 200, 404, locomotiveGroups);
        });
    });
    server.del('/locomotiveGroup', function (req, res, next) {
        locomotiveController.clear(function (err, data) {
            sendResponse(err, res, next, 200, 404, data);
        });
    });

    server.get('/locomotiveGroup/:id', function (req, res, next) {
        locomotiveController.getLocomotiveGroupById(req.params.id, function (err, locomotiveGroup) {
            sendResponse(err, res, next, 200, 404, locomotiveGroup);
        });
    });

    server.post('/locomotiveGroup', function (req, res, next) {
        locomotiveController.addLocomotiveGroup(req.params, function (err, locomotiveGroup) {
            sendDataToWebsocketClients(req, 'locomotiveGroup:added', locomotiveGroup);
            sendResponse(err, res, next, 201, 400, locomotiveGroup);
        });
    });

    server.put('/locomotiveGroup', function (req, res, next) {
        locomotiveController.updateLocomotiveGroup(req.params, function (err, locomotiveGroup) {
            sendDataToWebsocketClients(req, 'locomotiveGroup:updated', locomotiveGroup);
            sendResponse(err, res, next, 201, 400, locomotiveGroup);
        });
    });

    server.del('/locomotiveGroup/:id', function (req, res, next) {
        locomotiveController.removeLocomotiveGroup(req.params.id, function (err, locomotiveGroup) {
            sendDataToWebsocketClients(req, 'locomotiveGroup:removed', locomotiveGroup);
            sendResponse(err, res, next, 200, 404, locomotiveGroup);
        });
    });

    server.get('/locomotive/:id', function (req, res, next) {
        locomotiveController.getLocomotiveById(req.params.id, function (err, locomotive) {
            sendResponse(err, res, next, 200, 404, locomotive);
        });
    });

    server.post('/locomotive', function (req, res, next) {
        locomotiveController.addLocomotive(req.params, function (err, locomotive) {
            sendDataToWebsocketClients(req, 'locomotive:added', locomotive);
            sendResponse(err, res, next, 201, 400, locomotive);
        });
    });

    server.put('/locomotive', function (req, res, next) {
        locomotiveController.updateLocomotive(req.params, function (err, locomotive) {
            sendDataToWebsocketClients(req, 'locomotive:updated', locomotive);
            sendResponse(err, res, next, 201, 400, locomotive);
        });
    });

    server.del('/locomotive/:id', function (req, res, next) {
        locomotiveController.removeLocomotive(req.params.id, function (err, locomotive) {
            sendDataToWebsocketClients(req, 'locomotive:removed', locomotive);
            sendResponse(err, res, next, 200, 404, locomotive);
        });
    });

}