
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
            socket.emit('init', result);
        }
    });
    
}

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
                console.log(obj);

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

exports.addTurnoutGroup = function(socket, turnoutGroupName, fn) {
	var group = new TurnoutGroupModel({name: turnoutGroupName});
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

exports.addTurnout = function(socket, turnout, fn) {
	if (turnout.number < 1) {
        fn(true, 'number should be greater 0');
	} else {
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
        })
    }
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

exports.removeTurnout = function(socket, turnoutId, fn) {
    console.log('remvove turnout ' + turnoutId);
    TurnoutModel.remove({_id: turnoutId}, function(err) {
        if(!err) {
            TurnoutGroupModel.update(
                {}, {$pull: {turnouts: turnoutId}}, function(err, turnoutGroup) {
                if(!err) {
                    fn(false, '');
                    socket.broadcast.emit('turnout:removed', turnoutId);
                }else {
                    console.log("error");
                    fn(true, err);
                }
            });
        }else {
            fn(true, err);         
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