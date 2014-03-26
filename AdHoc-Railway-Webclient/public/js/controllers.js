'use strict';

/* Controllers */

function AppCtrl($scope, socket) {

    $scope.test = 'hello world';
    socket.on('turnout:init', function (data) {
        $scope.turnoutGroups = {};
        receivedNewTurnoutGroups(data.turnoutGroups, $scope);
        $scope.locomotives = {};
    });
}


function LocomotivesCtrl($scope, socket, $location) {
    socket.emit('locomotives:getAll', '', function (locomotives) {
        $scope.locomotives = locomotives;
    });
    socket.on('locomotive:add', function (locomotive) {
        $scope.locomotives.push(locomotive.name);
    });
}
LocomotivesCtrl.$inject = ['$scope', 'socket', '$location'];


