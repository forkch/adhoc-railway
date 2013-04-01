'use strict';

function LocomotivesCtrl($scope, socket) {
  $scope.locomotives = {};
  $scope.locomotiveGroups = {};
  socket.emit('locomotiveGroup:getAll', '', function(err, data) {
    if(!err) {
      receivedNewLocomotiveGroups(data.locomotiveGroups, $scope);
    }
  });

  socket.on('locomotive:init', function(data) {
    receivedNewLocomotiveGroups(data.locomotiveGroups, $scope);
  });

  socket.on('locomotiveGroup:added', function(locomotiveGroup) {
    addLocomotiveGroup(locomotiveGroup, $scope);
  });

  socket.on('locomotiveGroup:updated', function(locomotiveGroup) {
    updateLocomotiveGroup(locomotiveGroup, $scope);
  });

  socket.on('locomotiveGroup:removed', function(locomotiveGroup) {
    removeLocomotiveGroup(locomotiveGroup._id, $scope);
  });

  socket.on('locomotive:added', function(locomotive) {
    addLocomotive(locomotive, $scope);
  });

  socket.on('locomotive:updated', function(locomotive) {
    updateLocomotive(locomotive, $scope);
  });

  socket.on('locomotive:removed', function(locomotive) {
    removeLocomotive(locomotive._id, $scope);
  });

  $scope.removeLocomotive = function(locomotiveId) {
    $scope.error = null;

    socket.emit('locomotive:remove', $scope.locomotives[locomotiveId], function(err, msg) {
      if(!err) {
        removeLocomotive(locomotiveId, $scope);
      } else {
        $scope.error = 'Error removing locomotive (' + msg + ')';
      }
    });
  }
  $scope.removeLocomotiveGroup = function(locomotiveGroupId) {
    $scope.error = null;
    socket.emit('locomotiveGroup:remove', $scope.locomotiveGroups[locomotiveGroupId], function(err, msg) {
      if(!err) {
        removeLocomotiveGroup(locomotiveGroupId, $scope);
      } else {
        $scope.error = 'Error removing locomotive group(' + msg + ')';
      }
    });
  }

  $scope.addLocomotiveGroup = function() {
    socket.emit('locomotiveGroup:add', $scope.locomotiveGroupName, function(err, msg, locomotiveGroup) {
      if(!err) {
        $scope.locomotiveGroups[locomotiveGroup._id] = locomotiveGroup;
      }else {
        $scope.error = 'Error adding locomotiveGroup (' + msg + ')';
      }
    });
  }

  $scope.clearLocomotives = function() {
    $scope.error = null;
    socket.emit('locomotive:clear', '', function(err, msg, data) {
      if(!err) {
        receivedNewLocomotiveGroups(data.locomotiveGroups, $scope);
      } else {
        $scope.error = 'Error clearing locomotives: ' + msg;
      }
    })
  }

}
LocomotivesCtrl.$inject = ['$scope', 'socket'];

function AddLocomotiveGroupCtrl($scope, socket, $location,$routeParams) {
  $scope.locomotiveGroup = {
  }
  $scope.addLocomotiveGroup = function() {
    $scope.error = null;
    socket.emit('locomotiveGroup:add', $scope.locomotiveGroup, function(err, msg, locomotiveGroup) {
      if(!err) {
        $location.path('/locomotives')
      } else {
        $scope.error = 'Error adding locomotive group(' + msg + ')';
      }
    });
  }

}
AddLocomotiveGroupCtrl.$inject = ['$scope', 'socket', '$location'];

function EditLocomotiveGroupCtrl($scope, socket, $location, $routeParams) {
  socket.emit('locomotiveGroup:getById', $routeParams.id, function(locomotiveGroup) {
    if(locomotiveGroup != null) {
      $scope.locomotiveGroup = locomotiveGroup;
    }
  });
  $scope.editLocomotiveGroup = function() {
    $scope.error = null;
    socket.emit('locomotiveGroup:update', $scope.locomotiveGroup, function(err, msg) {
      if(!err) {
        $location.path('/locomotives')
      } else {
        $scope.error = 'Error updating locomotive group (' + msg + ')';
      }
    });
  }
}
EditLocomotiveGroupCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

function AddLocomotiveCtrl($scope, socket, $location,$routeParams) {
  $scope.locomotive = {
    group: $routeParams.groupId
  }
  $scope.addLocomotive = function() {
    $scope.error = null;
    socket.emit('locomotive:add', $scope.locomotive, function(err, msg, locomotive) {
      if(!err) {
        $location.path('/locomotives')
      } else {
        $scope.error = 'Error adding locomotive (' + msg + ')';
      }
    });
  }

}
AddLocomotiveCtrl.$inject = ['$scope', 'socket', '$location','$routeParams'];

function EditLocomotiveCtrl($scope, socket, $location, $routeParams) {
  socket.emit('locomotive:getById', $routeParams.id, function(locomotive) {
    if(locomotive != null) {
      $scope.locomotive = locomotive;
    }
  });
  $scope.editLocomotive = function() {
    $scope.error = null;
    socket.emit('locomotive:update', $scope.locomotive, function(err, msg) {
      if(!err) {
        $location.path('/locomotives')
      } else {
        $scope.error = 'Error updating locomotive (' + msg + ')';
      }
    });
  }
}
EditLocomotiveCtrl.$inject = ['$scope', 'socket', '$location', '$routeParams'];

/**************
// Private helpers
 **************/


function receivedNewLocomotiveGroups(locomotiveGroups, $scope) {
  $scope.locomotives = {};
  $scope.locomotiveGroups = {};
  angular.forEach(locomotiveGroups, function(locomotiveGroup, key) {
    $scope.locomotiveGroups[locomotiveGroup._id] = locomotiveGroup;
    angular.forEach(locomotiveGroup.locomotives, function(locomotive, id) {
      if($scope.locomotives === undefined) {
        $scope.locomotives = {};
      }
      $scope.locomotives[id] = locomotive;
    });
  });
}

function addLocomotiveGroup(locomotiveGroup, $scope) {
  $scope.locomotiveGroups[locomotiveGroup._id] = locomotiveGroup;
}

function addLocomotive(locomotive, $scope) {
  $scope.locomotives[locomotive._id] = locomotive;
  var locomotives = $scope.locomotiveGroups[locomotive.group].locomotives;
  if(!locomotives) {
    locomotives= {};
  }
  locomotives[locomotive._id] = locomotive;    
}

function updateLocomotive(locomotive, $scope) {
  var locomotiveId = locomotive._id;
  var groupId = $scope.locomotives[locomotiveId].group;
  $scope.locomotives[locomotive._id] = locomotive;
  $scope.locomotiveGroups[groupId].locomotives[locomotive._id] = locomotive;
}

function removeLocomotive(locomotiveId, $scope) {
  var groupId = $scope.locomotives[locomotiveId].group;
  delete $scope.locomotives[locomotiveId];
  delete $scope.locomotiveGroups[groupId].locomotives[locomotiveId];
}
function removeLocomotiveGroup(locomotiveGroupId, $scope) {
  delete $scope.locomotiveGroups[locomotiveGroupId]
}

function updateLocomotiveGroup(locomotiveGroup, $scope) {
  $scope.locomotiveGroups[locomotiveGroup._id] = locomotiveGroup;
}
