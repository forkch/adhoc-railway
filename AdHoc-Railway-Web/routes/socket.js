/*
 * Serve content over a socket
 */
TurnoutGroupModel = require('../models/turnout_model').TurnoutGroupModel;
TurnoutModel = require('../models/turnout_model').TurnoutModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;
mongoose.connect('mongodb://localhost/baehnle');

/*var t1 = new TurnoutModel({
            number:"1",
            bus1:"1",
            address1:"2",
            bus2:"",
            address2:"",
            type:"default"
        }).save();
var t2 = new TurnoutModel({
            number:"2",
            bus1:"1",
            address1:"2",
            bus2:"",
            address2:"",
            type:"default"
        }).save();
new TurnoutGroupModel({
    name: "HB",
    turnouts : [t1,t2]

}).save(function(err) {
    console.log('yay');
});*/

module.exports = function (socket) {

    TurnoutModel.find( function(err, turnouts) {
        console.log(turnouts);
        socket.emit('init', turnouts);
    });


    socket.on('turnout:add', function (turnout, fn) {
        if (turnout.number >= 1) {
            console.log('adding new turnout ' + JSON.stringify(turnout));
            new TurnoutModel(turnout).save(function(err) {
                if(!err) {
                    socket.broadcast.emit('turnout:added', turnout);
                    fn(true, '');
                }else{
                    fn(false, 'error adding turnout');
                }
            })
        } else {
            fn(false, 'number should be greater 0');
        }
    });

    socket.on('turnout:update', function (turnout, fn) {
        if (turnout.number >= 1) {
            console.log('updating turnout ' + JSON.stringify(turnout));
            
            var id = turnout._id;
            delete turnout._id;

            TurnoutModel.update({_id: id}, turnout, function(err,  numberAffected, rawResponse){
                if(!err) {
                    socket.broadcast.emit('turnout:updated', turnout);
                    fn(true, ''); 
                }else {
                    fn(false, err);
                }
            }); 
        } else {
            fn(false, 'number should be greater 0');
        }
    });

    socket.on('turnout:delete', function (turnoutId, fn) {
        
        console.log('deleting turnout ' + turnoutId);
        TurnoutModel.remove({_id: turnoutId}, function(err) {
            if(!err) {
                fn(true, '');
            }else {
                fn(false, err);
            }
        });
        
    });

    socket.on('turnouts:getAll', function (dummy, fn) {
        console.log('turnouts:getAll');
        TurnoutModel.find( function(err, turnouts) {
            fn(turnouts);
        });
    });

    socket.on('turnout:getById', function (id, fn) {
        console.log('turnout:getById: ' + id);
        TurnoutModel.findById(id, function(err, turnout) {
            if(!err) {
                fn(turnout);
            }else{
                fn(null);
            }
        });
    });

    socket.on('locomotives:getAll', function (dummy, fn) {
        //fn(data.locomotives);
    });

};
