var locomotiveController = require('../controllers/locomotivecontroller');

exports.init = function (server, sendDataToWebsocketClients) {
    server.get('/locomotiveGroup', function (req, res, next) {
        locomotiveController.getAllLocomotiveGroups(function (err, data) {
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
            sendDataToWebsocketClients('locomotiveGroup:added', locomotiveGroup);
            next();
        });
    });

    server.put('/locomotiveGroup', function (req, res, next) {
        locomotiveController.updateLocomotiveGroup(req.params, function (err, locomotiveGroup) {
            res.send(locomotiveGroup);
            sendDataToWebsocketClients('locomotiveGroup:updated', locomotiveGroup);
            next();
        });
    });

    server.del('/locomotiveGroup/:id', function (req, res, next) {
        locomotiveController.removeLocomotiveGroup(req.params.id, function (err, locomotiveGroupId) {
            res.send(locomotiveGroupId);
            sendDataToWebsocketClients('locomotiveGroup:removed', locomotiveGroup);
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
            res.send(locomotive._id);
            sendDataToWebsocketClients('locomotive:added', locomotive);
            next();
        });
    });

    server.put('/locomotive', function (req, res, next) {
        locomotiveController.updateLocomotive(req.params, function (err, locomotive) {
            res.send(locomotive);
            sendDataToWebsocketClients('locomotive:updated', locomotive);
            next();
        });
    });

    server.del('/locomotive/:id', function (req, res, next) {
        locomotiveController.removeLocomotive(req.params.id, function (err, locomotiveId) {
            res.send(locomotiveId);
            sendDataToWebsocketClients('locomotive:removed', locomotiveId);
            next();
        });
    });

}