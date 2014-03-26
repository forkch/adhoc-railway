var turnoutController = require('../controllers/turnoutcontroller');

exports.init = function (server, sendDataToWebsocketClients) {
    server.get('/turnoutGroup', function (req, res, next) {
        turnoutController.getAllTurnoutGroups(function (err, data) {
            console.log(data);
            res.send(data.turnoutGroups);
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