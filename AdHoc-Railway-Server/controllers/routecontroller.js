var TurnoutGroupModel = require('../models/turnout_model').TurnoutGroupModel;
var TurnoutModel = require('../models/turnout_model').TurnoutModel;
var RouteGroupModel = require('../models/turnout_model').RouteGroupModel;
var RouteModel = require('../models/turnout_model').RouteModel;

var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

exports.init = function (fn) {
    getAllRouteData(function (err, result) {
        if (!err) {
            fn(err, result);
        }
    });
}

exports.getAllRouteGroups = function (fn) {

    getAllRouteData(function (err, result) {
        if (!err) {
            fn(err, result);
        } else {
            fn(err, {msg: 'failed to find all route groups'});
        }
    });
}

exports.getRouteGroupById = function (routeGroupId, fn) {
    console.log('routeGroup:getById: ' + routeGroupId);
    RouteGroupModel.findById(routeGroupId, function (err, routeGroup) {
        if (!err) {
            fn(err, routeGroup.toJSON());
        } else {
            fn(err, {msg: 'failed to find route group with id ' + routeGroupId});
        }
    });
}
exports.addRouteGroup = function (routeGroup, fn) {
    if (routeGroup.number <= 0) {
        fn(true, {msg: 'number must be greater than 0'});
        return;
    }
    if (routeGroup.name == null || routeGroup.name.length == 0) {
        fn(true, {msg: 'name must be defined'});
        return;
    }
    var group = new RouteGroupModel(routeGroup);
    group.save(function (err, addedRouteGroup) {
        if (!err) {
            addedRouteGroup.routes = [];
            fn(err, addedRouteGroup.toJSON());
        } else {
            fn(err, {msg: 'failed to save route group'});
        }
    });
}

exports.updateRouteGroup = function (routeGroup, fn) {
    if (routeGroup.name == null || routeGroup.name.length == 0) {
        fn(true, {msg: 'name must be defined'});
        return;
    }
    console.log('updating route group' + JSON.stringify(routeGroup));

    var id = routeGroup._id;
    delete routeGroup._id;

    RouteGroupModel.update({_id: id}, routeGroup, function (err) {
        if (!err) {
            fn(err, routeGroup);
        } else {
            fn(err, {msg: 'failed to update routes group'});
        }
    });
}

exports.removeRouteGroup = function (routeGroupId, fn) {
    console.log('remove route group ' + routeGroupId);
    RouteModel.remove({"group": routeGroupId}, function (err) {
            if (!err) {
                RouteGroupModel.findById(routeGroupId, function (err, routeGroup) {
                    if (!err) {
                        routeGroup.remove(function (err) {
                            if (!err) {
                                fn(err, routeGroup.toJSON());
                            } else {
                                fn(err, {msg: 'failed to remove routes group'});
                            }
                        });
                    } else {
                        fn(err, {msg: 'failed to remove routes group'});
                    }
                });
            } else {
                fn(err, {msg: 'failed to remove the routes associated to route group'});
            }
        }
    )
    ;
}

exports.getRouteById = function (routeId, fn) {
    console.log('route:getById: ' + routeId);
    RouteModel.findById(routeId, function (err, route) {
        if (!err) {
            fn(err, route);
        } else {
            fn(err, {msg: 'failed to find route with id ' + routeId});
        }
    });
}

exports.addRoute = function (route, fn) {
    if (!route.number || route.number < 1) {
        fn(true, {msg: 'number must be greater 0'});
        return;
    }
    if (!route.name || route.name.length < 1) {
        fn(true, {msg: 'name of the route must be specified'});
        return;
    }

    if (!route.orientation || route.orientation.length < 1) {
        fn(true, {msg: 'orientation of the route must be specified'});
        return;
    }

    console.log('adding new route ' + JSON.stringify(route));
    new RouteModel(route).save(function (err, addedRoute) {
        if (!err) {
            console.log(addedRoute.group);
            RouteGroupModel.findById({_id: addedRoute.groupId}, function (err, routeGroup) {
                if (!err) {
                    console.log(routeGroup);
                    routeGroup.routes.push(addedRoute.id);
                    routeGroup.save();
                    fn(err, addedRoute.toJSON());
                } else {
                    fn(err, {msg: 'failed to add route'});
                }
            });
        } else {
            fn(err, {msg: 'failed to add route'});
        }
    });

}

exports.updateRoute = function (route, fn) {
    if (route.number <= 0) {
        fn(true, {msg: 'number must be greater 0'});
        return;
    }
    console.log('updating route ' + JSON.stringify(route));

    var id = route.id;
    delete route.id;

    RouteModel.update({_id: id}, route, function (err) {
        if (!err) {
            route.id = id;
            fn(err, route);
        } else {
            fn(err, {msg: 'failed to update route'});
        }
    });
}

exports.removeRoute = function (routeId, fn) {
    console.log('remvove route ' + routeId);
    RouteModel.findById(routeId, function (err, route) {
        if (!err) {
            if (route) {
                route.remove(function (err) {
                    if (!err) {
                        RouteGroupModel.update(
                            {}, {$pull: {routes: routeId}}, function (err) {
                                if (!err) {
                                    fn(err, route.toJSON());
                                } else {
                                    fn(err, {msg: 'failed to update route group'});
                                }
                            });
                    } else {
                        fn(err, {msg: 'route not found'});
                    }
                });
            } else {
                fn(err, {msg: 'route not found'});
            }
        } else {
            fn(err, {msg: 'failed to remove route '});
        }
    });
}

exports.clear = function (socket, fn) {
    RouteModel.remove(function (err) {
        if (!err) {
            RouteGroupModel.remove(function (err) {
                if (!err) {
                    getAllRouteData(function (err, result) {
                        if (!err) {
                            fn(err, result);
                        }
                    });
                } else {
                    fn(err, {msg: 'failed to clear route groups'});
                }
            });
        } else {
            fn(err, {msg: 'failed to clear routes'});
        }
    });
}

/* PRIVATE HELPERS */
getAllRouteData = function (fn) {

    RouteGroupModel.find().exec(function (err, routeGroups) {

        if (err) {
            fn(true, 'failed getting route data');
            return;
        }
        RouteModel.find().exec(function (err, routes) {
            if (err) {
                fn(true, 'failed getting route data');
                return
            }
            TurnoutModel.find().exec(function (err, turnouts) {


                var turnoutById = [];
                for (t in turnouts) {
                    var turnoutId = turnouts[t].id;

                    turnoutById[turnoutId] = turnouts[t];
                }

                var routeByGroupId = {};
                for (r in routes) {
                    var route = routes[r];

                    if (!routeByGroupId[route.groupId]) {
                        routeByGroupId[route.groupId] = [];
                    }

                    if (route.routedTurnouts.length > 0) {
                        for (t in route.routedTurnouts) {
                            var routedTurnout = route.routedTurnouts[t];
                            if (routedTurnout.turnoutId) {
                                var turnout = turnoutById[routedTurnout.turnoutId];
                                routedTurnout.turnout = turnout.toJSON();
                            }
                        }
                    }

                    var groupId = route.groupId;
                    routeByGroupId[groupId].push(route.toJSON());
                }

                var result = [];
                for (g in routeGroups) {
                    var routeGroup = routeGroups[g].toJSON();
                    var groupId = routeGroup.id;
                    var routesOfAGroup = routeByGroupId[groupId];
                    routeGroup['routes'] = routesOfAGroup;
                    result.push(routeGroup);
                }

                fn(err, result);
            });
        });
    });
}
