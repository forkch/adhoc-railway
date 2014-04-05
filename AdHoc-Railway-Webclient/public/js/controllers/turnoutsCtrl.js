'use strict';

function TurnoutsCtrl($scope, socket) {
    $scope.turnouts = {};
    $scope.turnoutGroups = {};
    socket.emit('turnoutGroup:getAll', '', function (err, turnoutGroups) {
        if (!err) {
            receivedNewTurnoutGroups(turnoutGroups, $scope);
        }
    });

    socket.on('turnout:init', function (turnoutGroups) {
        receivedNewTurnoutGroups(turnoutGroups, $scope);
    });

    socket.on('turnoutGroup:added', function (turnoutGroup) {
        addTurnoutGroup(turnoutGroup, $scope);
    });

    socket.on('turnoutGroup:updated', function (turnoutGroup) {
        updateTurnoutGroup(turnoutGroup, $scope);
    });

    socket.on('turnoutGroup:removed', function (turnoutGroup) {
        removeTurnoutGroup(turnoutGroup, $scope);
    });

    socket.on('turnout:added', function (turnout) {
        addTurnout(turnout, $scope);
    });

    socket.on('turnout:updated', function (turnout) {
        updateTurnout(turnout, $scope);
    });

    socket.on('turnout:removed', function (turnout) {
        removeTurnout(turnout, $scope);
    });

    $scope.removeTurnout = function (turnoutId) {
        $scope.error = null;

        socket.emit('turnout:remove', turnoutId, function (err, msg) {
            if (!err) {
                removeTurnout(turnoutId, $scope);
            } else {
                $scope.error = 'Error removing turnout (' + msg + ')';
            }
        });
    }
    $scope.removeTurnoutGroup = function (turnoutGroupId) {
        $scope.error = null;
        socket.emit('turnoutGroup:remove', turnoutGroupId, function (err, msg) {
            if (!err) {
                removeTurnoutGroup(turnoutGroupId, $scope);
            } else {
                $scope.error = 'Error removing turnout group(' + msg + ')';
            }
        });
    }

    $scope.clearTurnouts = function () {
        $scope.error = null;
        socket.emit('turnout:clear', '', function (err, msg) {
            if (!err) {
                receivedNewTurnoutGroups(msg, $scope);
            } else {
                $scope.error = 'Error clearing turnouts: ' + msg;
            }
        })
    }

}
TurnoutsCtrl.$inject = ['$scope', 'socket'];

function AddTurnoutGroupCtrl($scope, socket, $location) {
    $scope.turnoutGroup = {
    }
    $scope.addTurnoutGroup = function () {
        $scope.error = null;
        socket.emit('turnoutGroup:add', $scope.turnoutGroup, function (err, msg) {
            if (!err) {
                $location.path('/turnouts')
            } else {
                $scope.error = 'Error adding turnout group(' + msg + ')';
            }
        });
    }

}
AddTurnoutGroupCtrl.$inject = ['$scope', 'socket', '$location'];

function EditTurnoutGroupCtrl($scope, socket, $location, $routeParams) {
    socket.emit('turnoutGroup:getById', $routeParams.id, function (err, turnoutGroup) {
        if (!err) {
            $scope.turnoutGroup = turnoutGroup;
        }
    });
    $scope.editTurnoutGroup = function () {
        $scope.error = null;
        socket.emit('turnoutGroup:update', $scope.turnoutGroup, function (err, msg) {
            if (!err) {
                $location.path('/turnouts')
            } else {
                $scope.error = 'Error updating turnout group (' + msg + ')';
            }
        });
    }
}
AddTurnoutCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function AddTurnoutCtrl($scope, socket, $location, $routeParams) {
    $scope.turnout = {
        address1switched: false,
        address2switched: false,
        bus1: "1",
        bus2: "1",
        type: "default",
        defaultState: "straight",
        orientation: "north",
        groupId: $routeParams.groupId
    }
    $scope.addTurnout = function () {
        $scope.error = null;
        socket.emit('turnout:add', $scope.turnout, function (err, msg) {
            if (!err) {
                $location.path('/turnouts')
            } else {
                $scope.error = 'Error adding turnout (' + msg + ')';
            }
        });
    }

}
AddTurnoutCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function EditTurnoutCtrl($scope, socket, $location, $routeParams) {
    socket.emit('turnout:getById', $routeParams.id, function (err, turnout) {
        if (!err) {
            $scope.turnout = turnout;
        }
    });
    $scope.editTurnout = function () {
        $scope.error = null;
        socket.emit('turnout:update', $scope.turnout, function (err, msg) {
            if (!err) {
                $location.path('/turnouts')
            } else {
                $scope.error = 'Error updating turnout (' + msg + ')';
            }
        });
    }
}
EditTurnoutCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

/**************
 // Private helpers
 **************/
function receivedNewTurnoutGroups(turnoutGroups, $scope) {
    $scope.turnouts = {};
    $scope.turnoutGroups = {};
    angular.forEach(turnoutGroups, function (turnoutGroup) {
        var groupId = turnoutGroup.id;
        $scope.turnoutGroups[groupId] = turnoutGroup;
        angular.forEach(turnoutGroup.turnouts, function (turnout) {
            var turnoutId = turnout.id;
            if ($scope.turnouts === undefined) {
                $scope.turnouts = {};
            }
            $scope.turnouts[turnoutId] = turnout;
            $scope.turnoutGroups[groupId].turnouts[turnoutId] = turnout;
        });
    });
}

function addTurnoutGroup(turnoutGroup, $scope) {
    $scope.turnoutGroups[turnoutGroup.id] = turnoutGroup;
}

function addTurnout(turnout, $scope) {
    $scope.turnouts[turnout.id] = turnout;
    var turnouts = $scope.turnoutGroups[turnout.groupId].turnouts;
    if (!turnouts) {
        turnouts = {};
    }
    turnouts[turnout.id] = turnout;
}

function updateTurnout(turnout, $scope) {
    var turnoutId = turnout.id;
    var groupId = $scope.turnouts[turnoutId].groupId;
    $scope.turnouts[turnout.id] = turnout;
    $scope.turnoutGroups[groupId].turnouts[turnout.id] = turnout;
}

function removeTurnout(turnoutId, $scope) {
    var groupId = $scope.turnouts[turnoutId].groupId;
    delete $scope.turnouts[turnoutId];
    delete $scope.turnoutGroups[groupId].turnouts[turnoutId];
}

function removeTurnoutGroup(turnoutGroup, $scope) {
    delete $scope.turnoutGroups[turnoutGroup.id];
}

function updateTurnoutGroup(turnoutGroup, $scope) {
    $scope.turnoutGroups[turnoutGroup.id] = turnoutGroup;
}
