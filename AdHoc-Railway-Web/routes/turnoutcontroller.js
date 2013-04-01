
TurnoutGroupModel = require('../models/turnout_model').TurnoutGroupModel;
TurnoutModel = require('../models/turnout_model').TurnoutModel;
var mongoose = require('mongoose');

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

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
    }else {
      fn('failed to find all turnout groups');
    }
  });
}

exports.getTurnoutGroupById = function(socket, turnoutGroupId, fn) {
  console.log('turnoutGroup:getById: ' + turnoutGroupId);
  TurnoutGroupModel.findById(turnoutGroupId, function(err, turnoutGroup) {
    if(!err) {
      fn(turnoutGroup);
    }else{
      fn('failed to find turnout group with id ' + turnoutGroupId);
    }
  });
}

exports.addTurnoutGroup = function(socket, turnoutGroup, fn) {
  if(turnoutGroup.name == null || turnoutGroup.name.length == 0) {
    fn(true, 'name must be defined');
    return;
  }
  var group = new TurnoutGroupModel(turnoutGroup);
  group.save(function(err, addedTurnoutGroup) {
    if(!err) {
      var turnoutGroup = addedTurnoutGroup.toJSON();
      turnoutGroup.turnouts = {};
      socket.broadcast.emit('turnoutGroup:added', turnoutGroup);
      fn(false, 'success', turnoutGroup._id);
    } else {
      fn(true, 'failed to save turnout group');
    }
  });
}

exports.updateTurnoutGroup = function(socket, turnoutGroup, fn) {
  if(turnoutGroup.name == null || turnoutGroup.name.length == 0) {
    fn(true, 'name must be defined');
    return;
  }
  console.log('updating turnout group' + JSON.stringify(turnoutGroup));

  var id = turnoutGroup._id;
  delete turnoutGroup._id;

  TurnoutGroupModel.update({_id: id}, turnoutGroup, function(err,  numberAffected, rawResponse){
    if(!err) {
      turnoutGroup._id = id;
      socket.broadcast.emit('turnoutGroup:updated', turnoutGroup);
      fn(false, ''); 
    }else {
      fn(true, 'failed to update turnout group');
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
          fn(true, 'failed to remove turnout group');
        }
      });
    } else {
      fn(true, 'failed to remove the turnouts associated to turnout group');
    }
  });
}

exports.getAllTurnouts = function(socket,fn) {

  TurnoutModel.find(function (err, turnouts) {
    if(!err) {
      fn(false, '', turnouts);
    } else {
      fn(true, 'failed to find all turnouts');
    }
  });
}

exports.getById = function(socket, turnoutId, fn) {
  console.log('turnout:getById: ' + turnoutId);
  TurnoutModel.findById(turnoutId, function(err, turnout) {
    if(!err) {
      fn(turnout);
    }else{
      fn('failed to find turnout with id ' + turnoutId);
    }
  });
}

exports.addTurnout = function(socket, turnout, fn) {

  if(!validateTurnout(turnout, fn)){
    return;
  }

  console.log('adding new turnout ' + JSON.stringify(turnout));
  new TurnoutModel(turnout).save(function(err, addedTurnout) {
    if(!err) {
      console.log(addedTurnout.group);
      TurnoutGroupModel.findById({_id: addedTurnout.group}, function(err, turnoutGroup) {
        if(!err) {
          console.log(turnoutGroup);
          turnoutGroup.turnouts.push(addedTurnout._id);
          turnoutGroup.save();
          socket.broadcast.emit('turnout:added', addedTurnout);
          fn(false, 'success', addedTurnout._id);
        } else {
          fn(true, 'failed to add turnout');
        }
      });
    }else{
      fn(true, 'failed to add turnout');
    }
  });

}

exports.updateTurnout = function(socket, turnout, fn) {
  if(!validateTurnout(turnout, fn)){
    return;
  }

  console.log('updating turnout ' + JSON.stringify(turnout));

  var id = turnout._id;
  delete turnout._id;

  TurnoutModel.update({_id: id}, turnout, function(err,  numberAffected, rawResponse){
    if(!err) {
      turnout._id = id;
      socket.broadcast.emit('turnout:updated', turnout);
      fn(false, ''); 
    }else {
      fn(true, 'failed to update turnout');
    }
  });
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
          fn(true, 'failed to update turnout group');
        }
      });
    }else {
      fn(true, 'failed to remove turnout ');       
    }
  });
}

exports.clear = function(socket, fn) {
  TurnoutModel.remove( function (err) {
    if(!err) {
      TurnoutGroupModel.remove( function (err) {
        if(!err) {
          getAllTurnoutData(function(err, result) {
            if(!err) {
              socket.broadcast.emit('turnout:init', result);
              fn(false, '', result);
            }
          });
        } else {
          fn(true, 'failed to clear turnout groups','');
        }
      });
    } else {
      fn(true, 'failed to clear turnouts','');
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

validateTurnout = function(turnout, fn) {
  if (!turnout.number || turnout.number < 1) {
    fn(true, 'number must be greater 0');
    return false;
  }
  if(!turnout.bus1 || turnout.bus1 < 1) {
    fn(true, 'bus 1 must be greater 0');
    return false;
  }

  if(!turnout.address1 || turnout.address1 < 1) {
    fn(true, 'address 1 must be greater 0');
    return false;
  }

  if(turnout.type.toUpperCase() === "threeway".toUpperCase()) {
    if(!turnout.bus2 || turnout.bus2 < 1) {
      fn(true, 'bus 2 must be greater 0');
      return false;
    }

    if(!turnout.address2 || turnout.address2 < 1) {
      fn(true, 'address 2 must be greater 0');
      return false;
    }
  }
  return true;
}
