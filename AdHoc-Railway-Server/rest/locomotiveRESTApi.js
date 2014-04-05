var locomotiveController = require('../controllers/locomotivecontroller');

exports.init = function (server, sendDataToWebsocketClients) {
    server.get('/locomotiveGroup', function (req, res, next) {
        locomotiveController.getAllLocomotiveGroups(function (err, locomotiveGroups) {
            res.send(locomotiveGroups);
            next();
        });
    });
    server.del('/locomotiveGroup', function (req, res, next) {
        locomotiveController.clear(function (err, data) {
            res.send(data);
            next();
        });
    });

    server.get('/locomotiveGroup/:id', function (req, res, next) {
        locomotiveController.getLocomotiveGroupById(req.params.id, function (err, locomotiveGroup) {
            res.send(locomotiveGroup);
            next();
        });
    });

    server.post('/locomotiveGroup', function (req, res, next) {
        locomotiveController.addLocomotiveGroup(req.params, function (err, locomotiveGroup) {
            res.send(locomotiveGroup);
            sendDataToWebsocketClients(req, 'locomotiveGroup:added', locomotiveGroup);
            next();
        });
    });

    server.put('/locomotiveGroup', function (req, res, next) {
        locomotiveController.updateLocomotiveGroup(req.params, function (err, locomotiveGroup) {
            res.send(locomotiveGroup);
            sendDataToWebsocketClients(req, 'locomotiveGroup:updated', locomotiveGroup);
            next();
        });
    });

    server.del('/locomotiveGroup/:id', function (req, res, next) {
        locomotiveController.removeLocomotiveGroup(req.params.id, function (err, locomotiveGroup) {
            res.send(locomotiveGroup);
            sendDataToWebsocketClients(req, 'locomotiveGroup:removed', locomotiveGroup);
            next();
        });
    });

    server.del('/locomotiveGroup/:id', function (req, res, next) {
        locomotiveController.clear(req.params.id, function (err, locomotiveGroup) {
            res.send(locomotiveGroup);
            sendDataToWebsocketClients(req, 'locomotiveGroup:removed', locomotiveGroup);
            next();
        });
    });

    server.get('/locomotive/:id', function (req, res, next) {
        locomotiveController.getLocomotiveById(req.params.id, function (err, locomotive) {
            res.send(locomotive);
            next();
        });
    });

    server.post('/locomotive', function (req, res, next) {
        locomotiveController.addLocomotive(req.params, function (err, locomotive) {
            res.send(locomotive);
            sendDataToWebsocketClients(req, 'locomotive:added', locomotive);
            next();
        });
    });

    server.put('/locomotive', function (req, res, next) {
        locomotiveController.updateLocomotive(req.params, function (err, locomotive) {
            res.send(locomotive);
            sendDataToWebsocketClients(req, 'locomotive:updated', locomotive);
            next();
        });
    });

    server.del('/locomotive/:id', function (req, res, next) {
        locomotiveController.removeLocomotive(req.params.id, function (err, locomotive) {
            res.send(locomotive);
            sendDataToWebsocketClients(req, 'locomotive:removed', locomotiveId);
            next();
        });
    });

}