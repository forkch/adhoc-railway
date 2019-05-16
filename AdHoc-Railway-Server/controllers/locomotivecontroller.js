LocomotiveGroupModel = require('../models/locomotive_model').LocomotiveGroupModel;
LocomotiveModel = require('../models/locomotive_model').LocomotiveModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

var colorize = require('colorize');

exports.init = function (fn) {
    getAllLocomotiveData(function (err, result) {
        if (!err) {
            fn(err, result);
        }
    });
}

exports.getAllLocomotiveGroups = function (fn) {
    console.log("getAllLocomotiveGroups");
    getAllLocomotiveData(function (err, result) {
        if (!err) {
            fn(err, result);
        } else {
            fn(err, {msg: 'failed to find all locomotive groups'});
        }
    });
}

exports.getLocomotiveGroupById = function (locomotiveGroupId, fn) {
    console.log('getLocomotiveGroupById: ' + locomotiveGroupId);
    LocomotiveGroupModel.findById(locomotiveGroupId, function (err, locomotiveGroup) {
        if (!err) {
            fn(err, locomotiveGroup);
        } else {
            fn(err, {msg: 'failed to find locomotive group with id ' + locomotiveGroupId});
        }
    });
}

exports.addLocomotiveGroup = function (locomotiveGroup, fn) {
    if (locomotiveGroup.name == null || locomotiveGroup.name.length == 0) {
        fn(true, {msg: 'name must be defined'});
        return;
    }

    delete locomotiveGroup.id;
    delete locomotiveGroup.locomotives;
    console.log('adding new locomotive group: ' + locomotiveGroup.name);
    var group = new LocomotiveGroupModel(locomotiveGroup);
    group.save(function (err, addedlocomotiveGroup) {
        if (!err) {
            addedlocomotiveGroup.id = addedlocomotiveGroup._id;
            console.log('adding new locomotive group: ' + addedlocomotiveGroup.name + " with id: " + addedlocomotiveGroup.id);
            addedlocomotiveGroup.locomotives = [];
            fn(err, addedlocomotiveGroup.toJSON());
        } else {
            fn(err, {msg: 'failed to save locomotive group'});
        }
    });
}

exports.updateLocomotiveGroup = function (locomotiveGroup, fn) {
    if (locomotiveGroup.name == null || locomotiveGroup.name.length == 0) {
        fn(err, {msg: 'name must be defined'});
        return;
    }
    console.log('updating locomotive group ' + JSON.stringify(locomotiveGroup));

    LocomotiveGroupModel.update({_id: locomotiveGroup.id},
        {'$set': {
            'name': locomotiveGroup.name
        }
        }, function (err) {
            if (!err) {
                fn(err, locomotiveGroup);
            } else {
                fn(err, {msg: 'failed to update locomotive group'});
            }
        });
}

exports.removeLocomotiveGroup = function (locomotiveGroupId, fn) {
    console.log('remove locomotive group ' + locomotiveGroupId);
    LocomotiveModel.remove({"group": locomotiveGroupId}, function (err) {
            if (!err) {
                LocomotiveGroupModel.findById(locomotiveGroupId, function (err, locomotiveGroup) {
                    if (!err) {
                        locomotiveGroup.remove(function (err) {
                            if (!err) {
                                fn(err, locomotiveGroup.toJSON());
                            } else {
                                fn(err, {msg: 'failed to remove locomotive group'});
                            }
                        });

                    } else {
                        fn(err, 'failed to remove locomotive group');
                    }
                });
            }
            else {
                fn(err, 'failed to remove the locomotives associated to locomotive group');
            }
        }
    )
    ;
}


exports.getLocomotiveById = function (locomotiveId, fn) {
    console.log('locomotive:getById: ' + locomotiveId);
    LocomotiveModel.findById(locomotiveId, function (err, locomotive) {
        if (!err) {
            fn(err, locomotive);
        } else {
            fn(err, {msg: 'failed to find locomotive with id ' + locomotiveId});
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
                if (!err && locomotiveGroup) {
                    console.log(locomotiveGroup);
                    locomotiveGroup.locomotives.push(addedLocomotive.id);
                    locomotiveGroup.save();
                    fn(err, addedLocomotive.toJSON());
                } else {
                    console.error(err);
                    fn(err, {msg: 'failed to add locomotive to group'});
                }
            });
        } else {
            console.error(err);
            fn(err, {msg: 'failed to add locomotive'});
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
            fn(err, locomotive);
        } else {
            fn(err, 'failed to update locomotive');
        }
    });
}

exports.removeLocomotive = function (locomotiveId, fn) {
    console.log('remove locomotive ' + locomotiveId);
    LocomotiveModel.findById(locomotiveId, function (err, locomotive) {
        if (!err) {
            if (locomotive) {
                locomotive.remove(function (err) {
                    LocomotiveGroupModel.update({}, {$pull: {locomotives: locomotiveId}}, function (err, locomotiveGroup) {
                        if (!err) {
                            fn(err, locomotive.toJSON());
                        } else {
                            fn(err, 'failed to remove locomotive');
                        }
                    });
                });
            } else {
                fn(err, 'locomotive not found');
            }
        } else {
            fn(err, 'failed to remove locomotive ');
        }
    });
}

exports.clear = function (fn) {
    LocomotiveModel.remove(function (err) {
        if (!err) {
            LocomotiveGroupModel.remove(function (err) {
                if (!err) {
                    getAllLocomotiveData(function (err, result) {
                        if (!err) {
                            fn(err, result);
                        }
                    });
                } else {
                    fn(err, 'failed to clear locomotive groups', '');
                }
            });
        } else {
            fn(err, 'failed to clear locomotives', '');
        }
    });
}

/* PRIVATE HELPERS */
getAllLocomotiveData = function (fn) {
    LocomotiveGroupModel.find().exec(function (err, locomotiveGroups) {
        if (err) {
            fn(err, null);
        }
        LocomotiveModel.find().exec(function (err, locomotives) {
            if (err) {
                fn(err, null);
            }
            var locomotivesByGroupId = {};
            for (t in locomotives) {
                var locomotive = locomotives[t];
                if (!locomotivesByGroupId[locomotives[t].groupId]) {
                    locomotivesByGroupId[locomotives[t].groupId] = [];
                }
                var groupId = locomotives[t].groupId;
                locomotivesByGroupId[groupId].push(locomotive.toJSON());
            }
            var result = [];
            for (g in locomotiveGroups) {
                var group = locomotiveGroups[g].toJSON();
                var groupId = group.id;
                var locomotives = locomotivesByGroupId[groupId];
                group['locomotives'] = locomotives
                result.push(group);
            }
            fn(err, result);
        });
    });
}

validateLocomotive = function (locomotive, fn) {
    if (!locomotive.name || locomotive.name.length < 1) {
        fn(true, {msg: 'name must be specified'});
        return false;
    }
    if (!locomotive.type || locomotive.type.length < 1) {
        fn(true, {msg: 'type must be specified'});
        return false;
    }

    //if (!locomotive.bus || locomotive.bus < 1) {
    //    fn(true, {msg: 'bus must be greater 0'});
    //    return false;
   // }

    //if (!locomotive.address1 || locomotive.address1 < 1) {
    //    fn(true, {msg: 'address 1 must be greater 0'});
    //    return false;
    //}

    if (locomotive.type.toUpperCase() === "SIMULATED-MFX") {

        //if (!locomotive.address2 || locomotive.address2 < 1) {
        //    fn(true, {msg: 'address 2 must be greater 0'});
        //    return false;
        //}
    }
    return true;
}

