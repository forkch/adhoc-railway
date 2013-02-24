var TurnoutGroupModel = require('../models/turnout_model').TurnoutGroupModel;
var TurnoutModel = require('../models/turnout_model').TurnoutModel;
var RouteGroupModel = require('../models/turnout_model').RouteGroupModel;
var RouteModel = require('../models/turnout_model').RouteModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

exports.init = function (socket) {
  getAllRouteData(function (err, result) {
    if (!err) {
      socket.emit('route:init', result);
    }
  });
}

exports.getAllRouteGroups = function (socket,fn) {
    
  getAllRouteData(function (err, result) {
    if (!err) {
      fn(false, result);
    }else {
      fn('failed to find all route groups');
    }
  });
}

exports.getRouteGroupById = function (socket, routeGroupId, fn) {
  console.log('routeGroup:getById: ' + routeGroupId);
  RouteGroupModel.findById(routeGroupId, function (err, routeGroup) {
    if(!err) {
      fn(routeGroup);
    }else{
      fn('failed to find route group with id ' + routeGroupId);
    }
  });
}
exports.addRouteGroup = function (socket, routeGroup, fn) {
  if(routeGroup.number <= 0) {
  	fn(true, 'number must be greater than 0');
  	return;
  }
  if(routeGroup.name == null || routeGroup.name.length == 0) {
    fn(true, 'name must be defined');
    return;
  }
  var group = new RouteGroupModel(routeGroup);
  group.save(function(err, addedRouteGroup) {
    if(!err) {
      var routeGroup = addedRouteGroup.toJSON();
      routeGroup.routes = {};
      socket.broadcast.emit('routeGroup:added', routeGroup);
      fn(false, 'success', group);
    } else {
       fn(true, 'failed to save route group');
    }
  });
}

exports.updateRouteGroup = function (socket, routeGroup, fn) {
  if(routeGroup.name == null || routeGroup.name.length == 0) {
    fn(true, 'name must be defined');
    return;
  }
  console.log('updating route group' + JSON.stringify(routeGroup));
  
  var id = routeGroup._id;
  delete routeGroup._id;

  RouteGroupModel.update({_id: id}, routeGroup, function (err,  numberAffected, rawResponse){
    if (!err) {
      routeGroup._id = id;
      socket.broadcast.emit('routeGroup:updated', routeGroup);
      fn(false, ''); 
    }else {
      fn(true, 'failed to update routes group');
    }
  });
}

exports.removeRouteGroup = function (socket, routeGroup, fn) {
  var id = routeGroup._id;
  console.log('remove route group ' + id);
  RouteModel.remove({"group": id}, function (err) {
    if (!err) {
      RouteGroupModel.remove({_id: id}, function (err) {
          if (!err) {
              socket.broadcast.emit('routeGroup:removed', routeGroup);
              fn(false, '');
          } else {
              fn(true, 'failed to remove routes group');
          }
      });
    } else {
      fn(true, 'failed to remove the routes associated to route group');
    }
  });
}

exports.getRouteById = function(socket, routeId, fn) {
    console.log('route:getById: ' + routeId);
    RouteModel.findById(routeId, function(err, route) {
        if(!err) {
            fn(route);
        }else{
            fn('failed to find route with id ' + routeId);
        }
    });
}

exports.addRoute = function(socket, route, fn) {
    if (!route.number || route.number < 1) {
        fn(true, 'number must be greater 0');
        return ;
    }
    if(!route.name || route.name.length < 1) {
        fn(true, 'name of the route must be specified');
        return;
    }

    if(!route.orientation || route.orientation.length < 1) {
        fn(true, 'orientation of the route must be specified');
        return;
    }
    
    console.log('adding new route ' + JSON.stringify(route));
    new RouteModel(route).save(function(err, addedRoute) {
        if(!err) {
            console.log(addedRoute.group);
            RouteGroupModel.findById({_id: addedRoute.group}, function(err, routeGroup) {
                console.log(routeGroup);
                routeGroup.routes.push(addedRoute._id);
                routeGroup.save();
            });
            socket.broadcast.emit('route:added', addedRoute);
            fn(false, 'success');
        }else{
            fn(true, 'failed to add route');
        }
    });
    
}

exports.updateRoute = function(socket, route, fn) {
    if (route.number <= 0) {
        fn(false, 'number must be greater 0');
        return;
    }
    console.log('updating route ' + JSON.stringify(route));
    
    var id = route._id;
    delete route._id;

    RouteModel.update({_id: id}, route, function(err,  numberAffected, rawResponse){
        if(!err) {
            route._id = id;
            socket.broadcast.emit('route:updated', route);
            fn(false, ''); 
        }else {
        fn(true, 'failed to update route');
        }
    });
}

exports.removeRoute = function(socket, route, fn) {
    var routeId = route._id;
    console.log('remvove route ' + route._id);
    RouteModel.remove({_id: routeId}, function(err) {
        if(!err) {
            RouteGroupModel.update(
                {}, {$pull: {routes: routeId}}, function(err, routeGroup) {
                if(!err) {
                    socket.broadcast.emit('route:removed', route);
                    fn(false, '');
                }else {
                    fn(true, 'failed to update route group');
                }
            });
        }else {
            fn(true, 'failed to remove route ');       
        }
    });
}


/* PRIVATE HELPERS */
getAllRouteData = function (fn) {

  RouteGroupModel.find().lean().exec(function (err, routeGroups) {
      
    if (err) {
      fn(true, 'failed getting route data');
      return;
    }
    RouteModel.find().lean().exec(function (err, routes) {
      if (err) {
         fn(true, 'failed getting route data');
         return
      }  
      TurnoutModel.find().lean().exec(function (err, turnouts) {


        var turnoutById = [];
        for(t in turnouts) {
            console.log(turnouts[t].group + " --> " + JSON.stringify(turnouts[t]));
            var turnoutId = turnouts[t]._id;

            turnoutById[turnoutId] = turnouts[t];
        }

        var routeByGroupId = [];
        for (r in routes) {
          var route = routes[r];

          console.log(route.group + " --> " + JSON.stringify(route));
          if(!routeByGroupId[route.group]) {
              routeByGroupId[route.group] = {};
          }
          var routeId = route._id;

          var turnoutsOfRoute = {};
          for (t in route.routedTurnouts) {
              var routedTurnout = route.routedTurnouts[t];
              var turnout = turnoutById[routedTurnout.turnoutId];
              routedTurnout.turnout = turnout;
          }

          routeByGroupId[route.group][routeId] = route;
        }
        
        var result = {'routeGroups': []};
        for (g in routeGroups) {
          var routeGroup = routeGroups[g];
          var groupId = routeGroup._id;
          routeGroup.routes = [];
          routeGroup.routes = routeByGroupId[groupId];
          result.routeGroups.push(routeGroups[g]);
        }

        fn(false, result);
        });
    });
  });
}
