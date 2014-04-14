var turnoutController = require('../controllers/turnoutcontroller');

exports.init = function (server, sendDataToWebsocketClients, sendResponse) {
    server.get('/turnoutGroup', function (req, res, next) {
        turnoutController.getAllTurnoutGroups(function (err, turnoutGroups) {
            sendResponse(err, res, next, 200, 404, turnoutGroups);
        });
    });
    server.del('/turnoutGroup', function (req, res, next) {
        turnoutController.clear(function (err, turnoutGroups) {
            sendResponse(err, res, next, 200, 400, turnoutGroups);
        });
    });

    server.get('/turnoutGroup/:id', function (req, res, next) {
        turnoutController.getTurnoutGroupById(req.params.id, function (err, turnoutGroup) {
            sendResponse(err, res, next, 200, 404, turnoutGroup);
        });
    });

    server.post('/turnoutGroup', function (req, res, next) {
        turnoutController.addTurnoutGroup(req.params, function (err, turnoutGroup) {
            sendDataToWebsocketClients(err, req, 'turnoutGroup:added', turnoutGroup);
            sendResponse(err, res, next, 201, 400, turnoutGroup);
        });
    });

    server.put('/turnoutGroup', function (req, res, next) {
        turnoutController.updateTurnoutGroup(req.params, function (err, turnoutGroup) {
            sendDataToWebsocketClients(err, req, 'turnoutGroup:updated', turnoutGroup);
            sendResponse(err, res, next, 201, 400, turnoutGroup);
        });
    });

    server.del('/turnoutGroup/:id', function (req, res, next) {
        turnoutController.removeTurnoutGroup(req.params.id, function (err, turnoutGroup) {

            sendDataToWebsocketClients(err, req, 'turnoutGroup:removed', turnoutGroup);
            sendResponse(err, res, next, 200, 404, turnoutGroup);
        });
    });

    server.get('/turnout/:id', function (req, res, next) {
        turnoutController.getTurnoutById(req.params.id, function (err, turnout) {
            sendResponse(err, res, next, 200, 404, turnout);
        });
    });

    server.post('/turnout', function (req, res, next) {
        turnoutController.addTurnout(req.params, function (err, turnout) {

            sendDataToWebsocketClients(err, req, 'turnout:added', turnout);
            sendResponse(err, res, next, 201, 400, turnout);
        });
    });

    server.put('/turnout', function (req, res, next) {
        turnoutController.updateTurnout(req.params, function (err, turnout) {

            sendDataToWebsocketClients(err, req, 'turnout:updated', turnout);
            sendResponse(err, res, next, 201, 400, turnout);
        });
    });

    server.del('/turnout/:id', function (req, res, next) {
        turnoutController.removeTurnout(req.params.id, function (err, turnout) {

            sendDataToWebsocketClients(err, req, 'turnout:removed', turnout);
            sendResponse(err, res, next, 201, 404, turnout);
        });
    });
}