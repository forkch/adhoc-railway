var turnoutController = require('../controllers/turnoutcontroller');

exports.init = function (server, sendDataToWebsocketClients) {
    server.get('/turnoutGroup', function (req, res, next) {
        turnoutController.getAllTurnoutGroups(function (err, turnoutGroups) {
            console.log(turnoutGroups);
            res.send(turnoutGroups);
            next();
        });
    });
    server.del('/turnoutGroup', function (req, res, next) {
        turnoutController.clear(function (err, data) {
            res.send(data);
            next();

        });
    });

    server.get('/turnoutGroup/:id', function (req, res, next) {
        turnoutController.getTurnoutGroupById(req.params.id, function (err, turnoutGroup) {
            res.send(turnoutGroup);
            next();
        });
    });

    server.post('/turnoutGroup', function (req, res, next) {
        turnoutController.addTurnoutGroup(req.params, function (err, turnoutGroup) {
            res.send(turnoutGroup);
            sendDataToWebsocketClients('turnoutGroup:added', turnoutGroup);
            next();
        });
    });

    server.put('/turnoutGroup', function (req, res, next) {
        turnoutController.updateTurnoutGroup(req.params, function (err, turnoutGroup) {
            res.send(turnoutGroup);
            sendDataToWebsocketClients('turnoutGroup:updated', turnoutGroup);
            next();
        });
    });

    server.del('/turnoutGroup/:id', function (req, res, next) {
        turnoutController.removeTurnoutGroup(req.params.id, function (err, turnoutGroup) {
            res.send(turnoutGroup);
            sendDataToWebsocketClients('turnoutGroup:removed', turnoutGroup);
            next();
        });
    });

    server.get('/turnout/:id', function (req, res, next) {
        turnoutController.getTurnoutById(req.params.id, function (err, turnout) {
            res.send(turnout);
            next();
        });
    });

    server.post('/turnout', function (req, res, next) {
        turnoutController.addTurnout(req.params, function (err, turnout) {
            res.send(turnout._id);
            sendDataToWebsocketClients('turnout:added', turnout);
            next();
        });
    });

    server.put('/turnout', function (req, res, next) {
        turnoutController.updateTurnout(req.params, function (err, turnout) {
            res.send(turnout._id);
            sendDataToWebsocketClients('turnout:updated', turnout);
            next();
        });
    });

    server.del('/turnout/:id', function (req, res, next) {
        turnoutController.removeTurnout(req.params.id, function (err, turnoutId) {
            res.send(turnoutId);
            sendDataToWebsocketClients('turnout:removed', turnoutId);
            next();
        });
    });

}