
TurnoutGroupModel = require('../models/turnout_model').TurnoutGroupModel;
TurnoutModel = require('../models/turnout_model').TurnoutModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;
mongoose.connect('mongodb://localhost/baehnle');

var colorize = require('colorize');
var cconsole = colorize.console;

exports.init = function(socket) {

    getAllTurnoutData(function(err, result) {
        if(!err) {
            socket.emit('turnout:init', result);
        }
    });
    
}

exports.getAllTurnoutGroups = function(socket,fn) {
    
    getAllTurnoutData(function(err, result) {
        if(!err) {
            fn(false, result);
        }
    });
}

exports.addTurnoutGroup = function(socket, turnoutGroup, fn) {
    var group = new TurnoutGroupModel(turnoutGroup);
    group.save(function(err, addedTurnoutGroup) {
        if(!err) {
            var turnoutGroup = addedTurnoutGroup.toJSON();
            turnoutGroup.turnouts = {};
            socket.broadcast.emit('turnoutGroup:added', turnoutGroup);
            fn(false, 'success', group);
        } else {
            fn(true, 'error adding turnout group');
        }
    });
}

exports.updateTurnoutGroup = function(socket, turnoutGroup, fn) {
    
    console.log('updating turnout group' + JSON.stringify(turnoutGroup));
    
    var id = turnoutGroup._id;
    delete turnoutGroup._id;

    TurnoutGroupModel.update({_id: id}, turnoutGroup, function(err,  numberAffected, rawResponse){
        if(!err) {
            turnoutGroup._id = id;
            socket.broadcast.emit('turnoutGroup:updated', turnoutGroup);
            fn(false, ''); 
        }else {
            fn(true, err);
        }
    });
}

exports.removeTurnoutGroup = function(socket, turnoutGroup, fn) {
    var id = turnoutGroup._id;
    console.log('remove turnout group ' + id);
    TurnoutModel.remove({"group": id}, function(err) {
        if(!err) {
            TurnoutGroupModel.remove({_id: id}, function(err) {
                if(!err) {
                    socket.broadcast.emit('turnoutGroup:removed', turnoutGroup);
                    fn(false, '');
                } else {
                    fn(true, err);
                }
            });
        } else {
            fn(true, err);
        }
    });
}


exports.getById = function(socket, turnoutId, fn) {
    console.log('turnout:getById: ' + turnoutId);
    TurnoutModel.findById(turnoutId, function(err, turnout) {
        if(!err) {
            fn(turnout);
        }else{
            fn(null);
        }
    });
}

exports.addTurnout = function(socket, turnout, fn) {
    if (!turnout.number || turnout.number < 1) {
        fn(true, 'number should be greater 0');
        return ;
    }
    if(!turnout.bus1 || turnout.bus1 < 1) {
        fn(true, 'bus 1 should be greater 0');
        return;
    }

    if(!turnout.address1 || turnout.address1 < 1) {
        fn(true, 'address 1 should be greater 0');
        return;
    }
    
    console.log('adding new turnout ' + JSON.stringify(turnout));
    new TurnoutModel(turnout).save(function(err, addedTurnout) {
        if(!err) {
            console.log(addedTurnout.group);
            TurnoutGroupModel.findById({_id: addedTurnout.group}, function(err, turnoutGroup) {
                console.log(turnoutGroup);
                turnoutGroup.turnouts.push(addedTurnout._id);
                turnoutGroup.save();
            });
            socket.broadcast.emit('turnout:added', addedTurnout);
            fn(false, 'success');
        }else{
            fn(true, 'error adding turnout');
        }
    });
    
}

exports.updateTurnout = function(socket, turnout, fn) {
    if (turnout.number >= 1) {
        console.log('updating turnout ' + JSON.stringify(turnout));
        
        var id = turnout._id;
        delete turnout._id;

        TurnoutModel.update({_id: id}, turnout, function(err,  numberAffected, rawResponse){
            if(!err) {
                turnout._id = id;
                socket.broadcast.emit('turnout:updated', turnout);
                fn(false, ''); 
            }else {
                fn(true, err);
            }
        });
    } else {
        fn(false, 'number should be greater 0');
    }
}

exports.removeTurnout = function(socket, turnout, fn) {
    console.log('remvove turnout ' + turnout._id);
    var turnoutId = turnout._id;
    TurnoutModel.remove({_id: turnoutId}, function(err) {
        if(!err) {
            TurnoutGroupModel.update(
                {}, {$pull: {turnouts: turnoutId}}, function(err, turnoutGroup) {
                if(!err) {
                    socket.broadcast.emit('turnout:removed', turnout);
                    fn(false, '');
                }else {
                    fn(true, err);
                }
            });
        }else {
            fn(true, err);         
        }
    });
}

/* PRIVATE HELPERS */
getAllTurnoutData = function(fn) {

    TurnoutGroupModel.find().lean().exec(function(err, turnoutGroups) {
        
        if(err) {
            fn(true, null);
        }
        TurnoutModel.find().lean().exec(function(err, turnouts) {
            if(err) {
               fn(true, null);
            }    
            var turnoutByGroupId = [];
            for(t in turnouts) {
                console.log(turnouts[t].group + " --> " + JSON.stringify(turnouts[t]));
                if(!turnoutByGroupId[turnouts[t].group]) {
                    turnoutByGroupId[turnouts[t].group] = {};
                }
                var turnoutId = turnouts[t]._id;
                var obj = {};
                obj[turnoutId] = turnouts[t];

                turnoutByGroupId[turnouts[t].group][turnoutId] = turnouts[t];
            }
            
            var result = {'turnoutGroups': []};
            for(g in turnoutGroups) {
                var groupId = turnoutGroups[g]._id;
                turnoutGroups[g].turnouts = [];
                turnoutGroups[g].turnouts = turnoutByGroupId[groupId];
                result.turnoutGroups.push(turnoutGroups[g]);
            }

            fn(false, result);
        });
    });
}