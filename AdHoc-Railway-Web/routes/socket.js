/*
 * Serve content over a socket
 */
var turnoutController = require('./turnoutcontroller');

module.exports = function (socket) {

    turnoutController.init(socket);

    socket.on('turnout:add', function (turnout, fn) {
        console.log('turnout:add');
        turnoutController.addTurnout(socket, turnout, fn);
    });
 
    socket.on('turnout:update', function (turnout, fn) {
        console.log('turnout:update');
        turnoutController.updateTurnout(socket, turnout, fn);
    });

    socket.on('turnout:remove', function (turnoutId, fn) {
        console.log('turnout:remove');
        turnoutController.removeTurnout(socket, turnoutId, fn);
    });

    socket.on('turnouts:getAll', function (dummy, fn) {
        console.log('turnouts:getAll');
        turnoutController.getAll(socket, fn);
    });

    socket.on('turnout:getById', function (turnoutId, fn) {
        console.log('turnout:getById');
        turnoutController.getById(socket, turnoutId, fn);
    });


    socket.on('turnoutGroup:getAll', function (dummy, fn) {
        console.log('turnoutGroup:getAll');
        turnoutController.getAllTurnoutGroups(socket, fn);
    });

    socket.on('turnoutGroup:add', function (turnoutGroupName, fn) {
        console.log('turnoutGroup:add');
        turnoutController.addTurnoutGroup(socket, turnoutGroupName, fn);
    });

    socket.on('locomotives:getAll', function (dummy, fn) {
        //fn(data.locomotives);
    });

};
