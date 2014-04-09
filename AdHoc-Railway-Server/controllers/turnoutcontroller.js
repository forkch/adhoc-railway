TurnoutGroupModel = require('../models/turnout_model').TurnoutGroupModel;
TurnoutModel = require('../models/turnout_model').TurnoutModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

var colorize = require('colorize');
var cconsole = colorize.console;

exports.init = function (fn) {
    getAllTurnoutData(function (err, result) {
        if (!err) {
            fn(err, result);
        }
    });
}

exports.getAllTurnoutGroups = function (fn) {

    getAllTurnoutData(function (err, result) {
        if (!err) {
            fn(err, result);
        } else {
            fn(err, {msg: 'failed to find all turnout groups'});
        }
    });
}

exports.getTurnoutGroupById = function (turnoutGroupId, fn) {
    console.log('turnoutGroup:getById: ' + turnoutGroupId);
    TurnoutGroupModel.findById(turnoutGroupId, function (err, turnoutGroup) {
        if (!err) {
            fn(err, turnoutGroup.toJSON());
        } else {
            fn(err, {msg: 'failed to find turnout group with id ' + turnoutGroupId});
        }
    });
}

exports.addTurnoutGroup = function (turnoutGroup, fn) {
    if (turnoutGroup.name == null || turnoutGroup.name.length == 0) {
        fn(true, {msg: 'name must be defined'});
        return;
    }
    var group = new TurnoutGroupModel(turnoutGroup);
    group.save(function (err, addedTurnoutGroup) {
        if (!err) {
            addedTurnoutGroup.turnouts = [];
            fn(err, addedTurnoutGroup.toJSON());
        } else {
            fn(err, {msg: 'failed to save turnout group'});
        }
    });
}

exports.updateTurnoutGroup = function (turnoutGroup, fn) {
    if (turnoutGroup.name == null || turnoutGroup.name.length == 0) {
        fn(true, {msg: 'name must be defined'});
        return;
    }
    console.log('updating turnout group ' + JSON.stringify(turnoutGroup));

    TurnoutGroupModel.update({_id: turnoutGroup.id}, turnoutGroup, function (err) {
        if (!err) {
            fn(err, turnoutGroup);
        } else {
            fn(err, {msg: 'failed to update turnout group'});
        }
    });
}

exports.removeTurnoutGroup = function (turnoutGroupId, fn) {
    console.log('remove turnout group ' + turnoutGroupId);
    TurnoutModel.remove({"groupId": turnoutGroupId}, function (err) {
        if (!err) {
            TurnoutGroupModel.findById(turnoutGroupId, function (err, turnoutGroup) {
                if (!err && turnoutGroup) {
                    turnoutGroup.remove(function (err) {
                        if (!err) {
                            fn(err, turnoutGroup.toJSON());
                        } else {
                            fn(err, {msg: 'failed to remove turnout group'});
                        }
                    });
                } else {
                    fn(err, {msg: 'failed to remove turnout group'});
                }
            });
        } else {
            fn(err, {msg: 'failed to remove the turnouts associated to turnout group'});
        }
    });
}

exports.getAllTurnouts = function (fn) {

    TurnoutModel.find(function (err, turnouts) {
        if (!err) {
            fn(false, turnouts);
        } else {
            fn(true, {msg: 'failed to find all turnouts'});
        }
    });
}

exports.getTurnoutById = function (turnoutId, fn) {
    console.log('turnout:getById: ' + turnoutId);
    TurnoutModel.findById(turnoutId, function (err, turnout) {
        if (!err) {
            fn(err, turnout);
        } else {
            fn(err, {msg: 'failed to find turnout with id ' + turnoutId});
        }
    });
}

exports.addTurnout = function (turnout, fn) {

    if (!validateTurnout(turnout, fn)) {
        return;
    }

    console.log('adding new turnout ' + JSON.stringify(turnout));
    new TurnoutModel(turnout).save(function (err, addedTurnout) {
        if (!err) {
            console.log(addedTurnout);
            TurnoutGroupModel.findById({_id: addedTurnout.groupId}, function (err, turnoutGroup) {
                if (!err) {
                    console.log(turnoutGroup);
                    turnoutGroup.turnouts.push(addedTurnout.id);
                    turnoutGroup.save();
                    fn(err, addedTurnout.toJSON());
                } else {
                    fn(err, {msg: 'failed to add turnout'});
                }
            });
        } else {
            fn(err, {msg: 'failed to add turnout'});
        }
    });

}

exports.updateTurnout = function (turnout, fn) {
    if (!validateTurnout(turnout, fn)) {
        return;
    }

    console.log('updating turnout ' + JSON.stringify(turnout));

    var id = turnout.id;
    delete turnout.id;

    TurnoutModel.update({_id: id}, turnout, function (err, numberAffected, rawResponse) {
        if (!err) {
            turnout.id = id;
            fn(err, turnout);
        } else {
            fn(err, {msg: 'failed to update turnout'});
        }
    });
}

exports.removeTurnout = function (turnoutId, fn) {
    console.log('remvove turnout ' + turnoutId);
    TurnoutModel.findById(turnoutId, function (err, turnout) {
        if (!err) {
            if (turnout) {
                turnout.remove(function (err) {
                    TurnoutGroupModel.update({}, {$pull: {turnouts: turnoutId}}, function (err) {
                        if (!err) {
                            fn(err, turnout.toJSON());
                        } else {
                            fn(err, {msg: 'failed to remove turnout'});
                        }
                    });
                });
            } else {
                fn(err, {msg: 'turnout not found'});
            }
        } else {
            fn(err, {msg: 'failed to remove turnout '});
        }
    });
}

exports.clear = function (fn) {
    TurnoutModel.remove(function (err) {
        if (!err) {
            TurnoutGroupModel.remove(function (err) {
                if (!err) {
                    getAllTurnoutData(function (err, result) {
                        if (!err) {
                            fn(err, result);
                        }
                    });
                } else {
                    fn(err, {msg: 'failed to clear turnout groups'});
                }
            });
        } else {
            fn(err, {msg: 'failed to clear turnouts'});
        }
    });
}

/* PRIVATE HELPERS */
getAllTurnoutData = function (fn) {

    TurnoutGroupModel.find().exec(function (err, turnoutGroups) {
        if (err) {
            fn(err, {msg: 'failed to load turnout groups'});
        }
        TurnoutModel.find().exec(function (err, turnouts) {
            if (err) {
                fn(err, {msg: 'failed to load turnouts'});
            }
            var turnoutsByGroupId = {};
            for (t in turnouts) {
                var turnout = turnouts[t];
                if (!turnoutsByGroupId[turnouts[t].groupId]) {
                    turnoutsByGroupId[turnouts[t].groupId] = [];
                }
                var groupId = turnouts[t].groupId;
                turnoutsByGroupId[groupId].push(turnout.toJSON());
            }
            var result = [];
            for (g in turnoutGroups) {
                var group = turnoutGroups[g].toJSON();
                var groupId = group.id;
                var turnoutsOfAGroup = turnoutsByGroupId[groupId];
                group['turnouts'] = turnoutsOfAGroup;
                result.push(group);
            }
            fn(err, result);
        });
    });
}

validateTurnout = function (turnout, fn) {
    if (!turnout.number || turnout.number < 1) {
        fn(true, {msg: 'number must be greater 0'});
        return false;
    }
    if (!turnout.bus1 || turnout.bus1 < 1) {
        fn(true, {msg: 'bus 1 must be greater 0'});
        return false;
    }

    if (!turnout.address1 || turnout.address1 < 1) {
        fn(true, {msg: 'address 1 must be greater 0'});
        return false;
    }

    if (!turnout.type) {
        fn(true, {msg: 'turnout type must be specified'});
        return false;
    }
    turnout.type = turnout.type.toUpperCase();
    turnout.defaultState = turnout.defaultState.toUpperCase();
    turnout.orientation = turnout.orientation.toUpperCase();
    if (turnout.type === "THREEWAY") {
        if (!turnout.bus2 || turnout.bus2 < 1) {
            fn(true, {msg: 'bus 2 must be greater 0'});
            return false;
        }

        if (!turnout.address2 || turnout.address2 < 1) {
            fn(true, {msg: 'address 2 must be greater 0'});
            return false;
        }
    }
    return true;
}
