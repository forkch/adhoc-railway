var mongoose = require('mongoose');
var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

exports.LocomotiveSchema = new Schema({
  name: {type: String, index: true},
  bus: Number,
  address1: Number,
  address2: Number,
  type: String,
  image: String,
  imageBase64: String,
  description: String,
  group:  ObjectId,
  functions : [
    {
    number: Number,
    description: String,
    emergencyBrakeFunction: Boolean,
    deactivationDelay: Number
  }
  ]

});

exports.LocomotiveGroupSchema = new Schema({
  name: String,
  locomotives : [ObjectId]
});


exports.LocomotiveModel = mongoose.model('Locomotive', exports.LocomotiveSchema);
exports.LocomotiveGroupModel = mongoose.model('LocomotiveGroup', exports.LocomotiveGroupSchema);
