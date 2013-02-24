/*
 * Serve content over a socket
 */
var turnoutController = require('./turnoutcontroller');
var routeController = require('./routecontroller');

module.exports = function (socket) {

    turnoutController.init(socket);
    routeController.init(socket);

    socket.on('turnoutGroup:getAll', function (dummy, fn) {
        console.log('turnoutGroup:getAll');
        turnoutController.getAllTurnoutGroups(socket, fn);
    });

    socket.on('turnoutGroup:getById', function (turnoutId, fn) {
        console.log('turnoutGroup:getById');
        turnoutController.getTurnoutGroupById(socket,turnoutId, fn);
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

    socket.on('turnout:getById', function (turnoutId, fn) {
        console.log('turnout:getById');
        turnoutController.getById(socket, turnoutId, fn);
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

    /* ROUTES */

    socket.on('routeGroup:getAll', function(dummy, fn) {
        console.log('route:getAll');
        routeController.getAllRouteGroups(socket, fn);
    });
    
    socket.on('routeGroup:getById', function(routeGroupId, fn) {
        console.log('routeGroup:getById');
        routeController.getRouteGroupById(socket, routeGroupId, fn);
    });

    socket.on('routeGroup:add', function(routeGroup, fn) {
        console.log('routeGroup:add');
        routeController.addRouteGroup(socket, routeGroup, fn);
    });

    socket.on('routeGroup:update', function(routeGroup, fn) {
        console.log('routeGroup:update');
        routeController.updateRouteGroup(socket, routeGroup, fn);
    });

    socket.on('routeGroup:remove', function(routeGroup, fn) {
        console.log('routeGroup:remove');
        routeController.removeRouteGroup(socket, routeGroup, fn);
    });

    socket.on('route:getById', function(routeId, fn) {
        console.log('route:getById');
        routeController.getRouteById(socket, routeId, fn);
    });

    socket.on('route:add', function(route, fn) {
        console.log('route:add');
        routeController.addRoute(socket, route, fn);
    });

    socket.on('route:update', function(route, fn) {
        console.log('route:update');
        routeController.updateRoute(socket, route, fn);
    });

    socket.on('route:remove', function(route, fn) {
        console.log('route:remove');
        routeController.removeRoute(socket, route, fn);
    });

    socket.on('locomotive:getAll', function (dummy, fn) {
        console.log('locomotive:getAll');
    });

    socket.on('locomotive:add', function (locomotive, fn) {
        console.log('locomotive:add');
    });

    socket.on('locomotive:update', function (locomotive, fn) {
        console.log('locomotive:update');
    });
    
    socket.on('locomotive:remove', function (locomotive, fn) {
        console.log('locomotive:remove');
    });

};
