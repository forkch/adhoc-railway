'use strict';

function TurnoutsCtrl($scope, socket) {
    $scope.turnouts = {};
    socket.emit('turnoutGroup:getAll', '', function(err, data) {
        if(!err) {
            receivedNewTurnoutGroups(data.turnoutGroups, $scope);
        }
    });

    socket.on('turnoutGroup:added', function(turnoutGroup) {
        addTurnoutGroup(turnoutGroup, $scope);
    });

    socket.on('turnoutGroup:updated', function(turnoutGroup) {
        updateTurnoutGroup(turnoutGroup, $scope);
    });

    socket.on('turnoutGroup:removed', function(turnoutGroup) {
        removeTurnoutGroup(turnoutGroup._id, $scope);
    });

    socket.on('turnout:added', function(turnout) {
        addTurnout(turnout, $scope);
    });
    
    socket.on('turnout:updated', function(turnout) {
        updateTurnout(turnout, $scope);
    });

    socket.on('turnout:removed', function(turnout) {
        removeTurnout(turnout._id, $scope);
    });

    $scope.removeTurnout = function(turnoutId) {
        $scope.error = null;

        socket.emit('turnout:remove', $scope.turnouts[turnoutId], function(err, msg) {
            if(!err) {
                removeTurnout(turnoutId, $scope);
            } else {
                $scope.error = 'Error removing turnout (' + msg + ')';
            }
        });
    }
    $scope.removeTurnoutGroup = function(turnoutGroupId) {
        $scope.error = null;
        socket.emit('turnoutGroup:remove', $scope.turnoutGroups[turnoutGroupId], function(err, msg) {
            if(!err) {
                removeTurnoutGroup(turnoutGroupId, $scope);
            } else {
                $scope.error = 'Error removing turnout group(' + msg + ')';
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

function AddTurnoutGroupCtrl($scope, socket, $location,$routeParams) {
    $scope.turnoutGroup = {
    }
    $scope.addTurnoutGroup = function() {
        $scope.error = null;
        socket.emit('turnoutGroup:add', $scope.turnoutGroup, function(err, msg, turnoutGroup) {
            if(!err) {
                $location.path('/turnouts')
            } else {
                $scope.error = 'Error adding turnout group(' + msg + ')';
            }
        });
    }
    
}
AddTurnoutGroupCtrl.$inject = ['$scope', 'socket', '$location'];

function EditTurnoutGroupCtrl($scope, socket, $location, $routeParams) {
    socket.emit('turnoutGroup:getById', $routeParams.id, function(turnoutGroup) {
        if(turnoutGroup != null) {
            $scope.turnoutGroup = turnoutGroup;
        }
    });
    $scope.editTurnoutGroup = function() {
        $scope.error = null;
        socket.emit('turnoutGroup:update', $scope.turnoutGroup, function(err, msg) {
            if(!err) {
                $location.path('/turnouts')
            } else {
                $scope.error = 'Error updating turnout group (' + msg + ')';
            }
        });
    }
}
AddTurnoutCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function AddTurnoutCtrl($scope, socket, $location,$routeParams) {
    $scope.turnout = {
        address1switched: false,
        address2switched: false,
        bus1: "1",
        bus2: "1",
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

function addTurnoutGroup(turnoutGroup, $scope) {
    $scope.turnoutGroups[turnoutGroup._id] = turnoutGroup;
}

function addTurnout(turnout, $scope) {
    $scope.turnouts[turnout._id] = turnout;
    var turnouts = $scope.turnoutGroups[turnout.group].turnouts;
    if(!turnouts) {
        turnouts= {};
    }
    turnouts[turnout._id] = turnout;    
}

function updateTurnout(turnout, $scope) {
    var turnoutId = turnout._id;
    var groupId = $scope.turnouts[turnoutId].group;
    $scope.turnouts[turnout._id] = turnout;
    $scope.turnoutGroups[groupId].turnouts[turnout._id] = turnout;
}

function removeTurnout(turnoutId, $scope) {
    var groupId = $scope.turnouts[turnoutId].group;
    delete $scope.turnouts[turnoutId];
    delete $scope.turnoutGroups[groupId].turnouts[turnoutId];
}
function removeTurnoutGroup(turnoutGroupId, $scope) {
    delete $scope.turnoutGroups[turnoutGroupId]
}

function updateTurnoutGroup(turnoutGroup, $scope) {
    $scope.turnoutGroups[turnoutGroup._id] = turnoutGroup;
}