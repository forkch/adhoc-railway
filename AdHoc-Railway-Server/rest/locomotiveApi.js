var locomotiveController = require('../controllers/locomotivecontroller');

exports.init = function (server, sendDataToWebsocketClients) {
    server.get('/locomotiveGroup', function (req, res, next) {
        locomotiveController.getAllLocomotiveGroups(function (err, data) {
            console.log(data);
            res.send(data.locomotiveGroups);
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
            res.send(locomotive._id);
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