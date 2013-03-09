LocomotiveGroupModel = require('../models/locomotive_model').LocomotiveGroupModel;
LocomotiveModel = require('../models/locomotive_model').LocomotiveModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

var colorize = require('colorize');
var cconsole = colorize.console;

exports.init = function(socket) {
    getAllLocomotiveData(function(err, result) {
        if(!err) {
            socket.emit('locomotive:init', result);
        }
    });
}

exports.getAllLocomotiveGroups = function(socket,fn) {
    getAllLocomotiveData(function(err, result) {
        if(!err) {
            fn(false, result);
        }else {
            fn('failed to find all locomotive groups');
        }
    });
}

exports.getLocomotiveGroupById = function(socket, locomotiveGroupId, fn) {
    console.log('locomotiveGroup:getById: ' + locomotiveGroupId);
    LocomotiveGroupModel.findById(locomotiveGroupId, function(err, locomotiveGroup) {
        if(!err) {
            fn(locomotiveGroup);
        }else{
            fn('failed to find locomotive group with id ' + locomotiveGroupId);
        }
    });
}

exports.addLocomotiveGroup = function(socket, locomotiveGroup, fn) {
    if(locomotiveGroup.name == null || locomotiveGroup.name.length == 0) {
        fn(true, 'name must be defined');
        return;
    }
    var group = new LocomotiveGroupModel(locomotiveGroup);
    group.save(function(err, addedlocomotiveGroup) {
        if(!err) {
            var locomotiveGroup = addedlocomotiveGroup.toJSON();
            locomotiveGroup.locomotives = {};
            socket.broadcast.emit('locomotiveGroup:added', locomotiveGroup);
            fn(false, 'success', group);
        } else {
            fn(true, 'failed to save locomotive group');
        }
    });
}

exports.updateLocomotiveGroup = function(socket, locomotiveGroup, fn) {
    if(locomotiveGroup.name == null || locomotiveGroup.name.length == 0) {
        fn(true, 'name must be defined');
        return;
    }
    console.log('updating locomotive group' + JSON.stringify(locomotiveGroup));
    
    var id = locomotiveGroup._id;
    delete locomotiveGroup._id;

    LocomotiveGroupModel.update({_id: id}, locomotiveGroup, function(err,  numberAffected, rawResponse){
        if(!err) {
            locomotiveGroup._id = id;
            socket.broadcast.emit('locomotiveGroup:updated', locomotiveGroup);
            fn(false, ''); 
        }else {
            fn(true, 'failed to update locomotive group');
        }
    });
}

exports.removeLocomotiveGroup = function(socket, locomotiveGroup, fn) {
    var id = locomotiveGroup._id;
    console.log('remove locomotive group ' + id);
    LocomotiveModel.remove({"group": id}, function(err) {
        if(!err) {
            LocomotiveGroupModel.remove({_id: id}, function(err) {
                if(!err) {
                    socket.broadcast.emit('locomotiveGroup:removed', locomotiveGroup);
                    fn(false, '');
                } else {
                    fn(true, 'failed to remove locomotive group');
                }
            });
        } else {
            fn(true, 'failed to remove the locomotives associated to locomotive group');
        }
    });
}


exports.getById = function(socket, locomotiveId, fn) {
    console.log('locomotive:getById: ' + locomotiveId);
    LocomotiveModel.findById(locomotiveId, function(err, locomotive) {
        if(!err) {
            fn(locomotive);
        }else{
            fn('failed to find locomotive with id ' + locomotiveId);
        }
    });
}

exports.addLocomotive = function(socket, locomotive, fn) {
    if(!locomotive.bus || locomotive.bus < 1) {
        fn(true, 'bus must be greater 0');
        return;
    }

    if(!locomotive.address1 || locomotive.address1 < 1) {
        fn(true, 'address 1 must be greater 0');
        return;
    }
    
    console.log('adding new locomotive ' + JSON.stringify(locomotive));
    new Locomotive(locomotive).save(function(err, addedLocomotive) {
        if(!err) {
            console.log(addedLocomotive.group);
            LocomotiveGroupModel.findById({_id: addedLocomotive.group}, function(err, locomotiveGroup) {
                console.log(locomotiveGroup);
                locomotiveGroup.locomotives.push(addedLocomotive._id);
                locomotiveGroup.save();
            });
            socket.broadcast.emit('locomotive:added', addedLocomotive);
            fn(false, 'success');
        }else{
            fn(true, 'failed to add locomotive');
        }
    });
    
}

exports.updateLocomotive = function(socket, locomotive, fn) {
    console.log('updating locomotive ' + JSON.stringify(locomotive));
    
    var id = locomotive._id;
    delete locomotive._id;

    LocomotiveModel.update({_id: id}, locomotive, function(err,  numberAffected, rawResponse){
        if(!err) {
            locomotive._id = id;
            socket.broadcast.emit('locomotive:updated', locomotive);
            fn(false, ''); 
        }else {
        fn(true, 'failed to update locomotive');
        }
    });
}

exports.removeLocomotive = function(socket, locomotive, fn) {
    console.log('remvove locomotive ' + locomotive._id);
    var locomotiveId = locomotive._id;
    LocomotiveModel.remove({_id: locomotiveId}, function(err) {
        if(!err) {
            LocomotiveGroupModel.update(
                {}, {$pull: {locomotives: locomotiveId}}, function(err, locomotiveGroup) {
                if(!err) {
                    socket.broadcast.emit('locomotive:removed', locomotive);
                    fn(false, '');
                }else {
                    fn(true, 'failed to update locomotive group');
                }
            });
        }else {
            fn(true, 'failed to remove locomotive ');       
        }
    });
}

/* PRIVATE HELPERS */
getAllLocomotiveData = function(fn) {

    LocomotiveGroupModel.find().lean().exec(function(err, locomotiveGroups) {
        
        if(err) {
            fn(true, null);
        }
        LocomotiveModel.find().lean().exec(function(err, locomotives) {
            if(err) {
               fn(true, null);
            }    
            var locomotiveByGroupId = [];
            for(t in locomotives) {
                console.log(locomotives[t].group + " --> " + JSON.stringify(locomotives[t]));
                if(!locomotiveByGroupId[locomotives[t].group]) {
                    locomotiveByGroupId[locomotives[t].group] = {};
                }
                var locomotiveId = locomotives[t]._id;
                var obj = {};
                obj[locomotiveId] = locomotives[t];

                locomotiveByGroupId[locomotives[t].group][locomotiveId] = locomotives[t];
            }
            
            var result = {'locomotiveGroups': []};
            for(g in locomotiveGroups) {
                var groupId = locomotiveGroups[g]._id;
                locomotiveGroups[g].locomotives = [];
                locomotiveGroups[g].locomotives = locomotiveByGroupId[groupId];
                result.locomotiveGroups.push(locomotiveGroups[g]);
            }

            fn(false, result);
        });
    });
}

