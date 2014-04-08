'use strict';

function RoutesCtrl($scope, socket) {
    $scope.routes = {};
    $scope.routeGroups = {};
    socket.emit('routeGroup:getAll', '', function (err, routeGroups) {
        if (!err) {
            receivedNewRouteGroups(routeGroups, $scope);
        }
    });

    socket.on('route:init', function (routeGroups) {
        receivedNewRouteGroups(routeGroups, $scope);
    });

    socket.on('routeGroup:added', function (routeGroup) {
        addRouteGroup(routeGroup, $scope);
    });

    socket.on('routeGroup:updated', function (routeGroup) {
        updateRouteGroup(routeGroup, $scope);
    });

    socket.on('routeGroup:removed', function (routeGroup) {
        removeRouteGroup(routeGroup, $scope);
    });

    socket.on('route:added', function (route) {
        addRoute(route, $scope);
    });

    socket.on('route:updated', function (route) {
        updateRoute(route, $scope);
    });

    socket.on('route:removed', function (route) {
        removeRoute(route, $scope);
    });

    $scope.removeRoute = function (routeId) {
        $scope.error = null;

        socket.emit('route:remove', routeId, function (err, msg) {
            if (!err) {
                removeRoute(routeId, $scope);
            } else {
                $scope.error = 'Error removing route (' + msg + ')';
            }
        });
    }
    $scope.removeRouteGroup = function (routeGroupId) {
        $scope.error = null;
        socket.emit('routeGroup:remove', routeGroupId, function (err, msg) {
            if (!err) {
                removeRouteGroup(routeGroupId, $scope);
            } else {
                $scope.error = 'Error removing route group(' + msg + ')';
            }
        });
    }

    $scope.clearRoutes = function () {
        $scope.error = null;
        socket.emit('route:clear', '', function (err, msg) {
            if (!err) {
                receivedNewRouteGroups(msg, $scope);
            } else {
                $scope.error = 'Error clearing routes: ' + msg;
            }
        })
    }
}
RoutesCtrl.$inject = ['$scope', 'socket'];


function AddRouteGroupCtrl($scope, socket, $location, $routeParams) {
    $scope.routeGroup = {
    }
    $scope.addRouteGroup = function () {
        $scope.error = null;
        socket.emit('routeGroup:add', $scope.routeGroup, function (err, msg) {
            if (!err) {
                $location.path('/routes')
            } else {
                $scope.error = 'Error adding route group(' + msg + ')';
            }
        });
    }


}
AddRouteGroupCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function EditRouteGroupCtrl($scope, socket, $location, $routeParams) {
    socket.emit('routeGroup:getById', $routeParams.id, function (err, routeGroup) {
        if (!err && routeGroup != null) {
            $scope.routeGroup = routeGroup;
        }
    });
    $scope.editRouteGroup = function () {
        $scope.error = null;
        socket.emit('routeGroup:update', $scope.routeGroup, function (err, msg) {
            if (!err) {
                $location.path('/routes')
            } else {
                $scope.error = 'Error updating route group (' + msg + ')';
            }
        });
    }

}
EditRouteGroupCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function AddRouteCtrl($scope, socket, $location, $routeParams) {
    $scope.route = {
        groupId: $routeParams.groupId,
        routedTurnouts: []
    }

    getAllTurnouts(socket, $scope);

    $scope.addRoutedTurnout = function () {
        var rt = {number: $scope.newRoutedTurnout.number, state: $scope.newRoutedTurnout.state};
        rt._id = $scope.turnoutByNumber[$scope.newRoutedTurnout.number]._id;
        $scope.route.routedTurnouts.push(rt);
        $scope.newRoutedTurnout = null;
    }

    $scope.removeRoutedTurnout = function (routedTurnout) {
        var i = $scope.route.routedTurnouts.indexOf(routedTurnout);
        $scope.route.routedTurnouts.splice(i, 1);
    }

    $scope.addRoute = function () {
        $scope.error = null;
        populateTurnoutIds($scope);
        socket.emit('route:add', $scope.route, function (err, msg) {
            if (!err) {
                $location.path('/routes')
            } else {
                $scope.error = 'Error adding route (' + msg + ')';
            }
        });
    }

}
AddRouteCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function EditRouteCtrl($scope, socket, $location, $routeParams) {
    getAllTurnouts(socket, $scope, function () {
        socket.emit('route:getById', $routeParams.id, function (err, route) {
            if (!err) {
                $scope.route = route;
                angular.forEach($scope.route.routedTurnouts, function (routedTurnout) {
                    var turnout = $scope.turnoutById[routedTurnout.turnoutId];
                    if (turnout) {
                        var number = turnout.number;
                        routedTurnout.number = number;
                    }
                });
            }
        });
    });

    $scope.addRoutedTurnout = function () {
        var rt = {number: $scope.newRoutedTurnout.number, state: $scope.newRoutedTurnout.state};
        rt.id = $scope.turnoutByNumber[$scope.newRoutedTurnout.number].id;
        $scope.route.routedTurnouts.push(rt);
        $scope.newRoutedTurnout = null;
    }

    $scope.removeRoutedTurnout = function (routedTurnout) {
        var i = $scope.route.routedTurnouts.indexOf(routedTurnout);
        $scope.route.routedTurnouts.splice(i, 1);
    }


    $scope.editRoute = function () {
        $scope.error = null;
        populateTurnoutIds($scope);
        socket.emit('route:update', $scope.route, function (err, msg) {
            if (!err) {
                $location.path('/routes')
            } else {
                $scope.error = 'Error updating route (' + msg + ')';
            }
        });
    }
}
EditRouteCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];


/**************
 // Private helpers
 **************/
function receivedNewRouteGroups(routeGroups, $scope) {
    $scope.routes = {};
    $scope.routeGroups = {};
    angular.forEach(routeGroups, function (routeGroup) {
        var groupId = routeGroup.id;
        $scope.routeGroups[groupId] = routeGroup;
        angular.forEach(routeGroup.routes, function (route) {
            var routeId = route.id;
            if ($scope.routes === undefined) {
                $scope.routes = {};
            }
            $scope.routes[routeId] = route;
            $scope.routeGroups[groupId].routes[routeId] = route;
        });
    });
}

function addRouteGroup(routeGroup, $scope) {
    $scope.routeGroups[routeGroup.id] = routeGroup;
}

function addRoute(route, $scope) {
    $scope.routes[route.id] = route;
    var routes = $scope.routeGroups[route.groupId].routes;
    if (!routes) {
        routes = {};
    }
    routes[route.id] = route;
}

function updateRoute(route, $scope) {
    var routeId = route.id;
    var groupId = $scope.routes[routeId].groupId;
    $scope.routes[routeId] = route;
    $scope.routeGroups[groupId].routes[routeId] = route;
}

function removeRoute(routeId, $scope) {
    var groupId = $scope.routes[routeId].groupId;
    delete $scope.routes[routeId];
    delete $scope.routeGroups[groupId].routes[routeId];
}
function removeRouteGroup(routeGroupId, $scope) {
    delete $scope.routeGroups[routeGroupId]
}

function updateRouteGroup(routeGroup, $scope) {
    $scope.routeGroups[routeGroup.id].number = routeGroup.number;
    $scope.routeGroups[routeGroup.id].name = routeGroup.name;
    $scope.routeGroups[routeGroup.id].orientation = routeGroup.orientation;

}

function getAllTurnouts(socket, $scope, fn) {
    socket.emit('turnout:getAll', '', function (err, turnouts) {
        if (!err) {
            $scope.turnouts = turnouts;
            $scope.turnoutByNumber = {};
            $scope.turnoutById = {};
            angular.forEach(turnouts, function (t) {
                $scope.turnoutByNumber[t.number] = t;
                $scope.turnoutById[t.id] = t;
            });
        } else {
            $scope.error = 'Error getting all turnouts (' + msg + ')';
        }
        if (fn) {
            fn();
        }
    });

}
function populateTurnoutIds($scope) {
    angular.forEach($scope.route.routedTurnouts, function (routedTurnout) {
        routedTurnout.turnoutId = $scope.turnoutByNumber[routedTurnout.number].id;
    });
}
