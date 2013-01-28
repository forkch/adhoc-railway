'use strict';

/* Controllers */

function AppCtrl($scope, socket) {
  socket.on('init', function (data) {
      $scope.turnouts = data.turnouts;
      $scope.locomotives = data.locomotives;
  });
}

function TurnoutsCtrl($scope, socket) {
    socket.emit('turnouts:getAll', '', function(turnouts) {
       $scope.turnouts = turnouts;
    });
    socket.on('turnout:added', function(turnout) {
        $scope.turnouts.push(turnout);
    });
}
TurnoutsCtrl.$inject = ['$scope', 'socket'];

function AddTurnoutCtrl($scope, socket,$location) {
    $scope.turnout = {
        address1switched: false,
        address2switched: false,
        type : "default",
        defaultState: "straight",
        orientation: "north"
    }
    $scope.addTurnout = function() {
        $scope.error = null;
        socket.emit('turnout:add', $scope.turnout, function(result, msg) {
            if(!result) {
                $scope.error = 'Error adding turnout (' + msg + ')';
            } else {
                $location.path('/turnouts')
            }
        });
    }
}
AddTurnoutCtrl.$inject = ['$scope', 'socket', '$location'];

function EditTurnoutCtrl($scope, socket,$location, $routeParams) {
    $socket.emit('turnout:getFromId', $routeParams.id, function(turnout) {

    });
    $scope.addTurnout = function() {
        $scope.error = null;
        socket.emit('turnout:add', $scope.turnout, function(result, msg) {
            if(!result) {
                $scope.error = 'Error adding turnout (' + msg + ')';
            } else {
                $location.path('/turnouts')
            }
        });
    }
}
AddTurnoutCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];


function LocomotivesCtrl($scope, socket,$location) {
    socket.emit('locomotives:getAll', '', function(locomotives) {
        $scope.locomotives = locomotives;
    });
    socket.on('locomotive:add', function(locomotive) {
        $scope.locomotives.push(locomotive.name);
    });
}
LocomotivesCtrl.$inject = ['$scope', 'socket', '$location'];
