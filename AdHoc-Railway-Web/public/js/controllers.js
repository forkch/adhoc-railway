'use strict';

/* Controllers */

function AppCtrl($scope, socket) {
  socket.on('init', function (data) {
      $scope.turnouts = data.turnouts;
      $scope.locomotives = {};
  });
}

function TurnoutsCtrl($scope, socket) {
    $scope.turnouts = {};
    socket.emit('turnouts:getAll', '', function(turnouts) {
        console.log(turnouts); 
        $scope.turnouts = turnouts;
    });
    socket.on('turnout:added', function(turnout) {
        $scope.turnouts[turnout.id] = turnout;
    });
    socket.on('turnout:updated', function(turnout) {
        $scope.turnouts[turnout.id] = turnout;
    });

    $scope.deleteTurnout = function(turnoutId) {
        $scope.error = null;
        socket.emit('turnout:delete', turnoutId, function(result, msg) {
            if(!result) {
                $scope.error = 'Error deleting turnout (' + msg + ')';
            } else {
                $location.path('/turnouts')
            }
        });
    }
    
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

function EditTurnoutCtrl($scope, socket, $location, $routeParams) {
    socket.emit('turnout:getById', $routeParams.id, function(turnout) {
        if(turnout != null) {
            $scope.turnout = turnout;
        }
    });
    $scope.editTurnout = function() {
        $scope.error = null;
        socket.emit('turnout:update', $scope.turnout, function(result, msg) {
            if(!result) {
                $scope.error = 'Error updating turnout (' + msg + ')';
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
