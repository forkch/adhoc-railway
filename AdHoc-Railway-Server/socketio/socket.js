/*
 * Serve content over a socket
 */
var turnoutController = require('./../controllers/turnoutcontroller');
var routeController = require('./../controllers/routecontroller');
var locomotiveController = require('./../controllers/locomotivecontroller');


module.exports = function (socket) {

    function sendBroadcastToClients(err, event, data, fn) {
        if (!err) {
            socket.broadcast.emit(event, data);
        }
        fn(err, data);
    }

    locomotiveController.init(socket);
    turnoutController.init(socket);
    routeController.init(socket);

    socket.on('turnoutGroup:getAll', function (dummy, fn) {
        console.log('turnoutGroup:getAll');
        turnoutController.getAllTurnoutGroups(socket, fn);
    });

    socket.on('turnoutGroup:getById', function (turnoutId, fn) {
        console.log('turnoutGroup:getById');
        turnoutController.getTurnoutGroupById(socket, turnoutId, fn);
    });

    socket.on('turnoutGroup:add', function (turnoutGroupName, fn) {
        console.log('turnoutGroup:add');
        turnoutController.addTurnoutGroup(socket, turnoutGroupName, fn);
    });

    socket.on('turnoutGroup:update', function (turnoutGroup, fn) {
        console.log('turnoutGroup:update');
        turnoutController.updateTurnoutGroup(socket, turnoutGroup, fn);
    });
    socket.on('turnoutGroup:remove', function (turnoutGroup, fn) {
        console.log('turnoutGroup:remove');
        turnoutController.removeTurnoutGroup(socket, turnoutGroup, fn);
    });

    socket.on('turnout:getAll', function (dummy, fn) {
        console.log('turnout:getAll');
        turnoutController.getAllTurnouts(socket, fn);
    });

    socket.on('turnout:getById', function (turnoutId, fn) {
        console.log('turnout:getById');
        turnoutController.getTurnoutById(socket, turnoutId, fn);
    });

    socket.on('turnout:add', function (turnout, fn) {
        console.log('turnout:add');
        turnoutController.addTurnout(socket, turnout, fn);
    });

    socket.on('turnout:update', function (turnout, fn) {
        console.log('turnout:update');
        turnoutController.updateTurnout(socket, turnout, fn);
    });

    socket.on('turnout:remove', function (turnout, fn) {
        console.log('turnout:remove');
        turnoutController.removeTurnout(socket, turnout, fn);
    });

    socket.on('turnout:clear', function (dummy, fn) {
        console.log('turnout:clear');
        turnoutController.clear(socket, fn);
    });

    /* ROUTES */

    socket.on('routeGroup:getAll', function (dummy, fn) {
        console.log('route:getAll');
        routeController.getAllRouteGroups(socket, fn);
    });

    socket.on('routeGroup:getById', function (routeGroupId, fn) {
        console.log('routeGroup:getById');
        routeController.getRouteGroupById(socket, routeGroupId, fn);
    });

    socket.on('routeGroup:add', function (routeGroup, fn) {
        console.log('routeGroup:add');
        routeController.addRouteGroup(socket, routeGroup, fn);
    });

    socket.on('routeGroup:update', function (routeGroup, fn) {
        console.log('routeGroup:update');
        routeController.updateRouteGroup(socket, routeGroup, fn);
    });

    socket.on('routeGroup:remove', function (routeGroup, fn) {
        console.log('routeGroup:remove');
        routeController.removeRouteGroup(socket, routeGroup, fn);
    });

    socket.on('route:getById', function (routeId, fn) {
        console.log('route:getById');
        routeController.getRouteById(socket, routeId, fn);
    });

    socket.on('route:add', function (route, fn) {
        console.log('route:add');
        routeController.addRoute(socket, route, fn);
    });

    socket.on('route:update', function (route, fn) {
        console.log('route:update');
        routeController.updateRoute(socket, route, fn);
    });

    socket.on('route:remove', function (route, fn) {
        console.log('route:remove');
        routeController.removeRoute(socket, route, fn);
    });

    socket.on('route:clear', function (dummy, fn) {
        console.log('route:clear');
        routeController.clear(socket, fn);
    });

    /* LOCOMOTIVES */

    socket.on('locomotiveGroup:getAll', function (dummy, fn) {
        console.log('locomotiveGroup:getAll');
        locomotiveController.getAllLocomotiveGroups(fn);
    });

    socket.on('locomotiveGroup:getById', function (locomotiveId, fn) {
        console.log('locomotiveGroup:getById');
        locomotiveController.getLocomotiveGroupById(locomotiveId, fn);
    });

    socket.on('locomotiveGroup:add', function (locomotiveGroupName, fn) {
        console.log('locomotiveGroup:add');
        locomotiveController.addLocomotiveGroup(locomotiveGroupName, function (err, locomotiveGroup) {
            sendBroadcastToClients(err, 'locomotiveGroup:added', locomotiveGroup, fn);
        });
    });

    socket.on('locomotiveGroup:update', function (locomotiveGroup, fn) {
        console.log('locomotiveGroup:update');
        locomotiveController.updateLocomotiveGroup(locomotiveGroup, function (err, locomotiveGroup) {
            sendBroadcastToClients(err, 'locomotiveGroup:updated', locomotiveGroup, fn);
        });
    });

    socket.on('locomotiveGroup:remove', function (locomotiveGroupId, fn) {
        console.log('locomotiveGroup:remove');
        locomotiveController.removeLocomotiveGroup(locomotiveGroupId, function (err, locomotiveGroupId) {
            sendBroadcastToClients(err, 'locomotiveGroup:removed', locomotiveGroupId, fn);
        });
    });

    socket.on('locomotive:getById', function (locomotiveId, fn) {
        console.log('locomotive:getById');
        locomotiveController.getLocomotiveById(locomotiveId, fn);
    });

    socket.on('locomotive:add', function (locomotive, fn) {
        console.log('locomotive:add');
        locomotiveController.addLocomotive(locomotive, function (err, locomotive) {
            sendBroadcastToClients(err, 'locomotive:added', locomotive, fn);
        });
    });

    socket.on('locomotive:update', function (locomotive, fn) {
        console.log('locomotive:update');
        locomotiveController.updateLocomotive(locomotive, function (err, locomotive) {
            sendBroadcastToClients(err, 'locomotive:updated', locomotive, fn);
        });
    });

    socket.on('locomotive:remove', function (locomotiveId, fn) {
        console.log('locomotive:remove');
        locomotiveController.removeLocomotive(locomotiveId, function (err, locomotiveId) {
            sendBroadcastToClients(err, 'locomotive:removed', locomotiveId, fn);
        });
    });

    socket.on('locomotive:clear', function (dummy, fn) {
        console.log('locomotive:clear');
        locomotiveController.clear(function (err, locomotiveGroups) {
            sendBroadcastToClients(err, 'locomotive:init', locomotiveGroups, fn);

        });


        socket.broadcast.emit('locomotive:init', result);
    });
};
