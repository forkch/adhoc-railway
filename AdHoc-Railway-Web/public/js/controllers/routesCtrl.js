'use strict';

function RoutesCtrl($scope, socket) {
  $scope.routes = {};
  $scope.routeGroups = {};
  socket.emit('routeGroup:getAll', '', function(err, data) {
    if(!err) {
      receivedNewRouteGroups(data.routeGroups, $scope);
    }
  });

  socket.on('routeGroup:added', function(routeGroup) {
    addRouteGroup(routeGroup, $scope);
  });

  socket.on('routeGroup:updated', function(routeGroup) {
    updateRouteGroup(routeGroup, $scope);
  });

  socket.on('routeGroup:removed', function(routeGroup) {
    removeRouteGroup(routeGroup._id, $scope);
  });

  socket.on('route:added', function(route) {
    addRoute(route, $scope);
  });

  socket.on('route:updated', function(route) {
    updateRoute(route, $scope);
  });

  socket.on('route:removed', function(route) {
    removeRoute(route._id, $scope);
  });

  $scope.removeRoute = function(routeId) {
    $scope.error = null;

    socket.emit('route:remove', $scope.routes[routeId], function(err, msg) {
      if(!err) {
        removeRoute(routeId, $scope);
      } else {
        $scope.error = 'Error removing route (' + msg + ')';
      }
    });
  }
  $scope.removeRouteGroup = function(routeGroupId) {
    $scope.error = null;
    socket.emit('routeGroup:remove', $scope.routeGroups[routeGroupId], function(err, msg) {
      if(!err) {
        removeRouteGroup(routeGroupId, $scope);
      } else {
        $scope.error = 'Error removing route group(' + msg + ')';
      }
    });
  }

}
RoutesCtrl.$inject = ['$scope', 'socket'];


function AddRouteGroupCtrl($scope,socket, $location, $routeParams) {
  $scope.routeGroup = {
  }
  $scope.addRouteGroup = function() {
    $scope.error = null;
    socket.emit('routeGroup:add', $scope.routeGroup, function(err, msg, routeGroup) {
      if(!err) {
        $location.path('/routes')
      } else {
        $scope.error = 'Error adding route group(' + msg + ')';
      }
    });
  }


}
AddRouteGroupCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function EditRouteGroupCtrl($scope, socket, $location, $routeParams) {
  socket.emit('routeGroup:getById', $routeParams.id, function(routeGroup) {
    if(routeGroup != null) {
      $scope.routeGroup = routeGroup;
    }
  });
  $scope.editRouteGroup = function() {
    $scope.error = null;
    socket.emit('routeGroup:update', $scope.routeGroup, function(err, msg) {
      if(!err) {
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
    group: $routeParams.groupId,
    routedTurnouts: []
  }
  $scope.addRoutedTurnout = function() {
    var rt = {number: $scope.newRoutedTurnout.number, state: $scope.newRoutedTurnout.state};
    $scope.route.routedTurnouts.push(rt);
    $scope.newRoutedTurnout = null;
  }

  $scope.addRoute = function() {
    $scope.error = null;
    socket.emit('route:add', $scope.route, function(err, msg, route) {
      if(!err) {
        $location.path('/routes')
      } else {
        $scope.error = 'Error adding route (' + msg + ')';
      }
    });
  }

}
AddRouteCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function EditRouteCtrl($scope, socket, $location, $routeParams) {
  socket.emit('route:getById', $routeParams.id, function(route) {
    if(route != null) {
      $scope.route = route;
    }
  });
  $scope.editRoute = function() {
    $scope.error = null;
    socket.emit('route:update', $scope.route, function(err, msg) {
      if(!err) {
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
  angular.forEach(routeGroups, function(routeGroup, key) {
    $scope.routeGroups[routeGroup._id] = routeGroup;
    angular.forEach(routeGroup.routes, function(route, id) {
      if($scope.routes === undefined) {
        $scope.routes = {};
      }
      $scope.routes[id] = route;
    });
  });
}

function addRouteGroup(routeGroup, $scope) {
  $scope.routeGroups[routeGroup._id] = routeGroup;
}

function addRoute(route, $scope) {
  $scope.routes[route._id] = route;
  var routes = $scope.routeGroups[route.group].routes;
  if(!routes) {
    routes= {};
  }
  routes[route._id] = route;
}

function updateRoute(route, $scope) {
  var routeId = route._id;
  var groupId = $scope.routes[routeId].group;
  $scope.routes[routeId] = route;
  $scope.routeGroups[groupId].routes[routeId] = route;
}

function removeRoute(routeId, $scope) {
  var groupId = $scope.routes[routeId].group;
  delete $scope.routes[routeId];
  delete $scope.routeGroups[groupId].routes[routeId];
}
function removeRouteGroup(routeGroupId, $scope) {
  delete $scope.routeGroups[routeGroupId]
}

function updateRouteGroup(routeGroup, $scope) {
  $scope.routeGroups[routeGroup._id].number = routeGroup.number;
  $scope.routeGroups[routeGroup._id].name = routeGroup.name;
  $scope.routeGroups[routeGroup._id].orientation = routeGroup.orientation;

}
