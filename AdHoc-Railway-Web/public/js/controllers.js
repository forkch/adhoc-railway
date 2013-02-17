'use strict';

/* Controllers */

function AppCtrl($scope, socket) {
  socket.on('init', function (data) {
    $scope.turnoutGroups = {};
    receivedNewTurnoutGroups(data.turnoutGroups,$scope);
    $scope.locomotives = {};
  });
}


function TurnoutsCtrl($scope, socket) {
    $scope.turnouts = {};
    socket.emit('turnoutGroup:getAll', '', function(err, data) {
        if(!err) {
            receivedNewTurnoutGroups(data.turnoutGroups, $scope);
        }
    });

    socket.on('turnout:added', function(turnout) {
        $scope.turnouts[turnout._id] = turnout;
        var turnouts = $scope.turnoutGroups[turnout.group].turnouts;
        if(!turnouts) {
            turnouts= {};
        }
        turnouts[turnout._id] = turnout;
    });

    socket.on('turnoutGroup:added', function(turnoutGroup) {
        $scope.turnoutGroups[turnoutGroup._id] = turnoutGroup;
    });
    
    socket.on('turnout:updated', function(turnout) {
        console.log(turnout._id);
        $scope.turnouts[turnout._id] = turnout;
    });

    socket.on('turnout:removed', function(turnoutId) {
        removeTurnout(turnoutId, $scope);
        

    });

    $scope.deleteTurnout = function(turnoutId) {
        $scope.error = null;
        socket.emit('turnout:remove', turnoutId, function(err, msg) {
            if(!err) {
                removeTurnout(turnoutId, $scope);
            } else {
                $scope.error = 'Error removing turnout (' + msg + ')';
            }
        });
    }

    $scope.addTurnoutGroup = function() {
        socket.emit('turnoutGroup:add', $scope.turnoutGroupName, function(err, msg, turnoutGroup) {
            if(!err) {
                $scope.turnoutGroups[turnoutGroup._id] = turnoutGroup;
            }else {
                $scope.error = 'Error adding turnoutgroup (' + msg + ')';
            }
        });
    }
    
}
TurnoutsCtrl.$inject = ['$scope', 'socket'];



function AddTurnoutCtrl($scope, socket, $location,$routeParams) {
    $scope.turnout = {
        address1switched: false,
        address2switched: false,
        type : "default",
        defaultState: "straight",
        orientation: "north",
        group: $routeParams.groupId
    }
    $scope.addTurnout = function() {
        $scope.error = null;
        socket.emit('turnout:add', $scope.turnout, function(err, msg, turnout) {
            if(!err) {
                $location.path('/turnouts')
            } else {
                $scope.error = 'Error adding turnout (' + msg + ')';
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
        socket.emit('turnout:update', $scope.turnout, function(err, msg) {
            if(!err) {
                $location.path('/turnouts')
            } else {
                $scope.error = 'Error updating turnout (' + msg + ')';
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



/**************
// Private helpers
**************/


function receivedNewTurnoutGroups(turnoutGroups, $scope) {
    angular.forEach(turnoutGroups, function(turnoutGroup, key) {
        $scope.turnoutGroups[turnoutGroup._id] = turnoutGroup;
        angular.forEach(turnoutGroup.turnouts, function(turnout, id) {
            if($scope.turnouts === undefined) {
                $scope.turnouts = {};
            }
            $scope.turnouts[id] = turnout;
        });
    });
}

function removeTurnout(turnoutId, $scope) {
    var groupId = $scope.turnouts[turnoutId].group;
    delete $scope.turnouts[turnoutId];
    delete $scope.turnoutGroups[groupId].turnouts[turnoutId];
}