/*
 * Serve content over a socket
 */
var turnoutController = require('./turnoutcontroller');

module.exports = function (socket) {

    turnoutController.init(socket);

    socket.on('turnout:add', function (turnout, fn) {
        turnoutController.addTurnout(socket, turnout, fn);
    });
 
    socket.on('turnout:update', function (turnout, fn) {
        turnoutController.updateTurnout(socket, turnout, fn);
    });

    socket.on('turnout:remove', function (turnoutId, fn) {
        turnoutController.removeTurnout(socket, turnoutId, fn);
    });

    socket.on('turnouts:getAll', function (dummy, fn) {
        turnoutController.getAll(socket, fn);
    });

    socket.on('turnout:getById', function (turnoutId, fn) {
        turnoutController.getById(socket, turnoutId, fn);
    });

    socket.on('locomotives:getAll', function (dummy, fn) {
        //fn(data.locomotives);
    });

};
