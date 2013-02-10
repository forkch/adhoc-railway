
TurnoutGroupModel = require('../models/turnout_model').TurnoutGroupModel;
TurnoutModel = require('../models/turnout_model').TurnoutModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;
mongoose.connect('mongodb://localhost/baehnle');

exports.init = function(socket) {

    TurnoutModel.find(function(err, turnouts) {
    	console.log(turnouts);
        socket.emit('init', {"turnouts": turnouts});
    });
}
exports.addTurnout = function(socket, turnout, fn) {
	if (turnout.number >= 1) {
	        console.log('adding new turnout ' + JSON.stringify(turnout));
	        new TurnoutModel(turnout).save(function(err, addedTurnout) {
	            if(!err) {
	                socket.broadcast.emit('turnout:added', addedTurnout);
	                fn(true, 'success');
	            }else{
	                fn(false, 'error adding turnout');
	            }
	        })
    } else {
        fn(false, 'number should be greater 0');
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
                fn(true, ''); 
            }else {
                fn(false, err);
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
            console.log("success");
            fn(true, '');
            socket.broadcast.emit('turnout:removed', turnoutId);
        }else {
            fn(false, err);
        }
    });
}

exports.getAll = function(socket,fn) {
	console.log('turnouts:getAll');
    TurnoutModel.find( function(err, turnouts) {
        fn(turnouts);
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