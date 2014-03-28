LocomotiveGroupModel = require('../models/locomotive_model').LocomotiveGroupModel;
LocomotiveModel = require('../models/locomotive_model').LocomotiveModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

var colorize = require('colorize');

exports.init = function (socket) {
    getAllLocomotiveData(function (err, result) {
        if (!err) {
            socket.emit('locomotive:init', result);
        }
    });
}

exports.getAllLocomotiveGroups = function (fn) {
    console.log("getAllLocomotiveGroups");
    getAllLocomotiveData(function (err, result) {
        if (!err) {
            fn(false, result);
        } else {
            fn(true, 'failed to find all locomotive groups');
        }
    });
}

exports.getLocomotiveGroupById = function (locomotiveGroupId, fn) {
    console.log('getLocomotiveGroupById: ' + locomotiveGroupId);
    LocomotiveGroupModel.findById(locomotiveGroupId, function (err, locomotiveGroup) {
        if (!err) {
            fn(false, locomotiveGroup);
        } else {
            fn('failed to find locomotive group with id ' + locomotiveGroupId);
        }
    });
}

exports.addLocomotiveGroup = function (locomotiveGroup, fn) {
    if (locomotiveGroup.name == null || locomotiveGroup.name.length == 0) {
        fn(true, 'name must be defined');
        return;
    }

    console.log('adding new locomotive group: ' + locomotiveGroup.name);
    var group = new LocomotiveGroupModel(locomotiveGroup);
    group.save(function (err, addedlocomotiveGroup) {
        console.log(err);
        if (!err) {
            var locomotiveGroup = addedlocomotiveGroup.toJSON();
            locomotiveGroup.locomotives = [];
            fn(false, locomotiveGroup);
        } else {
            fn(true, 'failed to save locomotive group');
        }
    });
}

exports.updateLocomotiveGroup = function (locomotiveGroup, fn) {
    if (locomotiveGroup.name == null || locomotiveGroup.name.length == 0) {
        fn(true, 'name must be defined');
        return;
    }
    console.log('updating locomotive group' + JSON.stringify(locomotiveGroup));

    var id = locomotiveGroup.id;
    delete locomotiveGroup.id;

    LocomotiveGroupModel.update({_id: id}, locomotiveGroup, function (err, numberAffected, rawResponse) {
        if (!err) {
            locomotiveGroup.id = id;
            fn(false, locomotiveGroup);
        } else {
            fn(true, 'failed to update locomotive group');
        }
    });
}

exports.removeLocomotiveGroup = function (locomotiveGroupId, fn) {
    console.log('remove locomotive group ' + locomotiveGroupId);
    LocomotiveModel.remove({"group": locomotiveGroupId}, function (err) {
        if (!err) {
            LocomotiveGroupModel.remove({_id: locomotiveGroupId}, function (err) {
                if (!err) {
                    fn(false, locomotiveGroupId);
                } else {
                    fn(true, 'failed to remove locomotive group');
                }
            });
        } else {
            fn(true, 'failed to remove the locomotives associated to locomotive group');
        }
    });
}


exports.getLocomotiveById = function (locomotiveId, fn) {
    console.log('locomotive:getById: ' + locomotiveId);
    LocomotiveModel.findById(locomotiveId, function (err, locomotive) {
        if (!err) {
            fn(false, locomotive);
        } else {
            fn(true, 'failed to find locomotive with id ' + locomotiveId);
        }
    });
}

exports.addLocomotive = function (locomotive, fn) {

    if (!validateLocomotive(locomotive, fn)) {
        return;
    }
    console.log('adding new locomotive ' + JSON.stringify(locomotive));
    new LocomotiveModel(locomotive).save(function (err, addedLocomotive) {
        if (!err) {
            console.log(addedLocomotive.groupId);
            LocomotiveGroupModel.findById({_id: addedLocomotive.groupId}, function (err, locomotiveGroup) {
                if (!err) {
                    console.log(locomotiveGroup);
                    locomotiveGroup.locomotives.push(addedLocomotive.id);
                    locomotiveGroup.save();
                    fn(false, addedLocomotive);
                } else {
                    console.error(err);
                    fn(true, 'failed to add locomotive to group');
                }
            });
        } else {
            console.error(err);
            fn(true, 'failed to add locomotive');
        }
    });

}

exports.updateLocomotive = function (locomotive, fn) {
    console.log('updating locomotive ' + JSON.stringify(locomotive));
    if (!validateLocomotive(locomotive, fn)) {
        return;
    }

    var id = locomotive.id;
    delete locomotive.id;

    LocomotiveModel.update({_id: id}, locomotive, function (err, numberAffected, rawResponse) {
        if (!err) {
            locomotive.id = id;

            fn(false, locomotive);
        } else {
            fn(true, 'failed to update locomotive');
        }
    });
}

exports.removeLocomotive = function (locomotiveId, fn) {
    console.log('remove locomotive ' + locomotiveId);
    LocomotiveModel.remove({_id: locomotiveId}, function (err) {
        if (!err) {
            LocomotiveGroupModel.update(
                {}, {$pull: {locomotives: locomotiveId}}, function (err, locomotiveGroup) {
                    if (!err) {
                        fn(false, locomotiveId);
                    } else {
                        fn(true, 'failed to update locomotive group');
                    }
                });
        } else {
            fn(true, 'failed to remove locomotive ');
        }
    });
}

exports.clear = function (socket, fn) {
    LocomotiveModel.remove(function (err) {
        if (!err) {
            LocomotiveGroupModel.remove(function (err) {
                if (!err) {
                    getAllLocomotiveData(function (err, result) {
                        if (!err) {
                            socket.broadcast.emit('locomotive:init', result);
                            fn(false, '', result);
                        }
                    });
                } else {
                    fn(true, 'failed to clear locomotive groups', '');
                }
            });
        } else {
            fn(true, 'failed to clear locomotives', '');
        }
    });
}

/* PRIVATE HELPERS */
getAllLocomotiveData = function (fn) {
    LocomotiveGroupModel.find().exec(function (err, locomotiveGroups) {
        if (err) {
            fn(true, null);
        }
        LocomotiveModel.find().exec(function (err, locomotives) {
            if (err) {
                fn(true, null);
            }
            var locomotivesByGroupId = {};
            for (t in locomotives) {
                var locomotive = locomotives[t];
                if (!locomotivesByGroupId[locomotives[t].groupId]) {
                    locomotivesByGroupId[locomotives[t].groupId] = [];
                }
                var groupdId = locomotives[t].groupId;
                locomotivesByGroupId[groupdId].push(locomotive.toJSON());
            }
            var result = [];
            for (g in locomotiveGroups) {
                var group = locomotiveGroups[g].toJSON();
                var groupId = locomotiveGroups[g]._id;
                var locomotives = locomotivesByGroupId[groupdId];
                group['locomotives'] = locomotives
                result.push(group);
            }
            fn(false, result);
        });
    });
}

validateLocomotive = function (locomotive, fn) {
    if (!locomotive.name || locomotive.name.length < 1) {
        fn(true, 'name must be specified');
        return false;
    }
    if (!locomotive.type || locomotive.type.length < 1) {
        fn(true, 'type must be specified');
        return false;
    }

    if (!locomotive.bus || locomotive.bus < 1) {
        fn(true, 'bus must be greater 0');
        return false;
    }

    if (!locomotive.address1 || locomotive.address1 < 1) {
        fn(true, 'address 1 must be greater 0');
        return false;
    }

    if (locomotive.type.toUpperCase() === "SIMULATED-MFX") {

        if (!locomotive.address2 || locomotive.address2 < 1) {
            fn(true, 'address 2 must be greater 0');
            return false;
        }
    }
    return true;
}

